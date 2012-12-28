package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ArrayLoadExpression extends ExpressionNode {

	public static final int LOAD_TYPE_BYTE = 0;
	public static final int LOAD_TYPE_SHORT = 1;
	public static final int LOAD_TYPE_CHAR = 2;
	public static final int LOAD_TYPE_INT = 3;
	public static final int LOAD_TYPE_LONG = 4;
	public static final int LOAD_TYPE_FLOAT = 5;
	public static final int LOAD_TYPE_DOUBLE = 6;
	public static final int LOAD_TYPE_OBJECT = 7;
	
	private static final Type[] TYPE_TO_STACK = new Type[]
	{
		Type.BYTE_TYPE, Type.SHORT_TYPE, Type.CHAR_TYPE, Type.INT_TYPE,
		Type.LONG_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE, Type.getType("Ljava/lang/Object;")
	};
	
	private static final int[] TYPE_TO_OPCODE = new int[]
    {
		Opcodes.BALOAD, Opcodes.SALOAD, Opcodes.CALOAD, Opcodes.IALOAD,
		Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD,
    };
	
	/**
	 * Contains load type.
	 */
	private int type;
	/**
	 * Expression of array base.
	 */
	private ExpressionNode base;
	/**
	 * Index expression.
	 */
	private ExpressionNode index;
	
	public ArrayLoadExpression(int type, ExpressionNode base, ExpressionNode index) {
		this.type = type;
		this.base = base;
		this.index = index;

		overwrite(base, 0);
		overwrite(index, 1);

		if (type < LOAD_TYPE_BYTE || type > LOAD_TYPE_OBJECT)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return base.canTrim() && index.canTrim();
	}
	
	@Override
	public boolean altersLogic() {
		return base.altersLogic() || index.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return n.altersLogic() || base.affectedBy(n) || index.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		if (addr == 0)
			setBase((ExpressionNode)read(0));
		else if (addr == 1)
			setIndex((ExpressionNode)read(1));
	}
	
	@Override
	public Type getType() {
		return TYPE_TO_STACK[type];
	}
	
	@Override
	public ExpressionNode copy() {
		return new ArrayLoadExpression(type, base.copy(), index.copy());
	}

	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_ARRAY_INDEX;
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		base.accept(visitor);
		index.accept(visitor);
		int[] iCast = Utilities.primitiveCastOpcodes(index.getType(), Type.INT_TYPE);
		for (int i = 0; i < iCast.length; i++)
			visitor.visitInsn(iCast[i]);
		visitor.visitInsn(TYPE_TO_OPCODE[type]);
	}
	

	@Override
	public void print(CodePrinter printer) {
		int selfPriority = getPriority();
		int expressionPriority = base.getPriority();
		if (expressionPriority > selfPriority)
			printer.print('(');
		base.print(printer);
		if (expressionPriority > selfPriority)
			printer.print(')');
		printer.print('[');
		index.print(printer);
		printer.print(']');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public int getLoadType() {
		return type;
	}
	
	public void setLoadType(int type) {
		if (type < LOAD_TYPE_BYTE || type > LOAD_TYPE_OBJECT)
			throw new RuntimeException("WT");
		this.type = type;
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
}
