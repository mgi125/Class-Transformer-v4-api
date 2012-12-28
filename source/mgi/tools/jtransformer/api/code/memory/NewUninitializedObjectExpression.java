package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class NewUninitializedObjectExpression extends ExpressionNode {

	/**
	 * Type of the object.
	 */
	private Type type;
	
	public NewUninitializedObjectExpression(Type type) {
		this.type = type;
		
		if (type.getSort() != Type.OBJECT)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return true;
	}
	
	@Override
	public boolean altersLogic() {
		return false;
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return false;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public ExpressionNode copy() {
		return new NewUninitializedObjectExpression(type);
	}

	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_NEWOPERATOR;
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitTypeInsn(Opcodes.NEW, type.getInternalName());
	}
	

	@Override
	public void print(CodePrinter printer) {
		printer.print("new " + type.getClassName());
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public Type getObjectType() {
		return type;
	}
	
	public void setObjectType(Type type) {
		this.type = type;
	}

}
