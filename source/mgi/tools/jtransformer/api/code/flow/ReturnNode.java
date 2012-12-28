package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.objectweb.asm.Opcodes;

public class ReturnNode extends AbstractCodeNode {

	/**
	 * Contains return type.
	 */
	private Type type;
	/**
	 * Expression which is going to be returned.
	 */
	private ExpressionNode expression;
	
	public ReturnNode() {
		this(Type.VOID_TYPE, null);
	}
	
	public ReturnNode(Type type, ExpressionNode expr) {
		this.type = type;
		this.expression = expr;
		
		overwrite(expr, 0);
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return expression != null && expression.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return true;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return expression != null && expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
	}
	
	
	@Override
	public void accept(MethodVisitor visitor) {
		if (type != Type.VOID_TYPE) {
			expression.accept(visitor);
			if (Utilities.isPrimitive(type)) {
				int[] cast = Utilities.primitiveCastOpcodes(expression.getType(), type); // widen
				for (int i = 0; i < cast.length; i++)
					visitor.visitInsn(cast[i]);
			}
			visitor.visitInsn(Utilities.returnOpcode(type));
		}
		else {
			visitor.visitInsn(Opcodes.RETURN);
		}
	}

	@Override
	public void print(CodePrinter printer) {
		if (expression != null) {
			printer.print("return ");
			expression.print(printer);
			printer.print(';');
		}
		else {
			printer.print("return;");
		}
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	

}
