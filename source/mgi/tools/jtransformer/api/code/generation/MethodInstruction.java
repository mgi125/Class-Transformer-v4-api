package mgi.tools.jtransformer.api.code.generation;

public class MethodInstruction extends AbstractInstruction {

	/**
	 * Name of the class that owns
	 * this method.
	 */
	private String owner;
	/**
	 * Name of the method.
	 */
	private String name;
	/**
	 * Descriptor of the method.
	 */
	private String descriptor;
	
	public MethodInstruction(int opcode,String owner, String name, String descriptor) {
		super(opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescriptor() {
		return descriptor;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\t" + owner + " " + name + " " + descriptor;
	}

}
