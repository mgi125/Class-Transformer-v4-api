package mgi.tools.jtransformer.api.code.generation;

public class TypeInstruction extends AbstractInstruction {

	/**
	 * Name of the type (. replaced by /)
	 */
	public String type;
	
	public TypeInstruction(int opcode, String type) {
		super(opcode);
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\n" + type;
	}
}
