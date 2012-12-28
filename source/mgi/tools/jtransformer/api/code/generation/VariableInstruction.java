package mgi.tools.jtransformer.api.code.generation;

public class VariableInstruction extends AbstractInstruction {

	/**
	 * Index of the local variable.
	 */
	private int index;
	
	public VariableInstruction(int opcode, int index) {
		super(opcode);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\t" + index;
	}


}
