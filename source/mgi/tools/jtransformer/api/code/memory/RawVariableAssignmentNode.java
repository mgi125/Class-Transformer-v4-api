package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class RawVariableAssignmentNode extends AbstractCodeNode {

	/**
	 * Contains type of variable.
	 */
	private Type variableType;
	/**
	 * Index to which expression is being stored.
	 */
	private int index;
	/**
	 * Expression which is being stored.
	 */
	private ExpressionNode expression;
	
	public RawVariableAssignmentNode(Type variableType, int index, ExpressionNode expr) {
		this.variableType = variableType;
		this.index = index;
		this.expression = expr;
		
		overwrite(expr, 0);
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
		if (Utilities.isPrimitive(variableType)) {
			int[] cast = Utilities.primitiveCastOpcodes(expression.getType(), variableType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
		}
		visitor.visitVarInsn(Utilities.variableStoreOpcode(variableType), index);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("var_" + index + " = ");
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

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public Type getVariableType() {
		return variableType;
	}

	public void setVariableType(Type variableType) {
		this.variableType = variableType;
	}

}
