package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.general.ConstantExpression;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class ConditionalJumpNode extends AbstractCodeNode {
	public static final int COMPARE_EQ = 0;
	public static final int COMPARE_NE = 1;
	public static final int COMPARE_LT = 2;
	public static final int COMPARE_GE = 3;
	public static final int COMPARE_GT = 4;
	public static final int COMPARE_LE = 5;
	
	
	/**
	 * Left side of condition.
	 */
	private ExpressionNode left;
	/**
	 * Right side of condition.
	 */
	private ExpressionNode right;
	/**
	 * Contains condition type.
	 */
	private int type;
	/**
	 * Jump target of condition.
	 */
	private LabelAnnotation target;
	
	public ConditionalJumpNode(ExpressionNode left, ExpressionNode right, int type, LabelAnnotation target) {
		this.left = left;
		this.right = right;
		this.type = type;
		this.target = target;
		
		overwrite(left, 0);
		overwrite(right, 1);
		
		if (type < COMPARE_EQ || type > COMPARE_LE)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return left.altersLogic() || right.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return true;
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
	public void accept(MethodVisitor visitor) {	
		Type opType = Utilities.binaryOperationType(left.getType(), right.getType());
		
		if (Utilities.isObjectRef(opType)) {
			boolean isNull = right instanceof ConstantExpression && ((ConstantExpression)right).getConstant() == null;
			if (type != COMPARE_EQ && type != COMPARE_NE)
				throw new RuntimeException("WT");
			
			left.accept(visitor);
			if (isNull) {
				visitor.visitJumpInsn(type == COMPARE_EQ ? Opcodes.IFNULL : Opcodes.IFNONNULL, target.getLabel());
			}
			else {
				right.accept(visitor);
				visitor.visitJumpInsn(type == COMPARE_EQ ? Opcodes.IF_ACMPEQ : Opcodes.IF_ACMPNE, target.getLabel());
			}
		}
		else if (opType == Type.INT_TYPE) {
			boolean canShorten = right instanceof ConstantExpression && ((ConstantExpression)right).getConstant() instanceof Number && ((Number)((ConstantExpression)right).getConstant()).intValue() == 0;
			
			left.accept(visitor);
			int[] cast = Utilities.primitiveCastOpcodes(left.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			if (canShorten) {
				visitor.visitJumpInsn(Opcodes.IFEQ + type, target.getLabel());
			}
			else {
				right.accept(visitor);
				cast = Utilities.primitiveCastOpcodes(right.getType(), opType);
				for (int i = 0; i < cast.length; i++)
					visitor.visitInsn(cast[i]);
				visitor.visitJumpInsn(Opcodes.IF_ICMPEQ + type, target.getLabel());
			}
		}
		else if (opType == Type.LONG_TYPE) {
			left.accept(visitor);
			int[] cast = Utilities.primitiveCastOpcodes(left.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			right.accept(visitor);
			cast = Utilities.primitiveCastOpcodes(right.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			visitor.visitInsn(Opcodes.LCMP);
			visitor.visitJumpInsn(Opcodes.IFEQ + type, target.getLabel());
		}
		else if (opType == Type.FLOAT_TYPE) {
			left.accept(visitor);
			int[] cast = Utilities.primitiveCastOpcodes(left.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			right.accept(visitor);
			cast = Utilities.primitiveCastOpcodes(right.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			visitor.visitInsn((type == COMPARE_LT || type == COMPARE_LE) ? Opcodes.FCMPL : Opcodes.FCMPG);
			visitor.visitJumpInsn(Opcodes.IFEQ + type, target.getLabel());
		}
		else if (opType == Type.DOUBLE_TYPE) {
			left.accept(visitor);
			int[] cast = Utilities.primitiveCastOpcodes(left.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			right.accept(visitor);
			cast = Utilities.primitiveCastOpcodes(right.getType(), opType);
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
			visitor.visitInsn((type == COMPARE_LT || type == COMPARE_LE) ? Opcodes.DCMPL : Opcodes.DCMPG);
			visitor.visitJumpInsn(Opcodes.IFEQ + type, target.getLabel());
		}
		else
			throw new RuntimeException("WT");
	}
	
	/**
	 * Generate's operator string.
	 */
	public String operator() {
		switch (type) {
			case COMPARE_EQ:
				return "==";
			case COMPARE_NE:
				return "!=";
			case COMPARE_GT:
				return ">";
			case COMPARE_GE:
				return ">=";
			case COMPARE_LT:
				return "<";
			case COMPARE_LE:
				return "<=";
			default:
				return "(unknown operator)";
		}
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("IF ");
		printer.print('(');
		left.print(printer);
		printer.print(" " + operator() + " ");
		right.print(printer);
		printer.print(')');
		printer.tab();
		printer.print("\nGOTO\tl_" + target.getLabel().hashCode());
		printer.untab();
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	
	public LabelAnnotation getTarget() {
		return target;
	}
	
	public void setTarget(LabelAnnotation target) {
		this.target = target;
	}

	public ExpressionNode getLeft() {
		return left;
	}

	public void setLeft(ExpressionNode left) {
		this.left = left;
		overwrite(this.left, 0);
	}

	public ExpressionNode getRight() {
		return right;
	}

	public void setRight(ExpressionNode right) {
		this.right = right;
		overwrite(this.right, 1);
	}

	public int getOperationType() {
		return type;
	}

	public void setOperationType(int type) {
		if (type < COMPARE_EQ || type > COMPARE_LE)
			throw new RuntimeException("WT");
		this.type = type;
	}



}
