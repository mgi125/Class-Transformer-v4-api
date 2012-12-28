package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class InstanceofExpression extends ExpressionNode {

	/**
	 * Expression which will be tested.
	 */
	private ExpressionNode expression;
	/**
	 * Type which will be tested with expression.
	 */
	private Type type;
	
	public InstanceofExpression(ExpressionNode expression, Type type) {
		this.expression = expression;
		this.type = type;
		
		overwrite(expression, 0);
		
		if (type.getSort() <= Type.DOUBLE)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return expression.canTrim();
	}
	
	@Override
	public boolean altersLogic() {
		return expression.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(0));
	}
	
	@Override
	public Type getType() {
		return Type.BOOLEAN_TYPE;
	}
	
	@Override
	public ExpressionNode copy() {
		return new InstanceofExpression(expression.copy(), type);
	}

	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_LELTGEGTINSTANCEOF;
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		visitor.visitTypeInsn(Opcodes.INSTANCEOF, type.getInternalName());
	}
	

	@Override
	public void print(CodePrinter printer) {
		int selfPriority = getPriority();
		int expressionPriority = expression.getPriority();
		if (expressionPriority > selfPriority)
			printer.print('(');
		expression.print(printer);
		if (expressionPriority > selfPriority)
			printer.print(')');
		printer.print(" instanceof ");
		printer.print(type.getClassName());
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public ExpressionNode getExpression() {
		return expression;
	}
	
	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(expression, 0);
	}

	public Type getObjectType() {
		return type;
	}
	
	public void setObjectType(Type type) {
		if (type.getSort() <= Type.DOUBLE)
			throw new RuntimeException("WT");
		this.type = type;
	}

}
