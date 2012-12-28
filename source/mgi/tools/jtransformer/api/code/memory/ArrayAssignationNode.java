package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ArrayAssignationNode extends AbstractCodeNode {

	public static final int STORE_TYPE_BYTE = 0;
	public static final int STORE_TYPE_SHORT = 1;
	public static final int STORE_TYPE_CHAR = 2;
	public static final int STORE_TYPE_INT = 3;
	public static final int STORE_TYPE_LONG = 4;
	public static final int STORE_TYPE_FLOAT = 5;
	public static final int STORE_TYPE_DOUBLE = 6;
	public static final int STORE_TYPE_OBJECT = 7;
	
	private static final Type[] VALUE_TYPES = new Type[]
	{
		Type.BYTE_TYPE, Type.SHORT_TYPE, Type.CHAR_TYPE, Type.INT_TYPE,
		Type.LONG_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE, Type.getType("Ljava/lang/Object;")
	};
	
	private static final int[] STORE_OPCODES = new int[]
    {
		Opcodes.BASTORE, Opcodes.SASTORE, Opcodes.CASTORE, Opcodes.IASTORE,
		Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE,
    };
	
	/**
	 * Contains store type.
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
	/**
	 * Value expression.
	 */
	private ExpressionNode value;
	
	
	public ArrayAssignationNode(int type, ExpressionNode base, ExpressionNode index, ExpressionNode value) {
		this.type = type;
		this.base = base;
		this.index = index;
		this.value = value;

		overwrite(base, 0);
		overwrite(index, 1);
		overwrite(value, 2);

		if (type < STORE_TYPE_BYTE || type > STORE_TYPE_OBJECT)
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
	public void accept(MethodVisitor visitor) {
		base.accept(visitor);
		index.accept(visitor);
		int[] iCast = Utilities.primitiveCastOpcodes(index.getType(), Type.INT_TYPE); // widen
		for (int i = 0; i < iCast.length; i++)
			visitor.visitInsn(iCast[i]);
		value.accept(visitor);
		if (Utilities.isPrimitive(VALUE_TYPES[type])) {
			int[] vCast = Utilities.primitiveCastOpcodes(value.getType(), VALUE_TYPES[type]);
			for (int i = 0; i < vCast.length; i++)
				visitor.visitInsn(vCast[i]);
		}
		visitor.visitInsn(STORE_OPCODES[type]);
	}
	

	@Override
	public void print(CodePrinter printer) {
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
		printer.print(" = ");
		value.print(printer);
		printer.print(';');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public int getStoreType() {
		return type;
	}
	
	public void setStoreType(int type) {
		if (type < STORE_TYPE_BYTE || type > STORE_TYPE_OBJECT)
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
	
	public ExpressionNode getValue() {
		return value;
	}
	
	public void setValue(ExpressionNode expression) {
		this.value = expression;
		overwrite(expression, 2);
	}
}
