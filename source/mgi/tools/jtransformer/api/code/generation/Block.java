package mgi.tools.jtransformer.api.code.generation;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;


public class Block {
	private LabelAnnotation annotation = new LabelAnnotation();
	private List<AbstractCodeNode> code = new ArrayList<AbstractCodeNode>();
	private int startAddress;
	private Stack entryStack;
	
	public Block(int startAddress) {
		this(startAddress,null);
	}
	
	public Block(int startAddress, Stack entryStack) {
		this.startAddress = startAddress;
		this.entryStack = entryStack;
	}

	public int getStartAddress() {
		return startAddress;
	}
	
	public void setEntryStack(Stack stack) {
		entryStack = stack;
	}

	public Stack getEntryStack() {
		return entryStack;
	}

	public List<AbstractCodeNode> getCode() {
		return code;
	}

	public LabelAnnotation getAnnotation() {
		return annotation;
	}
}
