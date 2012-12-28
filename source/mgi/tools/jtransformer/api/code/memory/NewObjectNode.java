package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class NewObjectNode extends AbstractCodeNode {

	/**
	 * Contains type of object
	 */
	private Type type;
	/**
	 * Contains all arguments.
	 */
	private ExpressionNode[] arguments;
	/**
	 * Name of the method owner.
	 */
	private String owner;
	/**
	 * Descriptor of the method.
	 */
	private String descriptor;
	
	public NewObjectNode(Type type, ExpressionNode[] arguments, String owner, String descriptor) {
		this.type = type;
		this.arguments = arguments;
		this.owner = owner;
		this.descriptor = descriptor;
		
		for (int i = 0; i < arguments.length; i++)
			overwrite(arguments[i], i);
		
		if (type.getSort() != Type.OBJECT)
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
	public void accept(MethodVisitor visitor) {
		Type[] argTypes = Type.getArgumentTypes(descriptor);
		if (argTypes.length < arguments.length) {
			Type[] bck = argTypes;
			argTypes = new Type[bck.length + 1];
			System.arraycopy(bck, 0, argTypes, 1, bck.length);
			argTypes[0] = Type.getType("L" + owner + ";");
		}
		
		visitor.visitTypeInsn(Opcodes.NEW, type.getInternalName());
		for (int i = 0; i < arguments.length; i++) {
			arguments[i].accept(visitor);
			if (Utilities.isPrimitive(arguments[i].getType())) {
				int[] cast = Utilities.primitiveCastOpcodes(arguments[i].getType(), argTypes[i]);
				for (int a = 0; a < cast.length; a++)
					visitor.visitInsn(cast[a]);
			}
		}
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, "<init>", descriptor);
	}

	
	@Override
	public void onChildUpdated(int addr) {
		updateArgument(addr, (ExpressionNode)read(addr));
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("new ");
		printer.print(type.getInternalName().replace('/', '.'));
		printer.print('(');
		for (int i = 0; i < arguments.length; i++) {
			boolean needsComma = (i + 1) < arguments.length;
			arguments[i].print(printer);
			if (needsComma)
				printer.print(", ");
		}
		printer.print(')');
		printer.print(';');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public ExpressionNode[] getArguments() {
		return arguments;
	}
	
	public void setArguments(ExpressionNode[] arguments) {
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
	
	public Type getObjectType() {
		return type;
	}
	
	public void setObjectType(Type type) {
		if (type.getSort() != Type.OBJECT)
			throw new RuntimeException("WT");
		this.type = type;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}

}
