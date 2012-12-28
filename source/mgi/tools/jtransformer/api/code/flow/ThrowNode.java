package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ThrowNode extends AbstractCodeNode {

	/**
	 * Expression which is going to be throwed.
	 */
	private ExpressionNode expression;
	
	public ThrowNode(ExpressionNode expr) {
		this.expression = expr;
		
		overwrite(expression, 0);
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return expression.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return true;
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
		visitor.visitInsn(Opcodes.ATHROW);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("throw ");
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
