package mgi.tools.jtransformer.api.code;

import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;

public class LocalVariableInformation {
	
	/**
	 * Name of the local variable.
	 */
	private String name;
	/**
	 * Descriptor of the local variable.
	 */
	private String descriptor;
	/**
	 * Generics signature of the local variable (if any)
	 */
	private String signature;
	/**
	 * Position where local variable was declared.
	 */
	private LabelAnnotation start;
	/**
	 * Position where scope in which local variable
	 * was declared ends.
	 */
	private LabelAnnotation end;
	/**
	 * Index of the local variable.
	 */
	private int index;
	
	public LocalVariableInformation(String name, String descriptor, String signature, LabelAnnotation start, LabelAnnotation end, int index) {
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.start = start;
		this.end = end;
		this.index = index;
	}
	
	
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setStart(LabelAnnotation start) {
		this.start = start;
	}

	public LabelAnnotation getStart() {
		return start;
	}

	public void setEnd(LabelAnnotation end) {
		this.end = end;
	}

	public LabelAnnotation getEnd() {
		return end;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}


	@Override
	public String toString() {
		return "LocalVariable[" + name + "," + descriptor + "," + signature + ",l_" + start.getLabel().hashCode() + ",l_" + end.getLabel().hashCode() + "," + index + "]";
	}
	



}
