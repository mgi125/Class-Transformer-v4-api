package mgi.tools.jtransformer.api.code.math;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;


public class NegateExpression extends ExpressionNode {	
	/**
	 * Value of this expression.
	 */
	private ExpressionNode expression;
	
	public NegateExpression(ExpressionNode expression) {
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
		return expression.affectedBy(n);
	}
	
	@Override
	public ExpressionNode copy() {
		return new NegateExpression(expression.copy());
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
	}

	@Override
	public Type getType() {
		return Utilities.unaryOperationType(expression.getType());
	}
	
	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_UNARYPLUSMINUS;
	}

	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		int[] cast = Utilities.primitiveCastOpcodes(expression.getType(), getType());
		for (int i = 0; i < cast.length; i++)
			visitor.visitInsn(cast[i]);
		visitor.visitInsn(Utilities.negateOpcode(getType()));
	}

	@Override
	public void print(CodePrinter printer) {
		int selfPriority = getPriority();
		int exprPriority = expression.getPriority();
		printer.print('-');
		if (exprPriority > selfPriority)
			printer.print('(');
		expression.print(printer);
		if (exprPriority > selfPriority)
			printer.print(')');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(expression, 0);
	}

	public ExpressionNode getExpression() {
		return expression;
	}




}
