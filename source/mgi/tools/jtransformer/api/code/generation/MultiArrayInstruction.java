package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Opcodes;

public class MultiArrayInstruction extends AbstractInstruction {
	/**
	 * Descriptor of the array element.
	 */
	private String descriptor;
	/**
	 * Amount of dimensions.
	 */
	private int dimensions;
	
	public MultiArrayInstruction(String descriptor, int dimensions) {
		super(Opcodes.MULTIANEWARRAY);
		this.descriptor = descriptor;
		this.dimensions = dimensions;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public int getDimensions() {
		return dimensions;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\t" + descriptor + " " + dimensions;
	}
}
