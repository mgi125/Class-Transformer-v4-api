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
import mgi.tools.jtransformer.api.code.flow.SwitchNode;
import mgi.tools.jtransformer.api.code.flow.UnconditionalJumpNode;
import mgi.tools.jtransformer.api.code.general.CastExpression;
import mgi.tools.jtransformer.api.code.general.InvokeExpression;
import mgi.tools.jtransformer.api.code.general.PopableNode;
import mgi.tools.jtransformer.api.code.math.MathematicalExpression;
import mgi.tools.jtransformer.api.code.math.MathematicalVariableAssignationExpression;
import mgi.tools.jtransformer.api.code.math.MathematicalVariableAssignationNode;
import mgi.tools.jtransformer.api.code.memory.ArrayAssignationExpression;
import mgi.tools.jtransformer.api.code.memory.ArrayAssignationNode;
import mgi.tools.jtransformer.api.code.memory.FieldAssignationExpression;
import mgi.tools.jtransformer.api.code.memory.FieldAssignationNode;
import mgi.tools.jtransformer.api.code.memory.NewObjectExpression;
import mgi.tools.jtransformer.api.code.memory.NewUninitializedObjectExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignationExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignationNode;
import mgi.tools.jtransformer.api.code.memory.VariableLoadExpression;
import mgi.tools.jtransformer.api.code.tools.Macros;
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
				if (!(x instanceof VariableAssignationNode) && !(x instanceof VariableAssignationExpression))
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
							
							if (y instanceof VariableAssignationNode) {
								y = ((VariableAssignationNode)y).getExpression();
								continue;
							}
							else if (y instanceof VariableAssignationExpression) {
								y = ((VariableAssignationExpression)y).getExpression();
								continue;
							}
							else if (y instanceof FieldAssignationNode) {
								y = ((FieldAssignationNode)y).getExpression();
								continue;
							}
							else if (y instanceof FieldAssignationExpression) {
								y = ((FieldAssignationExpression)y).getExpression();
								continue;
							}
							else if (y instanceof ArrayAssignationNode) {
								y = ((ArrayAssignationNode)y).getValue();
								continue;
							}
							else if (y instanceof ArrayAssignationExpression) {
								y = ((ArrayAssignationExpression)y).getValue();
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
				final List<VariableLoadExpression> outlines = new ArrayList<VariableLoadExpression>();
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
							if (untransformed instanceof VariableAssignationNode) {
								VariableAssignationNode u = (VariableAssignationNode)untransformed;
								transformed = new VariableAssignationExpression(u.getVariableType(), u.getIndex(), u.getExpression());
								
								VariablesAnalyzer.VariableInformation i = analyzer.find(u.getIndex(), u);
								i.getWrites().remove(u);
								i.getWrites().add(transformed);
							}
							else if (untransformed instanceof FieldAssignationNode) {
								FieldAssignationNode u = (FieldAssignationNode)untransformed;
								transformed = new FieldAssignationExpression(u.getInstanceExpression(), u.getExpression(), u.getOwner(), u.getName(), u.getDescriptor());
							}
							else if (untransformed instanceof ArrayAssignationNode) {
								ArrayAssignationNode u = (ArrayAssignationNode)untransformed;
								transformed = new ArrayAssignationExpression(u.getStoreType(), u.getBase(), u.getIndex(), u.getValue());
							}
							else {
								throw new RuntimeException("WT");
							}
							
							int addr = code.addressOf(untransformed);
							code.setCodeAddress(addr);
							for (VariableLoadExpression outline : outlines) {
								final VariableLoadExpression outl = outline;
								final VariablesAnalyzer.VariableInformation infoout = analyzer.find(outl.getIndex(), outl);
								new NodeExplorer(transformed) {
									@Override
									public void onVisit(AbstractCodeNode n) {
										if (infoout.getWrites().contains(n)) {
											if (n instanceof VariableAssignationExpression) {
												VariableAssignationNode assgn;
												code.write(assgn = new VariableAssignationNode(((VariableAssignationExpression)n).getVariableType(),((VariableAssignationExpression)n).getIndex(), ((VariableAssignationExpression)n).getExpression()));
												getCurrent(getDepth()).overwrite(new VariableLoadExpression(((VariableAssignationExpression)n).getVariableType(), ((VariableAssignationExpression)n).getIndex()), getCurrentAddr(getDepth()));
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
							if (n instanceof VariableLoadExpression) {
								outlines.add((VariableLoadExpression)n);
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
				if (!(n instanceof VariableAssignationNode) && !(n instanceof VariableAssignationExpression))
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
	

	private int doTransformLocalVariablesAssign() {	
		final MutableInteger status = new MutableInteger();
		new NodeExplorer(code) {
			@Override
			public void onVisit(AbstractCodeNode n) {
				if (n instanceof VariableAssignationNode || n instanceof VariableAssignationExpression) {
					AbstractCodeNode assign = n;
					ExpressionNode expression = null;
					int index = -1;
					if (assign instanceof VariableAssignationNode) {
						expression = ((VariableAssignationNode)assign).getExpression();
						index = ((VariableAssignationNode)assign).getIndex();
					}
					else if (assign instanceof VariableAssignationExpression) {
						expression = ((VariableAssignationExpression)assign).getExpression();
						index = ((VariableAssignationExpression)assign).getIndex();
					}
					if (!Utilities.isPrimitive(expression.getType()))
						return;
					Type castType = null;
					if (expression instanceof CastExpression) {
						castType = ((CastExpression)expression).getNewType();
						expression = ((CastExpression)expression).getExpression();
					}
					Type type = castType != null ? castType : expression.getType();
					
					if (expression instanceof MathematicalExpression) {
						MathematicalExpression mathExpr = (MathematicalExpression)expression;
						if (mathExpr.getLeft() instanceof VariableLoadExpression) {
							VariableLoadExpression load = (VariableLoadExpression)mathExpr.getLeft();
							if (load.getIndex() == index) {
								VariablesAnalyzer.VariableInformation iWrite = analyzer.find(index, assign);
								VariablesAnalyzer.VariableInformation iRead = analyzer.find(load.getIndex(), load);
								VariablesAnalyzer.VariableInformation info = iWrite != iRead ? analyzer.mergeExternal(iWrite, iRead) : iWrite;
								info.getWrites().remove(assign);
								info.getReads().remove(load);
								
								if (assign instanceof VariableAssignationNode) {
									MathematicalVariableAssignationNode assignNode = new MathematicalVariableAssignationNode(type, mathExpr.getOperationType(), index, mathExpr.getRight());
									info.getWrites().add(assignNode);
									getCurrent(getDepth()).overwrite(assignNode, getCurrentAddr(getDepth()));
								}
								else if (assign instanceof VariableAssignationExpression) {
									MathematicalVariableAssignationExpression assignExpr = new MathematicalVariableAssignationExpression(type, mathExpr.getOperationType(), index, mathExpr.getRight());
									info.getWrites().add(assignExpr);
									getCurrent(getDepth()).overwrite(assignExpr, getCurrentAddr(getDepth()));
								}
							}
						}
					}
				}
			}	
		}.explore();
		
		
		return status.get();
	}
	
	private int doReduceCasts() {
		final MutableInteger total = new MutableInteger();
		
		new NodeExplorer(code) {
			@Override
			public void onVisit(AbstractCodeNode n) {
				if (n instanceof CastExpression) {	
					CastExpression cst = (CastExpression)n;
					if ((cst.getNewType() == Type.INT_TYPE || cst.getNewType() == Type.LONG_TYPE || cst.getNewType() == Type.FLOAT_TYPE || cst.getNewType() == Type.DOUBLE_TYPE) 
							&& cst.getType() == cst.getExpression().getType()) {
						// cast has no effect so remove it.
						getCurrent(getDepth()).overwrite(cst.getExpression(), getCurrentAddr(getDepth()));
						total.inc();
						doBreak();
						return;
					}
					else if (cst.getExpression() instanceof CastExpression) {
						CastExpression cst2 = (CastExpression)cst.getExpression();
						if (cst.getNewType() == cst2.getNewType()) {
							// reduce two casts to same type into one cast.
							cst.setExpression(cst2.getExpression());
							total.inc();
							doBreak();
							return;
						}
						else if (cst2.getNewType() == Type.INT_TYPE && (cst.getNewType() == Type.BYTE_TYPE || cst.getNewType() == Type.SHORT_TYPE || cst2.getNewType() == Type.CHAR_TYPE)) {
							// reduce as the int is unnecessary.
							cst.setExpression(cst2.getExpression());
							total.inc();
							doBreak();
							return;
						}
					}
				}
				else if (n instanceof MathematicalExpression) {
					// reduce some casts by rules of auto cast.
					MathematicalExpression mExpr = (MathematicalExpression)n;
					CastExpression left = mExpr.getLeft() instanceof CastExpression ? (CastExpression)mExpr.getLeft() : null;
					CastExpression right = mExpr.getRight() instanceof CastExpression ? (CastExpression)mExpr.getRight() : null;
					
					if (left != null && right != null) {
						right = null;
					}
					
					if ((left != null && right == null) || (left == null && right != null)) {
						CastExpression cast = left != null ? left : right;
						ExpressionNode other = left != null ? mExpr.getRight() : mExpr.getLeft();
						if ((other.getType() == Type.DOUBLE_TYPE && cast.getNewType() == Type.DOUBLE_TYPE) || 
							(other.getType() == Type.FLOAT_TYPE && cast.getNewType() == Type.FLOAT_TYPE) ||
							(other.getType() == Type.LONG_TYPE && cast.getNewType() == Type.LONG_TYPE) ||
							(other.getType() == Type.INT_TYPE && cast.getNewType() == Type.INT_TYPE)) {
							ExpressionNode to = cast.getExpression();
							int priority = other.getType() == Type.DOUBLE_TYPE ? 3 : (other.getType() == Type.FLOAT_TYPE ? 2 : (other.getType() == Type.LONG_TYPE ? 1 : 0));
							int nPriority = to.getType() == Type.DOUBLE_TYPE ? 3 : (to.getType() == Type.FLOAT_TYPE ? 2 : (to.getType() == Type.LONG_TYPE ? 1 : 0));
							if (nPriority <= priority) {
								if (left != null)
									mExpr.setLeft(to);
								else
									mExpr.setRight(to);
								total.inc();
							}
						}
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
		if (n instanceof VariableLoadExpression)
			analyzer.find(((VariableLoadExpression)n).getIndex(), n).getReads().remove(n);
		else if (n instanceof VariableAssignationNode)
			analyzer.find(((VariableAssignationNode)n).getIndex(), n).getWrites().remove(n);
		else if (n instanceof VariableAssignationExpression)
			analyzer.find(((VariableAssignationExpression)n).getIndex(), n).getWrites().remove(n);
		else if (n instanceof MathematicalVariableAssignationNode)
			analyzer.find(((MathematicalVariableAssignationNode)n).getIndex(), n).getWrites().remove(n);
		else if (n instanceof MathematicalVariableAssignationExpression)
			analyzer.find(((MathematicalVariableAssignationExpression)n).getIndex(), n).getWrites().remove(n);
	}
	
	private int doTransformNewObjects() {
		int total = 0;
		for (int addr = 0; code.read(addr) != null && code.read(addr + 1) != null; addr++) {
			AbstractCodeNode n1 = code.read(addr + 0);
			AbstractCodeNode n2 = code.read(addr + 1);
			
			if (n1 instanceof VariableAssignationNode && n2 instanceof PopableNode) {
				VariableAssignationNode assign = (VariableAssignationNode)n1;
				PopableNode pop = (PopableNode)n2;
				
				if (assign.getExpression() instanceof NewUninitializedObjectExpression && pop.getExpression() instanceof InvokeExpression) {
					NewUninitializedObjectExpression newObj = (NewUninitializedObjectExpression)assign.getExpression();
					InvokeExpression invoke = (InvokeExpression)pop.getExpression();
					if (invoke.getName().equals("<init>") && invoke.getArguments()[0] instanceof VariableLoadExpression) {
						VariableLoadExpression loadExpr = (VariableLoadExpression)invoke.getArguments()[0];
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
					if (invoke.getName().equals("<init>") && invoke.getArguments()[0] instanceof VariableAssignationExpression) {
						VariableAssignationExpression assign = (VariableAssignationExpression)invoke.getArguments()[0];
						if (assign.getExpression() instanceof NewUninitializedObjectExpression) {
							NewUninitializedObjectExpression newObj = (NewUninitializedObjectExpression)assign.getExpression();
							ExpressionNode[] arguments = new ExpressionNode[invoke.getArguments().length - 1];
							for (int i = 0; i < arguments.length; i++)
								arguments[i] = invoke.getArguments()[i + 1];
							NewObjectExpression newObjExpr = new NewObjectExpression(newObj.getObjectType(), arguments, invoke.getOwner(), invoke.getDescriptor());
							VariableAssignationNode assignNode = new VariableAssignationNode(assign.getVariableType(), assign.getIndex(), newObjExpr);
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
