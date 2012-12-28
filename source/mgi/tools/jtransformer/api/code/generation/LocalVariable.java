package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;

public class LocalVariable {
	/**
	 * Name of the local variable.
	 */
	private String name;
	/**
	 * Descriptor of the local variable.
	 */
	private String descriptor;
	/**
	 * Signature of the local variable.
	 */
	private String signature;
	/**
	 * Marker where variable was declared.
	 */
	private Label start;
	/**
	 * Marker where scope of the declared 
	 * variable ends.
	 */
	private Label end;
	/**
	 * Index of the local variable.
	 */
	private int index;
	
	public LocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.start = start;
		this.end = end;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public String getSignature() {
		return signature;
	}

	public Label getStart() {
		return start;
	}

	public Label getEnd() {
		return end;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return "LocalVariable[" + name + "," + descriptor + "," + signature + ",l_" + start.hashCode() + ",l_" + end.hashCode() + "," + index + "]";
	}
	
	
}
