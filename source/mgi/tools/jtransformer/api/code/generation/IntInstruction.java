package mgi.tools.jtransformer.api.code.generation;

public class IntInstruction extends AbstractInstruction {

	/**
	 * Operand of this instruction.
	 */
	private int operand;
	
	public IntInstruction(int opcode, int operand) {
		super(opcode);
		this.operand = operand;
	}

	public int getOperand() {
		return operand;
	}

	@Override
	public String toString() {
		return super.toString() + "\t" + operand;
	}

}
