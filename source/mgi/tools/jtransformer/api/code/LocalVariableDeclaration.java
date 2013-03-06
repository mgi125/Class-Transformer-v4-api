package mgi.tools.jtransformer.api.code;

import org.objectweb.asm.Type;

public class LocalVariableDeclaration {

	/**
	 * Contains type of the local
	 * variable.
	 */
	private Type type;
	/**
	 * Name of the local variable,
	 * may be null.
	 */
	private String name;
	/**
	 * Index of the local variable.
	 */
	private int index;
	/**
	 * Whether variable is argument.
	 */
	private boolean isArgument;
	
	public LocalVariableDeclaration(Type type, int index, boolean isArgument) {
		this(type, null, index, isArgument);
	}
	
	public LocalVariableDeclaration(Type type, String name, int index, boolean isArgument) {
		this.type = type;
		this.name = name;
		this.index = index;
		this.isArgument = isArgument;
	}
	

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean hasName() {
		return name != null;
	}

	public String getName() {
		if (hasName())
			return name;
		else
			return "var_" + index;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isArgument() {
		return isArgument;
	}

	public void setIsArgument(boolean isArgument) {
		this.isArgument = isArgument;
	}

	@Override
	public String toString() {
		return type.getClassName() + " " + getName();
	}
}
