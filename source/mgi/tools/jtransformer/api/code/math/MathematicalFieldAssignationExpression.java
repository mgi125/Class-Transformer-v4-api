package mgi.tools.jtransformer.api.code.math;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.general.ConstantExpression;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MathematicalFieldAssignationExpression extends ExpressionNode {
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
	 * Contains instance expression or null if it's 
	 * static field assignation.
	 */
	private ExpressionNode instanceExpression;
	/**
	 * Expression of the right side of the mathematical 
	 * operation.
	 */
	private ExpressionNode expression;
	/**
	 * Type of this operation.
	 */
	private int type;
	/**
	 * Name of the class that owns the field.
	 */
	private String owner;
	/**
	 * Name of the field to store to.
	 */
	private String name;
	/**
	 * Descriptor of the field to store to.
	 */
	private String descriptor;
	
	public MathematicalFieldAssignationExpression(ExpressionNode instanceExpression, ExpressionNode expression, int type, String owner, String name, String descriptor) {
		this.instanceExpression = instanceExpression;
		this.expression = expression;
		this.type = type;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		
		overwrite(instanceExpression, 0);
		overwrite(expression, instanceExpression == null ? 0 : 1);
		
		if (type < TYPE_ADD || type > TYPE_XOR)
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
		return n.altersLogic() || (instanceExpression != null && instanceExpression.affectedBy(n)) || expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		if (instanceExpression != null && addr == 0)
			setInstanceExpression((ExpressionNode)read(0));
		else if (instanceExpression == null && addr == 0)
			setExpression((ExpressionNode)read(0));
		else if (addr == 1)
			setExpression((ExpressionNode)read(1));
	}
	
	
	@Override
	public ExpressionNode copy() {
		return new MathematicalFieldAssignationExpression(instanceExpression != null ? instanceExpression.copy() : null, expression.copy(), type, owner, name, descriptor);
	}

	@Override
	public Type getType() {
		return Type.getType(descriptor);
	}
	
	@Override
	public int getPriority() {
		return isPlusMinusPrefix() ? ExpressionNode.PRIORITY_PLUSMINUSPREFIXPOSTFIX : ExpressionNode.PRIORITY_ASSIGNMENT;
	}
	
	/**
	 * Generate's operator string.
	 */
	public String operator() {
		switch (type) {
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
		boolean instanced = instanceExpression != null;
		if (instanced) {
			instanceExpression.accept(visitor);
			visitor.visitInsn(Utilities.dupOpcode(instanceExpression.getType()));
		}
		visitor.visitFieldInsn(instanced ? Opcodes.GETFIELD : Opcodes.GETSTATIC, owner, name, descriptor);
		expression.accept(visitor);
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
		int[] cast = Utilities.primitiveCastOpcodes(getType(), Type.getType(descriptor));
		for (int i = 0; i < cast.length; i++)
			visitor.visitInsn(cast[i]);
		visitor.visitInsn(instanced ? Utilities.dupXOpcode(expression.getType(), instanceExpression.getType()) : Utilities.dupOpcode(expression.getType()));
		visitor.visitFieldInsn(instanced ? Opcodes.PUTFIELD : Opcodes.PUTSTATIC, owner, name, descriptor);
	}

	@Override
	public void print(CodePrinter printer) {
		
		boolean isPrefix = isPlusMinusPrefix();
		if (isPrefix)
			printer.print(type == TYPE_ADD ? "++" : "--");
		
		if (instanceExpression != null) {
			int selfPriority = ExpressionNode.PRIORITY_MEMBER_ACCESS;
			int basePriority = instanceExpression.getPriority();
			if (basePriority > selfPriority)
				printer.print('(');
			instanceExpression.print(printer);
			if (basePriority > selfPriority)
				printer.print(')');
		}
		else
			printer.print(owner.replace('/', '.'));
		printer.print('.');
		printer.print(name);
		
		if (!isPrefix) {
			printer.print(" " + operator() + " ");
			int selfPriority = getPriority();
			int expressionPriority = expression.getPriority();
			if (expressionPriority > selfPriority)
				printer.print('(');
			expression.print(printer);
			if (expressionPriority > selfPriority)
				printer.print(')');
		}
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	/**
	 * Addon to change x += 1 to ++x and x -= 1 to --x
	 */
	private boolean isPlusMinusPrefix() {
		return (type == TYPE_ADD || type == TYPE_SUB) && expression instanceof ConstantExpression 
			&& ((ConstantExpression)expression).getConstant() instanceof Number
			&& ((Number)((ConstantExpression)expression).getConstant()).doubleValue() == 1D;
	}
	
	public void setInstanceExpression(ExpressionNode instanceExpression) {
		if (this.instanceExpression == null && instanceExpression != null) {
			this.instanceExpression = instanceExpression;
			overwrite(this.expression, 1);
			overwrite(this.instanceExpression, 0);
		}
		else if (this.instanceExpression != null && instanceExpression == null) {
			this.instanceExpression = instanceExpression;
			overwrite(this.expression, 0);
			overwrite(null, 1);
		}
		else {
			this.instanceExpression = instanceExpression;
			overwrite(this.instanceExpression, 0);
		}
	}

	public ExpressionNode getInstanceExpression() {
		return instanceExpression;
	}

	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(this.expression, instanceExpression == null ? 0 : 1);
	}

	public ExpressionNode getExpression() {
		return expression;
	}
	
	public int getOperationType() {
		return type;
	}
	
	public void setOperationType(int type) {
		if (type < TYPE_ADD || type > TYPE_XOR)
			throw new RuntimeException("WT");
		this.type = type;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}



}
