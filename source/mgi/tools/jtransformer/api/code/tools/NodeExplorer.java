package mgi.tools.jtransformer.api.code.tools;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;

public abstract class NodeExplorer {

	/**
	 * Contains root node.
	 */
	private AbstractCodeNode root;
	/**
	 * Contains currently visited nodes.
	 */
	private AbstractCodeNode[] current;
	/**
	 * Current address of the child on visited nodes.
	 */
	private int[] currentAddr;
	/**
	 * Contains current depth.
	 */
	private int depth;
	/**
	 * Whether chain was broken.
	 */
	private boolean broken;
	
	public NodeExplorer(AbstractCodeNode root) {
		this.root = root;
		this.current = new AbstractCodeNode[2];
		this.currentAddr = new int[2];
	}
	
	public void explore() {
		explore(0);
	}
	
	public void explore(int startAddr) {
		try {
			enterNode(root, startAddr);
		}
		catch (RuntimeException t) {
			if (t.getMessage() == null || !t.getMessage().equals("break-chain"))
				throw t;
			depth = 0;
			broken = false;
		}
	}
	
	private void enterNode(AbstractCodeNode n, int startAddr) {
		if ((depth + 1) >= current.length)
			expand();
		depth++;
		current[depth - 1] = n;
		for (int addr = startAddr; n.read(addr) != null; addr++) {
			currentAddr[depth - 1] = addr;
			AbstractCodeNode node = n.read(addr);
			enterNode(node, 0);
			onVisit(node);
			if (broken)
				throw new RuntimeException("break-chain");
		}
		current[depth - 1] = null;
		currentAddr[depth - 1] = 0;
		depth--;
	}
	
	private void expand() {
		AbstractCodeNode[] current = new AbstractCodeNode[this.current.length * 2];
		int[] currentAddr = new int[this.currentAddr.length * 2];
		System.arraycopy(this.current, 0, current, 0, this.current.length);
		System.arraycopy(this.currentAddr, 0, currentAddr, 0, this.currentAddr.length);
		this.current = current;
		this.currentAddr = currentAddr;
	}
	
	public abstract void onVisit(AbstractCodeNode n);

	public AbstractCodeNode getRoot() {
		return root;
	}
	
	public AbstractCodeNode getCurrent(int depth) {
		return current[depth - 1];
	}
	
	public int getCurrentAddr(int depth) {
		return currentAddr[depth - 1];
	}

	public int getDepth() {
		return depth;
	}
	
	public void doBreak() {
		broken = true;
	}




}
