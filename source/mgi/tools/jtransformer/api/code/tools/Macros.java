package mgi.tools.jtransformer.api.code.tools;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;

public class Macros {

	public static boolean nodeContains(AbstractCodeNode n, List<AbstractCodeNode> childs) {
		for (int addr = 0; n.read(addr) != null; addr++)
			if (nodeContains(n.read(addr), childs))
				return true;
		return false;
	}
	
	
	public static AbstractCodeNode[] getExecutionOrder(AbstractCodeNode n) {
		List<AbstractCodeNode> order = new ArrayList<AbstractCodeNode>();
		fillInExecutionOrder(n, order);
		return order.toArray(new AbstractCodeNode[0]);
	}
	
	
	private static void fillInExecutionOrder(AbstractCodeNode n, List<AbstractCodeNode> order) {
		for (int addr = 0; n.read(addr) != null; addr++)
			fillInExecutionOrder(n.read(addr), order);
		order.add(n);
	}
}
