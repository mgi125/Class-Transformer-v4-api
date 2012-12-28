package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Opcodes;

public class LoadConstantInstruction extends AbstractInstruction {
	/**
	 * Constant that this instruction loads.
	 */
	private Object constant;
	
	public LoadConstantInstruction(Object constant) {
		super(Opcodes.LDC);
		this.constant = constant;
	}
	
	public Object getConstant() {
		return constant;
	}
	
	public String toString() {
		return super.toString() + "\t" + constant;
	}

}
