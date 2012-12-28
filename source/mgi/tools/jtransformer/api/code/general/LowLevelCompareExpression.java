package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class LowLevelCompareExpression extends ExpressionNode {
	public static final int TYPE_CMPG = 0;
	public static final int TYPE_CMPL = 1;
	
	
	/**
	 * Left value of this expression.
	 */
	private ExpressionNode left;
	/**
	 * Right value of this expression.
	 */
	private ExpressionNode right;
	/**
	 * Type of this expression.
	 */
	private int type;
	
	public LowLevelCompareExpression(ExpressionNode right, ExpressionNode left, int type) {
		this.left = left;
		this.right = right;
		this.type = type;
		
		overwrite(left, 0);
		overwrite(right, 1);
		
		if (type < TYPE_CMPG || type > TYPE_CMPL)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return left.canTrim() && right.canTrim();
	}
	
	@Override
	public boolean altersLogic() {
		return left.altersLogic() || right.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return left.affectedBy(n) || right.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		if (addr == 0)
			setLeft((ExpressionNode)read(addr));
		else if (addr == 1)
			setRight((ExpressionNode)read(addr));
	}
	
	
	@Override
	public ExpressionNode copy() {
		return new LowLevelCompareExpression(right.copy(), left.copy(), type);
	}

	@Override
	public Type getType() {
		return Type.INT_TYPE;
	}
	
	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_CALL;
	}


	@Override
	public void accept(MethodVisitor visitor) {
		left.accept(visitor);
		right.accept(visitor);
		
		if (left.getType() == Type.LONG_TYPE || right.getType() == Type.LONG_TYPE) {
			visitor.visitInsn(Opcodes.LCMP);
		}
		else if (left.getType() == Type.FLOAT_TYPE || right.getType() == Type.FLOAT_TYPE) {
			visitor.visitInsn(type == TYPE_CMPG ? Opcodes.FCMPG : Opcodes.FCMPL);
		}
		else if (left.getType() == Type.DOUBLE_TYPE || right.getType() == Type.DOUBLE_TYPE) {
			visitor.visitInsn(type == TYPE_CMPG ? Opcodes.DCMPG : Opcodes.DCMPL);
		}
		else
			throw new RuntimeException("WT");
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print('(');
		left.print(printer);
		printer.print(" |LOWCMP| ");
		right.print(printer);
		printer.print(')');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}


	public void setLeft(ExpressionNode left) {
		this.left = left;
		overwrite(this.left, 0);
	}

	public ExpressionNode getLeft() {
		return left;
	}

	public void setRight(ExpressionNode right) {
		this.right = right;
		overwrite(this.right, 1);
	}

	public ExpressionNode getRight() {
		return right;
	}
	
	public int getOperationType() {
		return type;
	}
	
	public void setOperationType(int type) {
		if (type < TYPE_CMPG || type > TYPE_CMPL)
			throw new RuntimeException("WT");
		this.type = type;
	}

}
