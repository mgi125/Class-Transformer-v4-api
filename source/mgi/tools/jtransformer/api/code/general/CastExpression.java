package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CastExpression extends ExpressionNode {	
	/**
	 * Left value of this expression.
	 */
	private ExpressionNode expression;
	/**
	 * New type of the expression to cast to.
	 */
	private Type newType;
	
	public CastExpression(ExpressionNode expression,Type newType) {
		this.expression = expression;
		this.newType = newType;
		
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
		return new CastExpression(expression.copy(), newType);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
	}

	@Override
	public Type getType() {
		return newType;
	}
	
	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_CAST;
	}

	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		if (Utilities.isObjectRef(getType())) {
			visitor.visitTypeInsn(Opcodes.CHECKCAST, newType.getInternalName());
		}
		else {
			int[] instructions = Utilities.primitiveCastOpcodes(expression.getType(), newType);
			for (int i = 0; i < instructions.length; i++)
				visitor.visitInsn(instructions[i]);
		}
	}

	@Override
	public void print(CodePrinter printer) {
		int selfPriority = getPriority();
		int exprPriority = expression.getPriority();
		printer.print('(');
		printer.print(newType.getClassName());
		printer.print(')');
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

	public void setNewType(Type newType) {
		this.newType = newType;
	}

	public Type getNewType() {
		return newType;
	}




}
