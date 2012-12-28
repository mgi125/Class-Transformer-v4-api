package mgi.tools.jtransformer.api;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jtransformer.Constants;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

public class FieldNode extends FieldVisitor {
	/**
	 * Contains parent (class) of this field.
	 */
	private ClassNode parent;
	/**
	 * Contains accessor of this field.
	 */
	private int accessor;
	/**
	 * Contains name of this field.
	 */
	private String name;
	/**
	 * Contains descriptor of this field.
	 */
	private String descriptor;
	/**
	 * Contains generics signature of this field.
	 */
	private String signature;
	/**
	 * Contains default static value for
	 * this field.
	 */
	private Object value;
	
	/**
	 * Contains unknown field attributes.
	 */
	private List<Attribute> unknownAttributes = new ArrayList<Attribute>();
	
	
	public FieldNode(int accessor,String name,String descriptor,String signature, Object value,ClassNode parent) {
		super(Constants.ASM_API_VERSION);
		this.accessor = accessor;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.value = value;
		this.parent = parent;
	}
	
	@Override
	public void visitAttribute(Attribute attr) {
		unknownAttributes.add(attr);
	}
	
	@Override
	public void visitEnd() {
	}
	
	
	/**
	 * Strips any debugging information.
	 */
	public void stripDebugInformation() {
	}
	
	
	/**
	 * Forward's information about this field contents to given
	 * visitor.
	 */
	public void accept(FieldVisitor visitor) {
		for (Attribute attribute : unknownAttributes)
			visitor.visitAttribute(attribute);
		visitor.visitEnd();
	}

	
	
	public void setParent(ClassNode parent) {
		this.parent = parent;
	}

	public ClassNode getParent() {
		return parent;
	}

	public void setAccessor(int accessor) {
		this.accessor = accessor;
	}

	public int getAccessor() {
		return accessor;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "FieldNode[" + name + "," + descriptor + "," + signature + "," + value + "]";
	}
}
