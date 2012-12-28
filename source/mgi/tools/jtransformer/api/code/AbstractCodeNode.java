package mgi.tools.jtransformer.api.code;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

public abstract class AbstractCodeNode {

	private static final int INITIAL_BUFFER_SIZE = 4;
	private AbstractCodeNode[] childs;
	private int codeAddress;
	
	/**
	 * Whether this node can be trimmed.
	 */
	public abstract boolean canTrim();
	/**
	 * Whether this node alters code logic.
	 */
	public abstract boolean altersLogic();
	/**
	 * Whether this node alters code flow.
	 */
	public abstract boolean altersFlow();
	/**
	 * Whether this node execution result
	 * is affected by given node.
	 */
	public abstract boolean affectedBy(AbstractCodeNode n);
	/**
	 * Print's this node on given printer.
	 */
	public abstract void print(CodePrinter printer);
	/**
	 * Write's bytecode for this node to 
	 * given visitor.
	 */
	public abstract void accept(MethodVisitor visitor);
	/**
	 * Get's called when child at specific address
	 * get's updated.
	 */
	public void onChildUpdated(int address) { }

	
	public AbstractCodeNode() {
		childs = new AbstractCodeNode[INITIAL_BUFFER_SIZE];
		codeAddress = 0;
	}
	
	public AbstractCodeNode read() {
		if (childs[codeAddress] == null)
			return null;
		return childs[codeAddress++];
	}
	
	public AbstractCodeNode read(int addr) {
		if (addr < 0 || addr >= childs.length || (addr > 0 && childs[addr - 1] == null))
			throw new IllegalArgumentException("Invalid address.");
		return childs[addr];
	}
	
	public void overwrite(AbstractCodeNode node) {
		if (needsExpand())
			expand();
		if (childs[codeAddress] != node) {
			childs[codeAddress] = node;
			onChildUpdated(codeAddress);
		}
	}
	
	public void overwrite(AbstractCodeNode node, int addr) {
		if (needsExpand())
			expand();
		if (addr < 0 || addr >= childs.length || (addr > 0 && childs[addr - 1] == null))
			throw new IllegalArgumentException("Invalid address.");
		if (childs[addr] != node) {
			childs[addr] = node;
			onChildUpdated(addr);
		}
	}
	
	public void write(AbstractCodeNode node) {
		if (needsExpand())
			expand();
		if (childs[codeAddress] == null) {
			childs[codeAddress] = node;
			onChildUpdated(codeAddress++);
		}
		else {
			List<AbstractCodeNode> taken = new ArrayList<AbstractCodeNode>();
			for (int i = codeAddress; i < childs.length; i++) {
				if (childs[i] != null) {
					taken.add(childs[i]);
					childs[i] = null;
				}
			}
			childs[codeAddress] = node;
			onChildUpdated(codeAddress);
			int write = ++codeAddress;
			for (AbstractCodeNode n : taken) {
				childs[write] = n;
				onChildUpdated(write++);
			}
		}
		
	}
	
	public void delete() {
		delete(codeAddress);
	}
	
	public void delete(int address) {
		if (address < 0 || address >= childs.length || (address > 0 && childs[address - 1] == null))
			throw new IllegalArgumentException("Invalid address.");
		if (childs[address] == null)
			throw new RuntimeException("No element to delete.");
		if ((address + 1) < childs.length && childs[address + 1] == null) {
			childs[address] = null;
			onChildUpdated(address);
		}
		else {
			childs[address] = null;
			onChildUpdated(address);
			for (int i = address + 1; i < childs.length; i++) {
				childs[i - 1] = childs[i];
				onChildUpdated(i - 1);
				childs[i] = null;
				onChildUpdated(i);
			}
		}
	}
	
	public int addressOf(AbstractCodeNode child) {
		for (int i = 0; i < childs.length; i++)
			if (childs[i] == child)
				return i;
		return -1;
	}
	
	public List<AbstractCodeNode> listChilds() {
		List<AbstractCodeNode> list = new ArrayList<AbstractCodeNode>();
		for (int i = 0; i < childs.length; i++)
			if (childs[i] != null)
				list.add(childs[i]);
		return list;
	}
	
	public int size() {
		int total = 0;
		for (int i = 0; i < childs.length; i++)
			if (childs[i] != null)
				total++;
		return total;
	}
	

	private boolean needsExpand() {
		double max = childs.length * 0.50;
		return (double)size() > max;
	}
	
	
	private void expand() {
		if (childs.length >= Integer.MAX_VALUE)
			throw new RuntimeException("Can't expand anymore.");
		long newSize = childs.length * 2;
		if (newSize > Integer.MAX_VALUE)
			newSize = Integer.MAX_VALUE;
		AbstractCodeNode[] newBuffer = new AbstractCodeNode[(int)newSize];
		System.arraycopy(childs, 0, newBuffer, 0, childs.length);
		childs = newBuffer;
	}	

	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public void setCodeAddress(int codeAddress) {
		if (codeAddress < 0 || codeAddress >= childs.length || (codeAddress > 0 && childs[codeAddress - 1] == null))
			throw new IllegalArgumentException("Invalid address.");
		this.codeAddress = codeAddress;
	}

	public int getCodeAddress() {
		return codeAddress;
	}
	

	
}
