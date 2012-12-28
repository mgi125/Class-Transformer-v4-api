package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ArrayLengthExpression extends ExpressionNode {

	/**
	 * Expression of array.
	 */
	private ExpressionNode expression;
	
	public ArrayLengthExpression(ExpressionNode expression) {
		this.expression = expression;

		overwrite(expression, 0);
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
		return n.altersLogic() || expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(0));
	}
	
	@Override
	public Type getType() {
		return Type.INT_TYPE;
	}
	
	@Override
	public ExpressionNode copy() {
		return new ArrayLengthExpression(expression.copy());
	}

	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_MEMBER_ACCESS;
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		visitor.visitInsn(Opcodes.ARRAYLENGTH);
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
		printer.print(".length");
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
}
