package mgi.tools.jtransformer.api.code.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import mgi.tools.jtransformer.api.ClassNode;
import mgi.tools.jtransformer.api.MethodNode;
import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodeNode;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.TryCatchInformation;
import mgi.tools.jtransformer.api.code.flow.ConditionalJumpNode;
import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;
import mgi.tools.jtransformer.api.code.flow.ReturnNode;
import mgi.tools.jtransformer.api.code.flow.SwitchNode;
import mgi.tools.jtransformer.api.code.flow.UnconditionalJumpNode;
import mgi.tools.jtransformer.api.code.general.CastExpression;
import mgi.tools.jtransformer.api.code.general.InvokeExpression;
import mgi.tools.jtransformer.api.code.general.PopableNode;
import mgi.tools.jtransformer.api.code.math.MathematicalExpression;
import mgi.tools.jtransformer.api.code.memory.ArrayAssignmentExpression;
import mgi.tools.jtransformer.api.code.memory.ArrayAssignmentNode;
import mgi.tools.jtransformer.api.code.memory.FieldAssignmentExpression;
import mgi.tools.jtransformer.api.code.memory.FieldAssignmentNode;
import mgi.tools.jtransformer.api.code.memory.NewObjectExpression;
import mgi.tools.jtransformer.api.code.memory.NewUninitializedObjectExpression;
import mgi.tools.jtransformer.api.code.memory.RawVariableAssignmentExpression;
import mgi.tools.jtransformer.api.code.memory.RawVariableAssignmentNode;
import mgi.tools.jtransformer.api.code.memory.RawVariableLoadExpression;
import mgi.tools.jtransformer.api.code.tools.NodeExplorer;
import mgi.tools.jtransformer.api.code.tools.Utilities;
import mgi.tools.jtransformer.api.code.tools.VariablesAnalyzer;
import mgi.tools.jtransformer.utilities.MutableInteger;
import mgi.tools.jtransformer.utilities.MutableReference;

public class CodeOptimizer {

	/**
	 * Method , whose code we are
	 * optimizing.
	 * Used to obtain option flag's from 
	 * class node instance.
	 */
	private MethodNode method;
	/**
	 * Main node of the code.
	 */
	private CodeNode code;
	/**
	 * Contains variable's analyzer.
	 */
	private VariablesAnalyzer analyzer;
	/**
	 * Contains label's table.
	 */
	private Map<LabelAnnotation, Integer> labelsTable;
	/**
	 * Start address of stack dumps.
	 */
	private int stackDumpsStartAddress;
	
	public CodeOptimizer(MethodNode method, CodeNode code, int stackDumpsStartAddress) {
		this.method = method;
		this.code = code;
		this.stackDumpsStartAddress = stackDumpsStartAddress;
	}
	
	/**
	 * Optimize's given code.
	 */
	public void optimize() {
		analyzeLocals();
		generateLabelsTable();
		int total = 0;
		do {
			total = 0;

			//total += doReduceCasts();
			total += doInlineLocalVariables();
			total += doRemoveUnusedLocalVariables();

			total += doTransformNewObjects();
			//total += doTransformLocalVariablesAssign();
			
			total += doTrimCode();
		}
		while (total > 0);

		//List<VariablesAnalyzer.VariableInformation> info = analyzer.getAll();
		//System.err.println("Done");
	}
	
	private void analyzeLocals() {
		analyzer = new VariablesAnalyzer(method, code);
		analyzer.analyze();
	}
	
	
	private void generateLabelsTable() {
		labelsTable = new HashMap<LabelAnnotation, Integer>();
		NodeExplorer explorer = new NodeExplorer(code) {
			@Override
			public void onVisit(AbstractCodeNode n) {
				// TODO subroutines
				if (n instanceof UnconditionalJumpNode)
					increaseImportantUses(((UnconditionalJumpNode)n).getTarget());
				else if (n instanceof ConditionalJumpNode)
					increaseImportantUses(((ConditionalJumpNode)n).getTarget());
				else if (n instanceof SwitchNode) {
					SwitchNode sw = (SwitchNode)n;
					for (LabelAnnotation target : sw.getTargets())
						increaseImportantUses(target);
					increaseImportantUses(sw.getDefault());
				}
			}
		};
		explorer.explore();
		
		for (TryCatchInformation info : code.getTryCatches()) {
			increaseImportantUses(info.getStart());
			increaseImportantUses(info.getEnd());
			increaseImportantUses(info.getHandler());
		}
	}
	
	private int importantUsesCount(LabelAnnotation target) {
		if (!labelsTable.containsKey(target))
			return 0;
		return labelsTable.get(target).intValue();
	}
	
	private void increaseImportantUses(LabelAnnotation target) {
		if (!labelsTable.containsKey(target))
			labelsTable.put(target, 1);
		else
			labelsTable.put(target, labelsTable.get(target).intValue() + 1);
	}
	

	private int doInlineLocalVariables() {
		int total = 0;
		
		List<VariablesAnalyzer.VariableInformation> variables = analyzer.getAll();
		for (VariablesAnalyzer.VariableInformation information : variables) {
			if (information.isArgument() || information.getWrites().size() < 1 || information.getReads().size() < 1)
				continue;
			if ((method.getParent().getOptions() & ClassNode.OPT_OPTIMIZATION_SKIP_NONSYNTH_LOCALS) != 0 && information.getIndex() < stackDumpsStartAddress)
				continue;

			for (AbstractCodeNode x : information.getWrites()) {
				if (!(x instanceof RawVariableAssignmentNode) && !(x instanceof RawVariableAssignmentExpression))
					continue;
				
				final AbstractCodeNode write = x;
				final MutableReference<AbstractCodeNode> tail = new MutableReference<AbstractCodeNode>();
				new NodeExplorer(code) {
					@Override
					public void onVisit(AbstractCodeNode n) {
						if (n != write)
							return;
						
						AbstractCodeNode root = getDepth() >= 2 ? getCurrent(2) : n;
						boolean reached = false;
						for (AbstractCodeNode y = root;;) {
							if (y == write) {
								reached = true;
								break;
							}
							
							if (y instanceof RawVariableAssignmentNode) {
								y = ((RawVariableAssignmentNode)y).getExpression();
								continue;
							}
							else if (y instanceof RawVariableAssignmentExpression) {
								y = ((RawVariableAssignmentExpression)y).getExpression();
								continue;
							}
							else if (y instanceof FieldAssignmentNode) {
								y = ((FieldAssignmentNode)y).getExpression();
								continue;
							}
							else if (y instanceof FieldAssignmentExpression) {
								y = ((FieldAssignmentExpression)y).getExpression();
								continue;
							}
							else if (y instanceof ArrayAssignmentNode) {
								y = ((ArrayAssignmentNode)y).getValue();
								continue;
							}
							else if (y instanceof ArrayAssignmentExpression) {
								y = ((ArrayAssignmentExpression)y).getValue();
								continue;
							}
							
							break;
						}
						
						if (reached) {
							tail.set(root);
						}

						doBreak();
						return;
					}
				}.explore();
				
				if (tail.get() == null) {
					continue;
				}
				
				final MutableInteger status = new MutableInteger();
				final VariablesAnalyzer.VariableInformation info = information;
				final List<RawVariableLoadExpression> outlines = new ArrayList<RawVariableLoadExpression>();
				new NodeExplorer(code) {
					private boolean foundWrite = false;
					
					@Override
					public void onVisit(AbstractCodeNode n) {
						if (!foundWrite && n != tail.get())
							return;
						else if (!foundWrite && n == tail.get()) {
							foundWrite = true;
							return;
						}
						
						if ((n instanceof LabelAnnotation && importantUsesCount(((LabelAnnotation)n)) > 0)) {
							doBreak();
							return;
						}
						else if (info.getReads().contains(n)) {
							AbstractCodeNode transformed = null;
							AbstractCodeNode untransformed = tail.get();
							if (untransformed instanceof RawVariableAssignmentNode) {
								RawVariableAssignmentNode u = (RawVariableAssignmentNode)untransformed;
								transformed = new RawVariableAssignmentExpression(u.getVariableType(), u.getIndex(), u.getExpression());
								
								VariablesAnalyzer.VariableInformation i = analyzer.find(u.getIndex(), u);
								i.getWrites().remove(u);
								i.getWrites().add(transformed);
							}
							else if (untransformed instanceof FieldAssignmentNode) {
								FieldAssignmentNode u = (FieldAssignmentNode)untransformed;
								transformed = new FieldAssignmentExpression(u.getInstanceExpression(), u.getExpression(), u.getOwner(), u.getName(), u.getDescriptor());
							}
							else if (untransformed instanceof ArrayAssignmentNode) {
								ArrayAssignmentNode u = (ArrayAssignmentNode)untransformed;
								transformed = new ArrayAssignmentExpression(u.getStoreType(), u.getBase(), u.getIndex(), u.getValue());
							}
							else {
								throw new RuntimeException("WT");
							}
							
							int addr = code.addressOf(untransformed);
							code.setCodeAddress(addr);
							for (RawVariableLoadExpression outline : outlines) {
								final RawVariableLoadExpression outl = outline;
								final VariablesAnalyzer.VariableInformation infoout = analyzer.find(outl.getIndex(), outl);
								new NodeExplorer(transformed) {
									@Override
									public void onVisit(AbstractCodeNode n) {
										if (infoout.getWrites().contains(n)) {
											if (n instanceof RawVariableAssignmentExpression) {
												RawVariableAssignmentNode assgn;
												code.write(assgn = new RawVariableAssignmentNode(((RawVariableAssignmentExpression)n).getVariableType(),((RawVariableAssignmentExpression)n).getIndex(), ((RawVariableAssignmentExpression)n).getExpression()));
												getCurrent(getDepth()).overwrite(new RawVariableLoadExpression(((RawVariableAssignmentExpression)n).getVariableType(), ((RawVariableAssignmentExpression)n).getIndex()), getCurrentAddr(getDepth()));
												infoout.getWrites().remove(n);
												infoout.getReads().add(getCurrent(getDepth()).read(getCurrentAddr(getDepth())));
												infoout.getWrites().add(assgn);
											}
										}
									}
								}.explore();
							}
							
							info.getReads().remove(n);
							getCurrent(getDepth()).overwrite(transformed, getCurrentAddr(getDepth()));
							code.delete(code.addressOf(untransformed));
							status.set(1);
							

							
							
							doBreak();
							return;
							
						}
						else if (n.altersFlow() || tail.get().affectedBy(n) || n.affectedBy(tail.get())) {
							if (n instanceof RawVariableLoadExpression) {
								outlines.add((RawVariableLoadExpression)n);
							}
							else {
								doBreak();
								return;
							}
						}
					}
				}.explore();
				
				if (status.get() == 1) {
					total++;
					break; // might have modified
				}
			}
			
		}
		
		return total;
	}
	
	

	
	private int doRemoveUnusedLocalVariables() {
		int total = 0;
			
		List<VariablesAnalyzer.VariableInformation> variables = analyzer.getAll();
		main: for (VariablesAnalyzer.VariableInformation information : variables) {
			if (information.isArgument() || information.getWrites().size() < 1 || information.getReads().size() > 0)
				continue;
			if ((method.getParent().getOptions() & ClassNode.OPT_OPTIMIZATION_SKIP_NONSYNTH_LOCALS) != 0 && information.getIndex() < stackDumpsStartAddress)
				continue;
			for (AbstractCodeNode n : information.getWrites()) {
				if (!(n instanceof RawVariableAssignmentNode) && !(n instanceof RawVariableAssignmentExpression))
					continue main;
			}
			
			Iterator<AbstractCodeNode> it$ = information.getWrites().iterator();
			while (it$.hasNext()) {
				final AbstractCodeNode write = it$.next();
				final ExpressionNode expr = (ExpressionNode)write.read(0);
				
				new NodeExplorer(code) {
					@Override
					public void onVisit(AbstractCodeNode n) {
						if (n == write) {
							int depth = getDepth();
							if (depth > 1)
								getCurrent(depth).overwrite(expr, getCurrentAddr(depth));
							else
								getCurrent(depth).overwrite(new PopableNode(expr), getCurrentAddr(depth));
							doBreak();
						}
					}
				}.explore();
				
				it$.remove();
				total++;
			}
		}
		
		return total;
	}
	
	
	private int doReduceCasts() {
		final MutableInteger total = new MutableInteger();
		
		new NodeExplorer(code) {
			@Override
			public void onVisit(AbstractCodeNode n) {
				if (n instanceof CastExpression) {
					CastExpression c1 = (CastExpression)n;
					if (c1.getExpression() instanceof CastExpression) {
						CastExpression c2 = (CastExpression)c1.getExpression();
						if (Utilities.isPrimitive(c1.getType()) && c1.getType() == c2.getType()) {
							// reduce something like this doSomething((int)(int)someVar);
							c1.setExpression(c2.getExpression());
							total.inc();
						}
						else if (Utilities.isPrimitive(c1.getType()) && (c1.getType().getSort() > Type.BOOLEAN && c1.getType().getSort() < Type.INT) && c2.getType() == Type.INT_TYPE) {
							// reduce something like this doSomething((byte)(int)someVar);
							c1.setExpression(c2.getExpression());
							total.inc();
						}
					}
				}
				else if (n instanceof MathematicalExpression || n instanceof ConditionalJumpNode) {
					if (n instanceof MathematicalExpression && (((MathematicalExpression)n).getOperationType() == MathematicalExpression.TYPE_SHL || ((MathematicalExpression)n).getOperationType() == MathematicalExpression.TYPE_SHR))
						return;
					ExpressionNode left = (ExpressionNode)n.read(0);
					ExpressionNode right = (ExpressionNode)n.read(1);
					
					if (left instanceof CastExpression || right instanceof CastExpression) {
						CastExpression cast = left instanceof CastExpression ? (CastExpression)left : (CastExpression)right;
						ExpressionNode other = left == cast ? right : left;
						if ((cast.getType() == Type.DOUBLE_TYPE && other.getType() == Type.DOUBLE_TYPE) ||
							(cast.getType() == Type.FLOAT_TYPE && other.getType() == Type.FLOAT_TYPE) ||
							(cast.getType() == Type.LONG_TYPE && other.getType() == Type.LONG_TYPE)) {
							ExpressionNode unc = cast.getExpression();
							int priorityUncast = unc.getType() == Type.DOUBLE_TYPE ? 3 : (unc.getType() == Type.FLOAT_TYPE ? 2 : (unc.getType() == Type.LONG_TYPE ? 1 : 0));
							int priorityOther = other.getType() == Type.DOUBLE_TYPE ? 3 : (other.getType() == Type.FLOAT_TYPE ? 2 : (other.getType() == Type.LONG_TYPE ? 1 : 0));
							if (priorityOther > priorityUncast) {
								n.overwrite(unc, cast == left ? 0 : 1);
								total.inc();
							}
						}
					}
				}
				else if (n instanceof RawVariableAssignmentNode || n instanceof RawVariableAssignmentExpression) {
					ExpressionNode expr = (ExpressionNode)n.read(0);
					Type type = n instanceof RawVariableAssignmentNode ? ((RawVariableAssignmentNode)n).getVariableType() : ((RawVariableAssignmentExpression)n).getVariableType();
					if (Utilities.isPrimitive(type) && expr instanceof CastExpression) {
						if (type == expr.getType()) {
							n.overwrite(((CastExpression)expr).getExpression(), 0);
							total.inc();
						}
						else if (type == Type.INT_TYPE && (expr.getType().getSort() >= Type.BOOLEAN && expr.getType().getSort() <= Type.INT)) {
							n.overwrite(((CastExpression)expr).getExpression(), 0);
							if (n instanceof RawVariableAssignmentNode)
								((RawVariableAssignmentNode)n).setVariableType(expr.getType());
							else
								((RawVariableAssignmentExpression)n).setVariableType(expr.getType());
							total.inc();
						}
						
					}
				}
				else if (n instanceof FieldAssignmentNode || n instanceof FieldAssignmentExpression) {
					ExpressionNode expr = n instanceof FieldAssignmentNode ? ((FieldAssignmentNode)n).getExpression() : ((FieldAssignmentExpression)n).getExpression();
					Type type = n instanceof FieldAssignmentNode ? Type.getType(((FieldAssignmentNode)n).getDescriptor()) : Type.getType(((FieldAssignmentExpression)n).getDescriptor());
					if (Utilities.isPrimitive(type) && expr instanceof CastExpression && expr.getType() == type) {
						if (n instanceof FieldAssignmentNode)
							((FieldAssignmentNode)n).setExpression(((CastExpression)expr).getExpression());
						else
							((FieldAssignmentExpression)n).setExpression(((CastExpression)expr).getExpression());
						total.inc();
					}
				}
				else if (n instanceof ReturnNode) {
					ReturnNode rn = (ReturnNode)n;
					if (Utilities.isPrimitive(rn.getType()) && rn.getExpression() instanceof CastExpression && rn.getExpression().getType() == rn.getType()) {
						rn.setExpression((ExpressionNode)rn.getExpression().read(0));
						total.inc();
					}
				}
			}
		}.explore();
		
		return total.get();
	}
	
	
	private int doTrimCode() {
		int total = 0;
		
		for (int addr = 0; code.read(addr) != null;) {
			AbstractCodeNode n = code.read(addr);
			if (n.canTrim()) {
				deregisterFromAnalyzer(n);
				code.delete(addr);
				total++;
			}
			else {
				addr++;
			}
		}
		
		return total;
	}
	
	private void deregisterFromAnalyzer(AbstractCodeNode n) {
		for (int addr = 0; n.read(addr) != null; addr++)
			deregisterFromAnalyzer(n.read(addr));
		if (n instanceof RawVariableLoadExpression)
			analyzer.find(((RawVariableLoadExpression)n).getIndex(), n).getReads().remove(n);
		else if (n instanceof RawVariableAssignmentNode)
			analyzer.find(((RawVariableAssignmentNode)n).getIndex(), n).getWrites().remove(n);
		else if (n instanceof RawVariableAssignmentExpression)
			analyzer.find(((RawVariableAssignmentExpression)n).getIndex(), n).getWrites().remove(n);
	}
	
	private int doTransformNewObjects() {
		int total = 0;
		for (int addr = 0; code.read(addr) != null && code.read(addr + 1) != null; addr++) {
			AbstractCodeNode n1 = code.read(addr + 0);
			AbstractCodeNode n2 = code.read(addr + 1);
			
			if (n1 instanceof RawVariableAssignmentNode && n2 instanceof PopableNode) {
				RawVariableAssignmentNode assign = (RawVariableAssignmentNode)n1;
				PopableNode pop = (PopableNode)n2;
				
				if (assign.getExpression() instanceof NewUninitializedObjectExpression && pop.getExpression() instanceof InvokeExpression) {
					NewUninitializedObjectExpression newObj = (NewUninitializedObjectExpression)assign.getExpression();
					InvokeExpression invoke = (InvokeExpression)pop.getExpression();
					if (invoke.getName().equals("<init>") && invoke.getArguments()[0] instanceof RawVariableLoadExpression) {
						RawVariableLoadExpression loadExpr = (RawVariableLoadExpression)invoke.getArguments()[0];
						if (loadExpr.getIndex() == assign.getIndex()) {
							analyzer.find(loadExpr.getIndex(), loadExpr).getReads().remove(loadExpr);
							code.delete(addr + 1);
							ExpressionNode[] arguments = new ExpressionNode[invoke.getArguments().length - 1];
							for (int i = 0; i < arguments.length; i++)
								arguments[i] = invoke.getArguments()[i + 1];
							assign.setExpression(new NewObjectExpression(newObj.getObjectType(), arguments, invoke.getOwner(), invoke.getDescriptor()));
							total++;
						}
					}
				}
			}
		}
		
		for (int addr = 0; code.read(addr) != null; addr++) {
			AbstractCodeNode n1 = code.read(addr + 0);
			if (n1 instanceof PopableNode) {
				PopableNode pop = (PopableNode)n1;
				if (pop.getExpression() instanceof InvokeExpression) {
					InvokeExpression invoke = (InvokeExpression)pop.getExpression();
					if (invoke.getName().equals("<init>") && invoke.getArguments()[0] instanceof RawVariableAssignmentExpression) {
						RawVariableAssignmentExpression assign = (RawVariableAssignmentExpression)invoke.getArguments()[0];
						if (assign.getExpression() instanceof NewUninitializedObjectExpression) {
							NewUninitializedObjectExpression newObj = (NewUninitializedObjectExpression)assign.getExpression();
							ExpressionNode[] arguments = new ExpressionNode[invoke.getArguments().length - 1];
							for (int i = 0; i < arguments.length; i++)
								arguments[i] = invoke.getArguments()[i + 1];
							NewObjectExpression newObjExpr = new NewObjectExpression(newObj.getObjectType(), arguments, invoke.getOwner(), invoke.getDescriptor());
							RawVariableAssignmentNode assignNode = new RawVariableAssignmentNode(assign.getVariableType(), assign.getIndex(), newObjExpr);
							VariablesAnalyzer.VariableInformation info = analyzer.find(assign.getIndex(), assign);
							info.getWrites().remove(assign);
							info.getWrites().add(assignNode);
							code.overwrite(assignNode, code.addressOf(pop));
							total++;
						}
					}
				}
			}
		}
		return total;
	}
	


}
