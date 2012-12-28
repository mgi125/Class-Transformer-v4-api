package mgi.tools.jtransformer.api.code.math;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.general.ConstantExpression;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MathematicalArrayAssignationExpression extends ExpressionNode {

	public static final int STORE_TYPE_BYTE = 0;
	public static final int STORE_TYPE_SHORT = 1;
	public static final int STORE_TYPE_CHAR = 2;
	public static final int STORE_TYPE_INT = 3;
	public static final int STORE_TYPE_LONG = 4;
	public static final int STORE_TYPE_FLOAT = 5;
	public static final int STORE_TYPE_DOUBLE = 6;
	public static final int STORE_TYPE_OBJECT = 7;
	
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
	
	private static final int[] STORE_OPCODES = new int[]
    {
		Opcodes.BASTORE, Opcodes.SASTORE, Opcodes.CASTORE, Opcodes.IASTORE,
		Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE,
    };
	
	private static final int[] LOAD_OPCODES = new int[]
    {
		Opcodes.BALOAD, Opcodes.SALOAD, Opcodes.CALOAD, Opcodes.IALOAD,
		Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD,
    };
	
	/**
	 * Contains store type.
	 */
	private int storeType;
	/**
	 * Contains operation type.
	 */
	private int operationType;
	/**
	 * Expression of array base.
	 */
	private ExpressionNode base;
	/**
	 * Index expression.
	 */
	private ExpressionNode index;
	/**
	 * Value expression.
	 */
	private ExpressionNode value;
	
	
	public MathematicalArrayAssignationExpression(int storeType, int operationType, ExpressionNode base, ExpressionNode index, ExpressionNode value) {
		this.storeType = storeType;
		this.operationType = operationType;
		this.base = base;
		this.index = index;
		this.value = value;

		overwrite(base, 0);
		overwrite(index, 1);
		overwrite(value, 2);

		if (storeType < STORE_TYPE_BYTE || storeType > STORE_TYPE_OBJECT)
			throw new RuntimeException("WT");
		
		if (operationType < TYPE_ADD || operationType > TYPE_XOR)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return true;
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return n.altersLogic() || base.affectedBy(n) || index.affectedBy(n) || value.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		if (addr == 0)
			setBase((ExpressionNode)read(0));
		else if (addr == 1)
			setIndex((ExpressionNode)read(1));
		else if (addr == 2)
			setValue((ExpressionNode)read(2));
	}
	
	@Override
	public Type getType() {
		return value.getType();
	}
	
	@Override
	public ExpressionNode copy() {
		return new MathematicalArrayAssignationExpression(storeType, operationType, base.copy(), index.copy(), value.copy());
	}

	@Override
	public int getPriority() {
		return isPlusMinusPrefix() ? ExpressionNode.PRIORITY_PLUSMINUSPREFIXPOSTFIX : ExpressionNode.PRIORITY_ASSIGNMENT;
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		base.accept(visitor);
		index.accept(visitor);
		visitor.visitInsn(Opcodes.DUP2);
		visitor.visitInsn(LOAD_OPCODES[storeType]);
		value.accept(visitor);
		int opcode;
		switch (operationType) {
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
		visitor.visitInsn(Utilities.dupXOpcode(getType(), Type.DOUBLE_TYPE)); // double or long doesn't matter we need to get dupX_X2
		visitor.visitInsn(STORE_OPCODES[storeType]);
	}
	

	@Override
	public void print(CodePrinter printer) {
		boolean isPrefix = isPlusMinusPrefix();
		if (isPrefix)
			printer.print(operationType == TYPE_ADD ? "++" : "--");
		
		int accessPriority = ExpressionNode.PRIORITY_ARRAY_INDEX;
		int basePriority = base.getPriority();
		if (basePriority > accessPriority)
			printer.print('(');
		base.print(printer);
		if (basePriority > accessPriority)
			printer.print(')');
		printer.print('[');
		index.print(printer);
		printer.print(']');
		if (!isPrefix) {
			printer.print(" " + operator() + " ");
			int selfPriority = getPriority();
			int expressionPriority = value.getPriority();
			if (expressionPriority > selfPriority)
				printer.print('(');
			value.print(printer);
			if (expressionPriority > selfPriority)
				printer.print(')');
		}
	}
	
	/**
	 * Generate's operator string.
	 */
	public String operator() {
		switch (operationType) {
			case TYPE_ADD:
				return "+=";
			case TYPE_SUB:
				return "-=";
			case TYPE_MUL:
				return "*=";
			case TYPE_DIV:
				return "/=";
			case TYPE_REM:
				return "%=";
			case TYPE_SHL:
				return "<<=";
			case TYPE_SHR:
				return ">>=";
			case TYPE_USHR:
				return ">>>=";
			case TYPE_OR:
				return "|=";
			case TYPE_AND:
				return "&=";
			case TYPE_XOR:
				return "^=";
			default:
				return "(unknown operator)=";
		}
	}
	
	/**
	 * Addon to change x += 1 to ++x and x -= 1 to --x
	 */
	private boolean isPlusMinusPrefix() {
		return (operationType == TYPE_ADD || operationType == TYPE_SUB) && value instanceof ConstantExpression 
			&& ((ConstantExpression)value).getConstant() instanceof Number
			&& ((Number)((ConstantExpression)value).getConstant()).doubleValue() == 1D;
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public int getStoreType() {
		return storeType;
	}
	
	public void setStoreType(int type) {
		if (type < STORE_TYPE_BYTE || type > STORE_TYPE_OBJECT)
			throw new RuntimeException("WT");
		this.storeType = type;
	}
	
	public int getOperationType() {
		return operationType;
	}
	
	public void setOperationType(int type) {
		if (type < TYPE_ADD || type > TYPE_XOR)
			throw new RuntimeException("WT");
		this.operationType = type;
	}

	public ExpressionNode getBase() {
		return base;
	}
	
	public void setBase(ExpressionNode expression) {
		this.base = expression;
		overwrite(expression, 0);
	}
	
	public ExpressionNode getIndex() {
		return index;
	}
	
	public void setIndex(ExpressionNode expression) {
		this.index = expression;
		overwrite(expression, 1);
	}
	
	public ExpressionNode getValue() {
		return value;
	}
	
	public void setValue(ExpressionNode expression) {
		this.value = expression;
		overwrite(expression, 2);
	}
}
