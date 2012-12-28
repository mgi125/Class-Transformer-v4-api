package mgi.tools.jtransformer.api.code.generation;

import mgi.tools.jtransformer.api.code.tools.Utilities;

public abstract class AbstractInstruction {
	/**
	 * Opcode of this instruction.
	 */
	private int opcode;
	
	public AbstractInstruction(int opcode) {
		this.opcode = opcode;
	}
	
	public int getOpcode() {
		return opcode;
	}
	
	@Override
	public String toString() {
		return Utilities.getOpcodeName(opcode);
	}
}
