package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.LocalVariableDeclaration;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class VariableAssignmentExpression extends ExpressionNode {

	/**
	 * Contains declaration of local variable.
	 */
	private LocalVariableDeclaration declaration;
	/**
	 * Expression which is being stored.
	 */
	private ExpressionNode expression;
	
	public VariableAssignmentExpression(LocalVariableDeclaration declaration, ExpressionNode expr) {
		this.declaration = declaration;
		this.expression = expr;
		
		overwrite(expr, 0);
	}
	
	@Override
	public boolean canTrim() {
		return false;
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
		return expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
	}
	
	@Override
	public ExpressionNode copy() {
		return new VariableAssignmentExpression(declaration, expression.copy());
	}

	@Override
	public Type getType() {
		return declaration.getType();
	}
	
	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_ASSIGNMENT;
	}

	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		if (Utilities.isPrimitive(declaration.getType())) {
			int[] cast = Utilities.primitiveCastOpcodes(expression.getType(), declaration.getType());
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
		}
		visitor.visitInsn(Utilities.dupOpcode(getType()));
		visitor.visitVarInsn(Utilities.variableStoreOpcode(getType()), declaration.getIndex());
	}

	@Override
	public void print(CodePrinter printer) {
		int selfPriority = getPriority();
		int expressionPriority = expression.getPriority();
		printer.print(declaration.getName() + " = ");
		if (expressionPriority > selfPriority)
			printer.print('(');
		expression.print(printer);
		if (expressionPriority > selfPriority)
			printer.print(')');
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

	public LocalVariableDeclaration getDeclaration() {
		return declaration;
	}

	public void setDeclaration(LocalVariableDeclaration declaration) {
		this.declaration = declaration;
	}




}
