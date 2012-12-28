package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class InvokeExpression extends ExpressionNode {

	public static final int TYPE_INVOKESPECIAL   = 0;
	public static final int TYPE_INVOKEVIRTUAL   = 1;
	public static final int TYPE_INVOKEINTERFACE = 2;
	public static final int TYPE_INVOKESTATIC    = 3;
	
	private static final int[] TYPE_TO_OPCODE = new int[] 
	{
		Opcodes.INVOKESPECIAL, Opcodes.INVOKEVIRTUAL, 
		Opcodes.INVOKEINTERFACE, Opcodes.INVOKESTATIC 
	}; 
	
	/**
	 * Type of this invoke.
	 */
	private int type;
	/**
	 * Contains all arguments including 
	 * base (if it's instanced method).
	 */
	private ExpressionNode[] arguments;
	/**
	 * Name of the method owner.
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
	
	public InvokeExpression(int type, ExpressionNode[] arguments, String owner, String name, String descriptor) {
		this.type = type;
		this.arguments = arguments;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		
		for (int i = 0; i < arguments.length; i++)
			overwrite(arguments[i], i);
		
		if (type < TYPE_INVOKESPECIAL || type > TYPE_INVOKESTATIC)
			throw new RuntimeException("WT");
		if (type != TYPE_INVOKESTATIC && arguments.length <= 0)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return true;
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		if (n.altersLogic())
			return true;
		for (int i = 0; i < arguments.length; i++)
			if (arguments[i].affectedBy(n))
				return true;
		return false;
	}
	
	@Override
	public ExpressionNode copy() {
		ExpressionNode[] arguments = new ExpressionNode[this.arguments.length];
		for (int i = 0; i < arguments.length; i++)
			arguments[i] = this.arguments[i].copy();
		return new InvokeExpression(this.type, arguments, owner, name, descriptor);
	}

	@Override
	public Type getType() {
		return Type.getReturnType(descriptor);
	}

	@Override
	public void accept(MethodVisitor visitor) {
		Type[] argTypes = Type.getArgumentTypes(descriptor);
		if (argTypes.length < arguments.length) {
			Type[] bck = argTypes;
			argTypes = new Type[bck.length + 1];
			System.arraycopy(bck, 0, argTypes, 1, bck.length);
			argTypes[0] = Type.getType("L" + owner + ";");
		}
		
		for (int i = 0; i < arguments.length; i++) {
			arguments[i].accept(visitor);
			if (Utilities.isPrimitive(arguments[i].getType())) {
				int[] cast = Utilities.primitiveCastOpcodes(arguments[i].getType(), argTypes[i]);
				for (int a = 0; a < cast.length; a++)
					visitor.visitInsn(cast[a]);
			}
		}
		visitor.visitMethodInsn(TYPE_TO_OPCODE[type], owner, name, descriptor);
	}
	
	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_CALL;
	}
	
	@Override
	public void onChildUpdated(int addr) {
		updateArgument(addr, (ExpressionNode)read(addr));
	}

	@Override
	public void print(CodePrinter printer) {
		boolean instanced = type != TYPE_INVOKESTATIC;
		if (instanced) {
			int selfPriority = ExpressionNode.PRIORITY_MEMBER_ACCESS;
			int basePriority = arguments[0].getPriority();
			if (basePriority > selfPriority)
				printer.print('(');
			arguments[0].print(printer);
			if (basePriority > selfPriority)
				printer.print(')');
		}
		else
			printer.print(owner.replace('/', '.'));
		printer.print('.');
		printer.print(name);
		printer.print('(');
		for (int i = instanced ? 1 : 0; i < arguments.length; i++) {
			boolean needsComma = (i + 1) < arguments.length;
			arguments[i].print(printer);
			if (needsComma)
				printer.print(", ");
		}
		printer.print(')');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public int getCallType() {
		return type;
	}
	
	public void setCallType(int type) {
		if (type < TYPE_INVOKESPECIAL || type > TYPE_INVOKESTATIC)
			throw new RuntimeException("WT");
		this.type = type;
	}
	
	public ExpressionNode[] getArguments() {
		return arguments;
	}
	
	public void setArguments(ExpressionNode[] arguments) {
		if (type != TYPE_INVOKESTATIC && arguments.length <= 0)
			throw new RuntimeException("WT");
		if (arguments.length < this.arguments.length) {
			setCodeAddress(0);
			while (read(0) != null)
				delete();
		}
		this.arguments = arguments;
		for (int i = 0; i < arguments.length; i++)
			overwrite(arguments[i], i);
	}
	
	public void updateArgument(int id, ExpressionNode argument) {
		if (id < 0 || id >= arguments.length)
			throw new RuntimeException("WT");
		arguments[id] = argument;
		overwrite(argument, id);
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}

}
