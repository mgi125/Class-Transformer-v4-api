package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ConstantExpression extends ExpressionNode {
	/**
	 * Constant that this expression is pushing.
	 */
	private Object constant;
	
	public ConstantExpression(Object constant) {
		this.constant = constant;
	}
	
	@Override
	public boolean canTrim() {
		return true;
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
		return false;
	}
	
	@Override
	public ExpressionNode copy() {
		return new ConstantExpression(constant);
	}

	@Override
	public Type getType() {
		if (constant == null)
			return Type.getType("Ljava/lang/Object;");
		else if (constant instanceof Integer) {
			int val = ((Integer)constant).intValue();
			if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE)
				return Type.BYTE_TYPE;
			else if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE)
				return Type.SHORT_TYPE;
			else
				return Type.INT_TYPE;
		}
		else if (constant instanceof Long)
			return Type.LONG_TYPE;
		else if (constant instanceof Float)
			return Type.FLOAT_TYPE;
		else if (constant instanceof Double)
			return Type.DOUBLE_TYPE;
		else if (constant instanceof String)
			return Type.getType("Ljava/lang/String;");
		else if (constant instanceof Type) {
			Type type = (Type)constant;
			if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY)
				return Type.getType("Ljava/lang/Class;");
			else if (type.getSort() == Type.METHOD)
				return Type.getType("Ljava/lang/invoke/MethodType;");
			else
				throw new RuntimeException("WT");
		}
		else if (constant instanceof Handle)
			return Type.getType("Ljava/lang/invoke/MethodHandle;");
		// synthethic values (inserted by optimizations , etc
		else if (constant instanceof Boolean) {
			return Type.BOOLEAN_TYPE;
		}
		else if (constant instanceof Character) {
			return Type.CHAR_TYPE;
		}
		else 
			throw new RuntimeException("WT");
	}

	@Override
	public void accept(MethodVisitor visitor) {
		if (constant == null)
			visitor.visitInsn(Opcodes.ACONST_NULL);
		else if (constant instanceof Integer) {
			int value = ((Integer)constant).intValue();
			if (value >= -1 && value <= 5)
				visitor.visitInsn(Opcodes.ICONST_M1 + (value + 1));
			else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
				visitor.visitIntInsn(Opcodes.BIPUSH, value);
			else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
				visitor.visitIntInsn(Opcodes.SIPUSH, value);
			else
				visitor.visitLdcInsn(value);
		}
		else if (constant instanceof Long) {
			long value = ((Long)constant).longValue();
			if (value == 0L || value == 1L)
				visitor.visitInsn(value == 0L ? Opcodes.LCONST_0 : Opcodes.LCONST_1);
			else
				visitor.visitLdcInsn(value);
		}
		else if (constant instanceof Float) {
			float value = ((Float)constant).floatValue();
			if (value == 0F || value == 1F || value == 2F)
				visitor.visitInsn(Opcodes.FCONST_0 + (int)value);
			else
				visitor.visitLdcInsn(value);
		}
		else if (constant instanceof Double) {
			double value = ((Double)constant).doubleValue();
			if (value == 0D || value == 1D)
				visitor.visitInsn(value == 0 ? Opcodes.DCONST_0 : Opcodes.DCONST_1);
			else
				visitor.visitLdcInsn(value);
		}
		else if (constant instanceof String || constant instanceof Handle || constant instanceof Type) {
			visitor.visitLdcInsn(constant);
		}
		// synthethic values
		else if (constant instanceof Boolean) {
			boolean value = ((Boolean)constant).booleanValue();
			visitor.visitInsn(value ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
		}
		else if (constant instanceof Character) {
			char value = ((Character)constant).charValue();
			if (value >= -1 && value <= 5)
				visitor.visitInsn(Opcodes.ICONST_M1 + (value + 1));
			else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
				visitor.visitIntInsn(Opcodes.BIPUSH, value);
			else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
				visitor.visitIntInsn(Opcodes.SIPUSH, value);
			else
				visitor.visitLdcInsn(value);
		}
		else
			throw new RuntimeException("WT");
	}

	@Override
	public void print(CodePrinter printer) {
		if (constant == null)
			printer.print("null");
		else if (constant instanceof Integer)
			printer.print(((Integer)constant).intValue() + "");
		else if (constant instanceof Long)
			printer.print(((Long)constant).longValue() + "L");
		else if (constant instanceof Float)
			printer.print(((Float)constant).floatValue() + "F");
		else if (constant instanceof Double)
			printer.print(((Double)constant).doubleValue() + "D");
		else if (constant instanceof String)
			printer.print("\"" + constant + "\"");
		else if (constant instanceof Type) {
			Type type = (Type)constant;
			if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY)
				printer.print(type.getClassName() + ".class");
			else if (type.getSort() == Type.METHOD)
				printer.print("methodTypeOf(" + type + ")");
			else
				throw new RuntimeException("WT");
		}
		else if (constant instanceof Handle) {
			printer.print("handleOf(" + constant + ")");
		}
		// synthethic values
		else if (constant instanceof Boolean) {
			printer.print(((Boolean)constant).booleanValue() + "");
		}
		else if (constant instanceof Character) {
			// TODO , normal character printing
			printer.print('\'');
			printer.print(((Character)constant).charValue());
			printer.print('\'');
		}
		else
			throw new RuntimeException("WT");
	}

	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public void setConstant(Object constant) {
		this.constant = constant;
	}

	public Object getConstant() {
		return constant;
	}

}
