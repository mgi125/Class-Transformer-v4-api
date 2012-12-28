package mgi.tools.jtransformer.api.code.math;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.general.ConstantExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignationExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignationNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class MathematicalVariableAssignationNode extends AbstractCodeNode {
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
	 * Variable type.
	 */
	private Type variableType;
	/**
	 * Type of this expression.
	 */
	private int operationType;
	/**
	 * Index of the variable.
	 */
	private int index;
	/**
	 * Expression of the right side of the mathematical 
	 * operation.
	 */
	private ExpressionNode expression;
	
	public MathematicalVariableAssignationNode(Type variableType, int operationType, int index, ExpressionNode expression) {
		this.variableType = variableType;
		this.operationType = operationType;
		this.index = index;
		this.expression = expression;
		
		overwrite(expression, 0);
		
		if (operationType < TYPE_ADD || operationType > TYPE_XOR)
			throw new RuntimeException("WT");
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
		if (expression.affectedBy(n))
			return true;
		for (int addr = 0; n.read(addr) != null; addr++)
			if (affectedBy(n.read(addr)))
				return true;
		if (n instanceof VariableAssignationNode && ((VariableAssignationNode)n).getIndex() == index)
			return true;
		else if (n instanceof VariableAssignationExpression && ((VariableAssignationExpression)n).getIndex() == index)
			return true;
		else if (n instanceof MathematicalVariableAssignationNode && ((MathematicalVariableAssignationNode)n).getIndex() == index)
			return true;
		else if (n instanceof MathematicalVariableAssignationExpression && ((MathematicalVariableAssignationExpression)n).getIndex() == index)
			return true;
		return false;
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
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

	@Override
	public void accept(MethodVisitor visitor) {
		/*if ((operationType == TYPE_ADD || operationType == TYPE_SUB) && variableType == Type.INT_TYPE && expression instanceof ConstantExpression && ((ConstantExpression)expression).getConstant() instanceof Integer) {
			// optimization for iinc
			visitor.visitIincInsn(index, operationType == TYPE_ADD ? ((Integer)((ConstantExpression)expression).getConstant()).intValue() : -((Integer)((ConstantExpression)expression).getConstant()).intValue());
			visitor.visitVarInsn(OpcodeUtilities.variableLoadOpcode(expression.getType()), index);
			return;
		}*/
		
		Type opType = Utilities.binaryOperationType(variableType, expression.getType());
	
		visitor.visitVarInsn(Utilities.variableLoadOpcode(variableType), index);
		int[] vCast = Utilities.primitiveCastOpcodes(variableType, opType);
		for (int i = 0; i < vCast.length; i++)
			visitor.visitInsn(vCast[i]);
		expression.accept(visitor);
		int[] exprCast = Utilities.primitiveCastOpcodes(expression.getType(), opType);
		for (int i = 0; i < exprCast.length; i++)
			visitor.visitInsn(exprCast[i]);
		
		int opcode;
		switch (operationType) {
			case TYPE_ADD:
				opcode = Utilities.addOpcode(opType);
				break;
			case TYPE_SUB:
				opcode = Utilities.subtractOpcode(opType);
				break;
			case TYPE_MUL:
				opcode = Utilities.multiplyOpcode(opType);
				break;
			case TYPE_DIV:
				opcode = Utilities.divideOpcode(opType);
				break;
			case TYPE_REM:
				opcode = Utilities.remainderOpcode(opType);
				break;
			case TYPE_SHL:
				opcode = Utilities.bitShiftLeftOpcode(opType);
				break;
			case TYPE_SHR:
				opcode = Utilities.bitShiftRightOpcode(opType);
				break;
			case TYPE_USHR:
				opcode = Utilities.bitShiftRightUnsignedOpcode(opType);
				break;
			case TYPE_OR:
				opcode = Utilities.bitOrOpcode(opType);
				break;
			case TYPE_AND:
				opcode = Utilities.bitAndOpcode(opType);
				break;
			case TYPE_XOR:
				opcode = Utilities.bitXorOpcode(opType);
				break;
			default:
				throw new RuntimeException("WT");
		}
		visitor.visitInsn(opcode);
		int[] cast = Utilities.primitiveCastOpcodes(opType, variableType);
		for (int i = 0; i < cast.length; i++)
			visitor.visitInsn(cast[i]);
		visitor.visitVarInsn(Utilities.variableStoreOpcode(variableType), index);
	}

	@Override
	public void print(CodePrinter printer) {
		if (isPlusMinusPostfix()) {
			printer.print("var_" + index + (operationType == TYPE_ADD ? "++" : "--") + ";");
		}
		else {
			printer.print("var_" + index + " " + operator() + " ");
			expression.print(printer);
			printer.print(";");
		}
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	/**
	 * Addon to change x += 1 to ++x and x -= 1 to --x
	 */
	private boolean isPlusMinusPostfix() {
		return (operationType == TYPE_ADD || operationType == TYPE_SUB) && expression instanceof ConstantExpression 
			&& ((ConstantExpression)expression).getConstant() instanceof Number
			&& ((Number)((ConstantExpression)expression).getConstant()).doubleValue() == 1D;
	}

	public void setExpression(ExpressionNode right) {
		this.expression = right;
		overwrite(this.expression, 0);
	}

	public ExpressionNode getExpression() {
		return expression;
	}
	
	public int getOperationType() {
		return operationType;
	}
	
	public void setOperationType(int type) {
		if (type < TYPE_ADD || type > TYPE_XOR)
			throw new RuntimeException("WT");
		this.operationType = type;
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
