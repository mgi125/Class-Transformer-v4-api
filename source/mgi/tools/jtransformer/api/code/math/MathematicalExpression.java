package mgi.tools.jtransformer.api.code.math;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;


public class MathematicalExpression extends ExpressionNode {
	public static final int TYPE_ADD = 0;
	public static final int TYPE_SUB = 1;
	public static final int TYPE_MUL = 2;
	public static final int TYPE_DIV = 3;
	public static final int TYPE_REM = 4;
	public static final int TYPE_SHL = 5;
	public static final int TYPE_SHR = 6;
	public static final int TYPE_USHR = 7;
	public static final int TYPE_OR = 8;
	public static final int TYPE_AND = 9;
	public static final int TYPE_XOR = 10;
	
	
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
	
	public MathematicalExpression(ExpressionNode right, ExpressionNode left, int type) {
		this.left = left;
		this.right = right;
		this.type = type;
		
		overwrite(left, 0);
		overwrite(right, 1);
		
		if (type < TYPE_ADD || type > TYPE_XOR)
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
		return new MathematicalExpression(right.copy(), left.copy(), type);
	}

	@Override
	public Type getType() {
		if (type == TYPE_SHL || type == TYPE_SHR)
			return Utilities.unaryOperationType(left.getType());
		else
			return Utilities.binaryOperationType(left.getType(), right.getType());
	}
	
	@Override
	public int getPriority() {
		switch (type) {
			case TYPE_ADD:
			case TYPE_SUB:
				return ExpressionNode.PRIORITY_ADDSUB;
			case TYPE_MUL:
			case TYPE_DIV:
			case TYPE_REM:
				return ExpressionNode.PRIORITY_MULDIVREM;
			case TYPE_SHL:
			case TYPE_SHR:
			case TYPE_USHR:
				return ExpressionNode.PRIORITY_BITSHIFTS;
			case TYPE_OR:
				return ExpressionNode.PRIORITY_BITOR;
			case TYPE_AND:
				return ExpressionNode.PRIORITY_BITAND;
			case TYPE_XOR:
				return ExpressionNode.PRIORITY_BITXOR;
			default:
				return super.getPriority();
		}
	}
	
	/**
	 * Generate's operator string.
	 */
	public String operator() {
		switch (type) {
			case TYPE_ADD:
				return "+";
			case TYPE_SUB:
				return "-";
			case TYPE_MUL:
				return "*";
			case TYPE_DIV:
				return "/";
			case TYPE_REM:
				return "%";
			case TYPE_SHL:
				return "<<";
			case TYPE_SHR:
				return ">>";
			case TYPE_USHR:
				return ">>>";
			case TYPE_OR:
				return "|";
			case TYPE_AND:
				return "&";
			case TYPE_XOR:
				return "^";
			default:
				return "(unknown operator)";
		}
	}

	@Override
	public void accept(MethodVisitor visitor) {
		Type leftType = null;
		Type rightType = null;
		if (type == TYPE_SHL || type == TYPE_SHR) {
			leftType = getType();
			rightType = Type.INT_TYPE;
		}
		else {
			leftType = rightType = getType();
		}
		left.accept(visitor);
		int[] lCast = Utilities.primitiveCastOpcodes(left.getType(), leftType);
		for (int i = 0; i < lCast.length; i++)
			visitor.visitInsn(lCast[i]);
		
		right.accept(visitor);
		int[] rCast = Utilities.primitiveCastOpcodes(right.getType(), rightType);
		for (int i = 0; i < rCast.length; i++)
			visitor.visitInsn(rCast[i]);
		int opcode;
		switch (type) {
			case TYPE_ADD:
				opcode = Utilities.addOpcode(getType());
				break;
			case TYPE_SUB:
				opcode = Utilities.subtractOpcode(getType());
				break;
			case TYPE_MUL:
				opcode = Utilities.multiplyOpcode(getType());
				break;
			case TYPE_DIV:
				opcode = Utilities.divideOpcode(getType());
				break;
			case TYPE_REM:
				opcode = Utilities.remainderOpcode(getType());
				break;
			case TYPE_SHL:
				opcode = Utilities.bitShiftLeftOpcode(getType());
				break;
			case TYPE_SHR:
				opcode = Utilities.bitShiftRightOpcode(getType());
				break;
			case TYPE_USHR:
				opcode = Utilities.bitShiftRightUnsignedOpcode(getType());
				break;
			case TYPE_OR:
				opcode = Utilities.bitOrOpcode(getType());
				break;
			case TYPE_AND:
				opcode = Utilities.bitAndOpcode(getType());
				break;
			case TYPE_XOR:
				opcode = Utilities.bitXorOpcode(getType());
				break;
			default:
				throw new RuntimeException("WT");
		}
		visitor.visitInsn(opcode);
	}

	@Override
	public void print(CodePrinter printer) {
		int selfPriority = this.getPriority();
		int leftPriority = left.getPriority();
		int rightPriority = right.getPriority();
		if (leftPriority > selfPriority)
			printer.print('(');
		left.print(printer);
		if (leftPriority > selfPriority)
			printer.print(')');
		printer.print(" " + operator() + " ");
		if (rightPriority > selfPriority)
			printer.print('(');
		right.print(printer);
		if (rightPriority > selfPriority)
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
		if (type < TYPE_ADD || type > TYPE_XOR)
			throw new RuntimeException("WT");
		this.type = type;
	}

}
