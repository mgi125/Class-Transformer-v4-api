package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class PopableNode extends AbstractCodeNode {

	/**
	 * Expression which is going to be poped.
	 */
	private ExpressionNode expression;
	
	public PopableNode(ExpressionNode expr) {
		this.expression = expr;
		
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
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		if (expression.getType() != Type.VOID_TYPE)
			visitor.visitInsn(Utilities.popOpcode(expression.getType()));
	}

	@Override
	public void print(CodePrinter printer) {
		expression.print(printer);
		printer.print(';');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}


	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(this.expression, 0);
	}

	public ExpressionNode getExpression() {
		return expression;
	}
	
	

}
