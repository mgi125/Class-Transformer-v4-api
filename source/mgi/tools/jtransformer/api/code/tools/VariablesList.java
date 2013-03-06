package mgi.tools.jtransformer.api.code.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.LocalVariableDeclaration;
import mgi.tools.jtransformer.api.code.memory.VariableLoadExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignmentNode;
import mgi.tools.jtransformer.api.code.memory.VariableAssignmentExpression;

public class VariablesList {
	/**
	 * Contains information collected.
	 */
	private Map<Integer, List<VariableInformation>> information;
	
	public VariablesList() {
		this.information = new HashMap<Integer, List<VariableInformation>>();
	}
	
	
	/**
	 * Generate's declaration's information by reading
	 * given node and it's childs.
	 */
	public void generate(AbstractCodeNode n) {
		information.clear();
		for (int addr = 0; n.read(addr) != null; addr++)
			generate(n.read(addr));
		
		LocalVariableDeclaration dec = null;
		boolean read = false;
		if (n instanceof VariableLoadExpression) {
			dec = ((VariableLoadExpression)n).getDeclaration();
			read = true;
		}
		else if (n instanceof VariableAssignmentNode) {
			dec = ((VariableAssignmentNode)n).getDeclaration();
		}
		else if (n instanceof VariableAssignmentExpression) {
			dec = ((VariableAssignmentExpression)n).getDeclaration();
		}
		
		if (dec != null) {
			List<VariableInformation> infos = find(dec.getIndex());
			VariableInformation info = null;
			if (infos != null) {
				for (VariableInformation i : infos)
					if (i.getDeclaration() == dec)
						info = i;
			}
			
			if (info != null)
				(read ? info.getReads() : info.getWrites()).add(n);
			else {
				info = new VariableInformation(dec);
				(read ? info.getReads() : info.getWrites()).add(n);
				
				if (infos != null)
					infos.add(info);
				else {
					List<VariableInformation> l = new ArrayList<VariableInformation>();
					l.add(info);
					information.put(dec.getIndex(), l);
				}
			}
		
		}
	}

	
	/**
	 * Find's all variable's with specified index.
	 */
	public List<VariableInformation> find(int index) {
		if (!information.containsKey(index))
			return null;
		return information.get(index);
	}
	
	/**
	 * Find's variable by index and node that does read or write
	 * to it.
	 */
	public VariableInformation find(int index, AbstractCodeNode n) {
		if (!information.containsKey(index))
			return null;
		for (VariableInformation info : information.get(index)) {
			if (info.getReads().contains(n) || info.getWrites().contains(n))
				return info;
		}
		return null;
	}
	
	/**
	 * Return's new list containing all variable's that can be found in code.
	 */
	public List<VariableInformation> getAll() {
		List<VariableInformation> all = new ArrayList<VariableInformation>();
		for (List<VariableInformation> vars : information.values())
			all.addAll(vars);
		return all;
	}
	
	
	public static class VariableInformation {
		/**
		 * Contains variable declaration.
		 */
		private LocalVariableDeclaration declaration;
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
		
		public VariableInformation(LocalVariableDeclaration declaration) {
			this.declaration = declaration;
			this.reads = new ArrayList<AbstractCodeNode>();
			this.writes = new ArrayList<AbstractCodeNode>();
		}

		public LocalVariableDeclaration getDeclaration() {
			return declaration;
		}

		public void setDeclaration(LocalVariableDeclaration declaration) {
			this.declaration = declaration;
		}


		public List<AbstractCodeNode> getReads() {
			return reads;
		}

		public List<AbstractCodeNode> getWrites() {
			return writes;
		}


	}

}
