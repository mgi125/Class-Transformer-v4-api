package mgi.tools.jtransformer.api.code.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;


import mgi.tools.jtransformer.api.MethodNode;
import mgi.tools.jtransformer.api.TypesLoader;
import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodeNode;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.LocalVariableDeclaration;
import mgi.tools.jtransformer.api.code.TryCatchInformation;
import mgi.tools.jtransformer.api.code.flow.ConditionalJumpNode;
import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;
import mgi.tools.jtransformer.api.code.flow.ReturnNode;
import mgi.tools.jtransformer.api.code.flow.SwitchNode;
import mgi.tools.jtransformer.api.code.flow.ThrowNode;
import mgi.tools.jtransformer.api.code.flow.UnconditionalJumpNode;
import mgi.tools.jtransformer.api.code.memory.RawVariableAssignmentExpression;
import mgi.tools.jtransformer.api.code.memory.RawVariableAssignmentNode;
import mgi.tools.jtransformer.api.code.memory.RawVariableLoadExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignmentExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignmentNode;
import mgi.tools.jtransformer.api.code.memory.VariableLoadExpression;
import mgi.tools.jtransformer.api.code.tools.NodeExplorer;
import mgi.tools.jtransformer.utilities.ArrayQueue;

/**
 * Variable's analyzer that analyze's & updates variable types on
 * their raw read/write nodes/expressions.
 */
public class LocalVariablesTypesAnalyzer {

	/**
	 * Contains method.
	 */
	private MethodNode method;
	/**
	 * Contains code.
	 */
	private CodeNode code;
	/**
	 * Contains types loader.
	 */
	private TypesLoader typesLoader;
	/**
	 * Contains information collected.
	 */
	private Map<Integer, List<VariableInformation>> information;
	
	
	/**
	 * Contains all visitors.
	 */
	private Visitor[] visitors;
	/**
	 * Contains queue.
	 */
	private ArrayQueue<Visitor> queue;
	
	public LocalVariablesTypesAnalyzer(MethodNode method, CodeNode code) {
		this.method = method;
		this.code = code;
		this.typesLoader = method.getParent().getAPI().getTypesLoader();
		this.information = new HashMap<Integer, List<VariableInformation>>();
		int amountBlocks = 0;
		for (int addr = 0; code.read(addr) != null; addr++)
			if (code.read(addr) instanceof LabelAnnotation)
				amountBlocks++;
		this.visitors = new Visitor[amountBlocks];
		this.queue = new ArrayQueue<Visitor>(code.size());
	}
	
	public void analyze() {
		Visitor v = null;
		queue.insert(v = visitors[0] = new Visitor((LabelAnnotation) code.read(0)));
		
		int index = 0;
		if ((method.getAccessor() & Opcodes.ACC_STATIC) == 0) {
			VariableInformation info = new VariableInformation(index++, Type.getType("L" + method.getParent().getName() + ";"), true);
			add(info);
			v.addEntry(info);
		}
		
		Type[] arguments = Type.getArgumentTypes(method.getDescriptor());
		for (int i = 0; i < arguments.length; i++) {
			VariableInformation info = new VariableInformation(index, arguments[i], true);
			add(info);
			v.addEntry(info);
			index += arguments[i].getSize();
		}
		
		while (queue.size() > 0) {
			Visitor visitor = queue.take();
			visit(visitor);
		}
	}
	
	public void createDeclarations() {
		final Map<AbstractCodeNode, AbstractCodeNode> replaceTable = new HashMap<AbstractCodeNode, AbstractCodeNode>();
		for (List<VariableInformation> vars : information.values()) {
			for (VariableInformation var : vars) {
				LocalVariableDeclaration declaration = new LocalVariableDeclaration(var.getType(), var.getIndex(), var.isArgument());
				for (AbstractCodeNode read : var.getReads())
					replaceTable.put(read, new VariableLoadExpression(declaration));
				for (AbstractCodeNode write : var.getWrites()) {
					if (write instanceof VariableAssignmentExpression)
						replaceTable.put(write, new VariableAssignmentExpression(declaration, (ExpressionNode)write.read(0)));
					else
						replaceTable.put(write, new VariableAssignmentNode(declaration, (ExpressionNode)write.read(0)));
				}
			}
		}
		
		new NodeExplorer(code) {
			@Override
			public void onVisit(AbstractCodeNode n) {
				AbstractCodeNode replacement = replaceTable.get(n);
				if (replacement != null) {
					getCurrent(getDepth()).overwrite(replacement, getCurrentAddr(getDepth()));
				}
			}
		}.explore();
	}
	
	
	/**
	 * Visit's code using specified visitor.
	 */
	private void visit(Visitor visitor) {
		visitor.buildExit();
		
		// TODO optimize trycaches stuff
		for (TryCatchInformation info : code.getTryCatches()) {
			int imin = visitorId(info.getStart());
			int imax = visitorId(info.getEnd());
			int ithis = visitorId(visitor.getStart());
			if (ithis < imin || ithis >= imax)
				continue;
			visitJump(visitor, info.getHandler());
		}
		// ----------------------------
		
		int addr = code.addressOf(visitor.getStart()) + 1;
		for (; code.read(addr) != null; addr++) { 
			AbstractCodeNode n = code.read(addr);
			exploreNode(visitor, n);
			if (n instanceof LabelAnnotation) {
				visitJump(visitor, (LabelAnnotation)n);
				break;
			}
			else if (n instanceof UnconditionalJumpNode) {
				visitJump(visitor, ((UnconditionalJumpNode)n).getTarget());
				break;
			}
			else if (n instanceof ConditionalJumpNode) {
				visitJump(visitor, ((ConditionalJumpNode)n).getTarget());
				// don't break, as this is two way route
			}
			else if (n instanceof SwitchNode) {
				SwitchNode sw = (SwitchNode)n;
				for (int i = 0; i < sw.getTargets().length; i++)
					visitJump(visitor, sw.getTargets()[i]);
				visitJump(visitor, sw.getDefault());
				break;
			}
			else if (n instanceof ReturnNode || n instanceof ThrowNode) {
				break;
			}
			// TODO subroutines
		}
	}
	
	private void exploreNode(Visitor visitor, AbstractCodeNode n) {
		for (int addr = 0; n.read(addr) != null; addr++)
			exploreNode(visitor, n.read(addr));
		
		if (n instanceof RawVariableAssignmentNode) {
			RawVariableAssignmentNode vn = (RawVariableAssignmentNode)n;
			VariableInformation info = create(vn.getIndex(), vn.getExpression().getType(), vn);
			vn.setVariableType(info.getType());
			if (!info.getWrites().contains(vn))
				info.getWrites().add(vn);
			visitor.set(info);
		}
		else if (n instanceof RawVariableAssignmentExpression) {
			RawVariableAssignmentExpression vn = (RawVariableAssignmentExpression)n;
			VariableInformation info = create(vn.getIndex(), vn.getExpression().getType(), vn);
			vn.setVariableType(info.getType());
			if (!info.getWrites().contains(vn))
				info.getWrites().add(vn);
			visitor.set(info);
		}
		else if (n instanceof RawVariableLoadExpression) {
			RawVariableLoadExpression vl = (RawVariableLoadExpression)n;
			visitor.merge(vl.getIndex());
			VariableInformation info = visitor.getSingle(vl.getIndex());
			vl.setVariableType(vl.getType());
			if (info == null) {
				throw new RuntimeException("WT");
			}
			if (!info.getReads().contains(vl))
				info.getReads().add(vl);
		}
	}
	
	
	/**
	 * Return's visitor that will visit (or has already visited) given jump.
	 */
	private Visitor visitJump(Visitor parent, LabelAnnotation target) {
		int id = visitorId(target);
		if (visitors[id] == null) {
			Visitor visitor = (visitors[id] = new Visitor(target, parent));
			queue.insert(visitor);
			return visitor;
		}
		else {
			Visitor v = visitors[id];
			int max = Math.max(parent.exitVariables.length, v.entryVariables.length);
			parent.expand(max);
			v.expand(max);
			
			boolean needsRevisit = false;
			for (int i = 0; i < parent.exitVariables.length; i++) {
				if (parent.exitVariables[i] == null)
					continue;
				else if (v.entryVariables[i] == null) {
					v.entryVariables[i] = new ArrayList<VariableInformation>(parent.exitVariables[i]);
					needsRevisit |= v.entryVariables[i].size() > 0;
				}
				else {
					for (VariableInformation info : parent.exitVariables[i]) {
						if (!v.entryVariables[i].contains(info)) {
							v.entryVariables[i].add(info);
							needsRevisit |= true;
						}
					}
				}
			}
			
			if (needsRevisit && !queue.lookup(v)) {
				queue.insert(v);
			}
			
			return v;
		}
	}
	
	/**
	 * Get's information for specific index and parent,
	 * if there's no information yet , it creates new information.
	 */
	private VariableInformation create(int index, Type type, AbstractCodeNode parent) {
		VariableInformation info = find(index, parent);
		if (info != null)
			return info;
		info = new VariableInformation(index, type, false);
		info.getWrites().add(parent);
		add(info);
		return info;
	}
	
	
	/**
	 * Find's visitor id for given label.
	 */
	private int visitorId(LabelAnnotation label) {
		int total = 0;
		for (int addr = 0; code.read(addr) != null; addr++) {
			if (code.read(addr) instanceof LabelAnnotation) {
				if (code.read(addr) == label)
					return total;
				total++;
			}
		}
		return -1;
	}
	
	
	/**
	 * Merge's two variables into one.
	 */
	private VariableInformation merge(VariableInformation i1, VariableInformation i2) {
		if (i1.getIndex() != i2.getIndex())
			throw new RuntimeException("WT");
		VariableInformation info = new VariableInformation(i1.getIndex(), typesLoader.getCommonType(i1.getType(), i2.getType()), i1.isArgument() || i2.isArgument());
		info.getReads().addAll(i1.getReads());
		info.getReads().addAll(i2.getReads());
		info.getWrites().addAll(i1.getWrites());
		info.getWrites().addAll(i2.getWrites());
		
		List<VariableInformation> vars = information.get(i1.getIndex());
		vars.remove(i1);
		vars.remove(i2);
		vars.add(info);
		
		for (int i = 0; i < visitors.length; i++) {
			// merge information in visitors.
			Visitor v = visitors[i];
			if (v == null)
				continue;
			
			for (int x = 0; x < 2; x++) {
				List<VariableInformation>[] variables = x == 0 ? v.entryVariables : v.exitVariables;
				for (int vv = 0; vv < variables.length; vv++) {
					List<VariableInformation> list = variables[vv];
					if (list == null)
						continue;
					
					if (list.contains(i1) || list.contains(i2)) {
						list.remove(i1);
						list.remove(i2);
						
						list.add(info);
					}
				}
			}
		}
		
		return info;
	}
	
	/**
	 * Add's given variable information.
	 */
	private void add(VariableInformation info) {
		if (!information.containsKey(info.getIndex())) {
			information.put(info.getIndex(), new ArrayList<VariableInformation>());
		}
		
		List<VariableInformation> variables = information.get(info.getIndex());
		if (!variables.contains(info)) {
			variables.add(info);
		}
	}
	
	/**
	 * Find's variable by index and node that does read or write
	 * to it.
	 */
	private VariableInformation find(int index, AbstractCodeNode n) {
		if (!information.containsKey(index))
			return null;
		for (VariableInformation info : information.get(index)) {
			if (info.getReads().contains(n) || info.getWrites().contains(n))
				return info;
		}
		return null;
	}
	
	public class Visitor {
		/**
		 * Contains start position.
		 */
		private LabelAnnotation start;
		/**
		 * Contains variables information by index at the start.
		 */
		private List<VariableInformation>[] entryVariables;
		/**
		 * Contains variables information by index at the end.
		 */
		private List<VariableInformation>[] exitVariables;
		
		@SuppressWarnings("unchecked")
		public Visitor(LabelAnnotation start) {
			this.start = start;
			this.entryVariables = (List<VariableInformation>[]) new List<?>[2];
			buildExit();
		}
		
		@SuppressWarnings("unchecked")
		public Visitor(LabelAnnotation start, Visitor parent) {
			this.start = start;
			this.entryVariables = (List<VariableInformation>[]) new List<?>[parent.exitVariables.length];
			for (int i = 0; i < parent.exitVariables.length; i++)
				if (parent.exitVariables[i] != null)
					this.entryVariables[i] = new ArrayList<VariableInformation>(parent.exitVariables[i]);
			buildExit();
		}
		
		public void merge(int index) {
			expand(index + 1);
			if (exitVariables[index] == null || exitVariables[index].size() < 2)
				return;
			VariableInformation[] information = exitVariables[index].toArray(new VariableInformation[0]);
			for (int i = 1; i < information.length; i++)
				information[0] = LocalVariablesTypesAnalyzer.this.merge(information[0], information[i]);
		}
		
		@SuppressWarnings("unchecked")
		public void buildExit() {
			exitVariables = (List<VariableInformation>[]) new List<?>[entryVariables.length];
			for (int i = 0; i < entryVariables.length; i++)
				if (entryVariables[i] != null)
					exitVariables[i] = new ArrayList<VariableInformation>(entryVariables[i]);
		}
		
		public void addEntry(VariableInformation info) {
			expand(info.index + 1);
			if (entryVariables[info.index] == null)
				entryVariables[info.index] = new ArrayList<VariableInformation>();
			if (!entryVariables[info.index].contains(info))
				entryVariables[info.index].add(info);
		}	
		
		public VariableInformation getSingle(int index) {
			expand(index + 1);
			if (exitVariables[index] == null || exitVariables[index].size() != 1)
				return null;
			for (VariableInformation info : exitVariables[index])
				return info;
			return null;
		}
		
		public List<VariableInformation> get(int index) {
			expand(index + 1);
			return exitVariables[index];
		}
		
		public void set(VariableInformation info) {
			expand(info.index + 1);
			if (exitVariables[info.index] == null)
				exitVariables[info.index] = new ArrayList<VariableInformation>();
			exitVariables[info.index].clear();
			exitVariables[info.index].add(info);
		}
		
		@SuppressWarnings("unchecked")
		private void expand(int minRequired) {
			int required = Math.min(entryVariables.length, exitVariables.length);
			while (minRequired > required)
				required *= 2;
			if (entryVariables.length != required) {
				List<?>[] variables = new List<?>[required];
				System.arraycopy(this.entryVariables, 0, variables, 0, this.entryVariables.length);
				this.entryVariables = (List<VariableInformation>[]) variables;
			}
			if (exitVariables.length != required) {
				List<?>[] variables = new List<?>[required];
				System.arraycopy(this.exitVariables, 0, variables, 0, this.exitVariables.length);
				this.exitVariables = (List<VariableInformation>[]) variables;
			}
		}

		public LabelAnnotation getStart() {
			return start;
		}
	}
	
	public static class VariableInformation {
		/**
		 * Index of the variable.
		 */
		private int index;
		/**
		 * Type of the variable.
		 */
		private Type type;
		/**
		 * Whether variable is argument.
		 */
		private boolean argument;
		/**
		 * Contains list of all nodes that reads 
		 * this variable.
		 */
		private List<AbstractCodeNode> reads;
		/**
		 * Contains list of all nodes that writes
		 * to the variable.
		 */
		private List<AbstractCodeNode> writes;
		
		public VariableInformation(int index, Type type, boolean isArgument) {
			this.index = index;
			this.type = type;
			this.argument = isArgument;
			this.reads = new ArrayList<AbstractCodeNode>();
			this.writes = new ArrayList<AbstractCodeNode>();
		}

		public int getIndex() {
			return index;
		}
		
		public Type getType() {
			return type;
		}
		
		public boolean isArgument() {
			return argument;
		}

		public List<AbstractCodeNode> getReads() {
			return reads;
		}

		public List<AbstractCodeNode> getWrites() {
			return writes;
		}

	}
	
}
