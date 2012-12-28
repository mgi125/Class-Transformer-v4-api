package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

public class DynamicMethodInstruction extends AbstractInstruction {

	/**
	 * Name of the method.
	 */
	private String name;
	/**
	 * Descriptor of the method.
	 */
	private String descriptor;
	/**
	 * Handle of the bootstrap method.
	 */
	private Handle bootstrapHandle;
	/**
	 * Bootstrap arguments.
	 */
	private Object[] bootstrapArguments;
	
	
	public DynamicMethodInstruction(String name, String descriptor, Handle bsh, Object[] bsa) {
		super(Opcodes.INVOKEDYNAMIC);
		this.name = name;
		this.descriptor = descriptor;
		this.bootstrapHandle = bsh;
		this.bootstrapArguments = bsa;
	}

	public String getName() {
		return name;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public Handle getBootstrapHandle() {
		return bootstrapHandle;
	}

	public Object[] getBootstrapArguments() {
		return bootstrapArguments;
	}
	
	@Override
	public String toString() {
		String argsString = "";
		for (int i = 0; i < bootstrapArguments.length; i++)
			argsString += bootstrapArguments[i].toString() + ((i + 1) <= bootstrapArguments.length ? "," : "");
		return super.toString() + "\t" + name + " " + descriptor + " " + bootstrapHandle + " " + argsString;
	}

}
