package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Opcodes;

public class IntIncrementInstruction extends AbstractInstruction {
	/**
	 * Index of the local variable.
	 */
	private int index;
	/**
	 * Amount to increment.
	 */
	private int incrementor;
	
	public IntIncrementInstruction(int index, int incrementor) {
		super(Opcodes.IINC);
		this.index = index;
		this.incrementor = incrementor;
	}

	public int getIndex() {
		return index;
	}

	public int getIncrementor() {
		return incrementor;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\t" + index + " " + incrementor;
	}
	
	
}
