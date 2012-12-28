package mgi.tools.jtransformer.api.code.tools;

import java.lang.reflect.Field;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import mgi.tools.jtransformer.api.code.flow.ConditionalJumpNode;
import mgi.tools.jtransformer.api.code.memory.ArrayAssignationExpression;
import mgi.tools.jtransformer.api.code.memory.ArrayLoadExpression;

public class Utilities {
	
	public static String getOpcodeName(int opcode) {
		try {
			Field[] fields = Class.forName("org.objectweb.asm.Opcodes").getFields();
			int count = 0;
			for (Field f : fields) {
				if (f.getGenericType().toString().contains("int") && count >= 48 && f.getInt(null) == opcode)
					return (f.getName());
				count++;
			}
		} catch (Exception e) {
		}
		return "n/a:" + opcode;
	}
	
	public static Type variableLoadType(int opcode) {
		if (opcode == Opcodes.ILOAD)
			return Type.INT_TYPE;
		else if (opcode == Opcodes.LLOAD)
			return Type.LONG_TYPE;
		else if (opcode == Opcodes.FLOAD)
			return Type.FLOAT_TYPE;
		else if (opcode == Opcodes.DLOAD)
			return Type.DOUBLE_TYPE;
		else if (opcode == Opcodes.ALOAD)
			return Type.getType("Ljava/lang/Object;");
		else 
			throw new RuntimeException("WO");
	}
	
	public static int variableLoadOpcode(Type type) {
		if (type.getSort() >= Type.BOOLEAN && type.getSort() <= Type.INT)
			return Opcodes.ILOAD;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LLOAD;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FLOAD;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DLOAD;
		else if (type.getSort() >= Type.ARRAY && type.getSort() <= Type.OBJECT)
			return Opcodes.ALOAD;
		else
			throw new RuntimeException("WT");
	}
	
	public static int variableStoreOpcode(Type type) {
		if (type.getSort() >= Type.BOOLEAN && type.getSort() <= Type.INT)
			return Opcodes.ISTORE;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LSTORE;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FSTORE;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DSTORE;
		else if (type.getSort() >= Type.ARRAY && type.getSort() <= Type.OBJECT)
			return Opcodes.ASTORE;
		else
			throw new RuntimeException("WT");
	}
	
	public static int dupOpcode(Type type) {
		if (type.getSize() == 1)
			return Opcodes.DUP;
		else if (type.getSize() == 2)
			return Opcodes.DUP2;
		else
			throw new RuntimeException("WT");
	}
	
	public static int dupXOpcode(Type dType, Type bType) {
		if (dType.getSize() == 1 && bType.getSize() == 1)
			return Opcodes.DUP_X1;
		else if (dType.getSize() == 1 && bType.getSize() == 2)
			return Opcodes.DUP_X2;
		else if (dType.getSize() == 2 && bType.getSize() == 1)
			return Opcodes.DUP2_X1;
		else if (dType.getSize() == 2 && bType.getSize() == 2)
			return Opcodes.DUP2_X2;
		else
			throw new RuntimeException("WT");
	}
	
	public static int popOpcode(Type type) {
		if (type.getSize() == 1)
			return Opcodes.POP;
		else if (type.getSize() == 2)
			return Opcodes.POP2;
		else
			throw new RuntimeException("WT");
	}
	
	public static int returnOpcode(Type type) {
		if (type.getSort() >= Type.BOOLEAN && type.getSort() <= Type.INT)
			return Opcodes.IRETURN;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LRETURN;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FRETURN;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DRETURN;
		else if (type.getSort() >= Type.ARRAY && type.getSort() <= Type.OBJECT)
			return Opcodes.ARETURN;
		else
			throw new RuntimeException("WT");
	}
	
	public static int addOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IADD;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LADD;
		else if (type == Type.BYTE_TYPE)
			return Opcodes.FADD;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DADD;
		else
			throw new RuntimeException("WT");
	}
	
	public static int subtractOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.ISUB;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LSUB;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FSUB;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DSUB;
		else
			throw new RuntimeException("WT");
	}
	
	public static int multiplyOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IMUL;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LMUL;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FMUL;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DMUL;
		else
			throw new RuntimeException("WT");
	}
	
	public static int divideOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IDIV;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LDIV;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FDIV;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DDIV;
		else
			throw new RuntimeException("WT");
	}
	
	public static int remainderOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IREM;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LREM;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FREM;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DREM;
		else
			throw new RuntimeException("WT");
	}
	
	public static int bitAndOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IAND;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LAND;
		else
			throw new RuntimeException("WT");
	}
	
	public static int bitOrOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IOR;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LOR;
		else
			throw new RuntimeException("WT");
	}
	
	public static int bitXorOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IXOR;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LXOR;
		else
			throw new RuntimeException("WT");
	}
	
	public static int bitShiftLeftOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.ISHL;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LSHL;
		else
			throw new RuntimeException("WT");
	}
	
	public static int bitShiftRightOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.ISHR;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LSHR;
		else
			throw new RuntimeException("WT");
	}
	
	public static int bitShiftRightUnsignedOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.IUSHR;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LUSHR;
		else
			throw new RuntimeException("WT");
	}
	
	public static int[] primitiveCastOpcodes(Type from, Type to) {
		int sortFrom = from.getSort();
		int sortTo = to.getSort();
		
		switch (sortFrom) {
			case Type.BYTE:
				if (sortTo == Type.BOOLEAN || sortTo == Type.BYTE || sortTo == Type.INT)
					return new int[] { };
				else if (sortTo == Type.SHORT)
					return new int[] { Opcodes.I2S };
				else if (sortTo == Type.CHAR)
					return new int[] { Opcodes.I2C };
				else if (sortTo == Type.LONG)
					return new int[] { Opcodes.I2L };
				else if (sortTo == Type.FLOAT)
					return new int[] { Opcodes.I2F };
				else if (sortTo == Type.DOUBLE)
					return new int[] { Opcodes.I2D };
				break;
			case Type.SHORT:
				if (sortTo == Type.BOOLEAN || sortTo == Type.SHORT || sortTo == Type.INT)
					return new int[] { };
				else if (sortTo == Type.BYTE)
					return new int[] { Opcodes.I2B };
				else if (sortTo == Type.CHAR)
					return new int[] { Opcodes.I2C };
				else if (sortTo == Type.LONG)
					return new int[] { Opcodes.I2L };
				else if (sortTo == Type.FLOAT)
					return new int[] { Opcodes.I2F };
				else if (sortTo == Type.DOUBLE)
					return new int[] { Opcodes.I2D };
				break;
			case Type.CHAR:
				if (sortTo == Type.BOOLEAN || sortTo == Type.CHAR || sortTo == Type.INT)
					return new int[] { };
				else if (sortTo == Type.BYTE)
					return new int[] { Opcodes.I2B };
				else if (sortTo == Type.SHORT)
					return new int[] { Opcodes.I2S };
				else if (sortTo == Type.LONG)
					return new int[] { Opcodes.I2L };
				else if (sortTo == Type.FLOAT)
					return new int[] { Opcodes.I2F };
				else if (sortTo == Type.DOUBLE)
					return new int[] { Opcodes.I2D };
				break;
			case Type.BOOLEAN:
			case Type.INT:
				if (sortTo == Type.BOOLEAN || sortTo == Type.INT)
					return new int[] { };
				else if (sortTo == Type.BYTE)
					return new int[] { Opcodes.I2B };
				else if (sortTo == Type.SHORT)
					return new int[] { Opcodes.I2S };
				else if (sortTo == Type.CHAR)
					return new int[] { Opcodes.I2C };
				else if (sortTo == Type.LONG)
					return new int[] { Opcodes.I2L };
				else if (sortTo == Type.FLOAT)
					return new int[] { Opcodes.I2F };
				else if (sortTo == Type.DOUBLE)
					return new int[] { Opcodes.I2D };
				break;
			case Type.LONG:
				if (sortTo == Type.BYTE)
					return new int[] { Opcodes.L2I, Opcodes.I2B };
				else if (sortTo == Type.SHORT)
					return new int[] { Opcodes.L2I, Opcodes.I2S };
				else if (sortTo == Type.CHAR)
					return new int[] { Opcodes.L2I, Opcodes.I2C };
				else if (sortTo == Type.INT || sortTo == Type.BOOLEAN)
					return new int[] { Opcodes.L2I };
				else if (sortTo == Type.LONG)
					return new int[] { };
				else if (sortTo == Type.FLOAT)
					return new int[] { Opcodes.L2F };
				else if (sortTo == Type.DOUBLE)
					return new int[] { Opcodes.L2D };
				break;
			case Type.FLOAT:
				if (sortTo == Type.BYTE)
					return new int[] { Opcodes.F2I, Opcodes.I2B };
				else if (sortTo == Type.SHORT)
					return new int[] { Opcodes.F2I, Opcodes.I2S };
				else if (sortTo == Type.CHAR)
					return new int[] { Opcodes.F2I, Opcodes.I2C };
				else if (sortTo == Type.INT || sortTo == Type.BOOLEAN)
					return new int[] { Opcodes.F2I };
				else if (sortTo == Type.LONG)
					return new int[] { Opcodes.L2F };
				else if (sortTo == Type.FLOAT)
					return new int[] { };
				else if (sortTo == Type.DOUBLE)
					return new int[] { Opcodes.F2D };
				break;
			case Type.DOUBLE:
				if (sortTo == Type.BYTE)
					return new int[] { Opcodes.D2I, Opcodes.I2B };
				else if (sortTo == Type.SHORT)
					return new int[] { Opcodes.D2I, Opcodes.I2S };
				else if (sortTo == Type.CHAR)
					return new int[] { Opcodes.D2I, Opcodes.I2C };
				else if (sortTo == Type.INT || sortTo == Type.BOOLEAN)
					return new int[] { Opcodes.D2I };
				else if (sortTo == Type.LONG)
					return new int[] { Opcodes.D2L };
				else if (sortTo == Type.FLOAT)
					return new int[] { Opcodes.D2F };
				else if (sortTo == Type.DOUBLE)
					return new int[] { };
				break;
		}
		
		throw new RuntimeException("WT");
	}
	
	public static Type primitiveCastType(int opcode) {
		switch (opcode) {
			case Opcodes.I2B:
				return Type.BYTE_TYPE;
			case Opcodes.I2C:
				return Type.CHAR_TYPE;
			case Opcodes.I2S:
				return Type.SHORT_TYPE;
			case Opcodes.L2I:
			case Opcodes.F2I:
			case Opcodes.D2I:
				return Type.INT_TYPE;
			case Opcodes.I2L:
			case Opcodes.F2L:
			case Opcodes.D2L:
				return Type.LONG_TYPE;
			case Opcodes.I2F:
			case Opcodes.L2F:
			case Opcodes.D2F:
				return Type.FLOAT_TYPE;
			case Opcodes.I2D:
			case Opcodes.L2D:
			case Opcodes.F2D:
				return Type.DOUBLE_TYPE;
			default:
				throw new RuntimeException("WT");
		}
	}
	
	public static int arrayLoadType(int opcode) {
		switch (opcode) {
			case Opcodes.BALOAD:
				return ArrayLoadExpression.LOAD_TYPE_BYTE;
			case Opcodes.CALOAD:
				return ArrayLoadExpression.LOAD_TYPE_CHAR;
			case Opcodes.SALOAD:
				return ArrayLoadExpression.LOAD_TYPE_SHORT;
			case Opcodes.IALOAD:
				return ArrayLoadExpression.LOAD_TYPE_INT;
			case Opcodes.LALOAD:
				return ArrayLoadExpression.LOAD_TYPE_LONG;
			case Opcodes.FALOAD:
				return ArrayLoadExpression.LOAD_TYPE_FLOAT;
			case Opcodes.DALOAD:
				return ArrayLoadExpression.LOAD_TYPE_DOUBLE;
			case Opcodes.AALOAD:
				return ArrayLoadExpression.LOAD_TYPE_OBJECT;
			default:
				throw new RuntimeException("WT");
		}
	}
	
	public static int arrayStoreType(int opcode) {
		switch (opcode) {
			case Opcodes.BASTORE:
				return ArrayAssignationExpression.STORE_TYPE_BYTE;
			case Opcodes.CASTORE:
				return ArrayAssignationExpression.STORE_TYPE_CHAR;
			case Opcodes.SASTORE:
				return ArrayAssignationExpression.STORE_TYPE_SHORT;
			case Opcodes.IASTORE:
				return ArrayAssignationExpression.STORE_TYPE_INT;
			case Opcodes.LASTORE:
				return ArrayAssignationExpression.STORE_TYPE_LONG;
			case Opcodes.FASTORE:
				return ArrayAssignationExpression.STORE_TYPE_FLOAT;
			case Opcodes.DASTORE:
				return ArrayAssignationExpression.STORE_TYPE_DOUBLE;
			case Opcodes.AASTORE:
				return ArrayAssignationExpression.STORE_TYPE_OBJECT;
			default:
				throw new RuntimeException("WT");
		}
	}
	
	public static int primitiveArrayOpcode(Type type) {
		switch (type.getElementType().getSort()) {
			case Type.BOOLEAN:
				return Opcodes.T_BOOLEAN;
			case Type.BYTE:
				return Opcodes.T_BYTE;
			case Type.SHORT:
				return Opcodes.T_SHORT;
			case Type.CHAR:
				return Opcodes.T_CHAR;
			case Type.INT:
				return Opcodes.T_INT;
			case Type.LONG:
				return Opcodes.T_LONG;
			case Type.FLOAT:
				return Opcodes.T_FLOAT;
			case Type.DOUBLE:
				return Opcodes.T_DOUBLE;
			default:
				throw new RuntimeException("WT");
		}
	}
	
	public static Type primitiveArrayType(int opcode) {
		switch (opcode) {
			case Opcodes.T_BOOLEAN:
				return Type.getType("[Z");
			case Opcodes.T_BYTE:
				return Type.getType("[B");
			case Opcodes.T_SHORT:
				return Type.getType("[S");
			case Opcodes.T_CHAR:
				return Type.getType("[C");
			case Opcodes.T_INT:
				return Type.getType("[I");
			case Opcodes.T_LONG:
				return Type.getType("[J");
			case Opcodes.T_FLOAT:
				return Type.getType("[F");
			case Opcodes.T_DOUBLE:
				return Type.getType("[D");
			default:
				throw new RuntimeException("WT");
		}
	}
	
	public static int conditionalCompareType(int opcode) {
		switch (opcode) {
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ICMPEQ:
			case Opcodes.IFEQ:
				return ConditionalJumpNode.COMPARE_EQ;
			case Opcodes.IF_ACMPNE:
			case Opcodes.IF_ICMPNE:
			case Opcodes.IFNE:
				return ConditionalJumpNode.COMPARE_NE;
			case Opcodes.IF_ICMPGT:
			case Opcodes.IFGT:
				return ConditionalJumpNode.COMPARE_GT;
			case Opcodes.IF_ICMPGE:
			case Opcodes.IFGE:
				return ConditionalJumpNode.COMPARE_GE;
			case Opcodes.IF_ICMPLT:
			case Opcodes.IFLT:
				return ConditionalJumpNode.COMPARE_LT;
			case Opcodes.IF_ICMPLE:
			case Opcodes.IFLE:
				return ConditionalJumpNode.COMPARE_LE;
			default:
				throw new RuntimeException("WT");
		}
	}
	
	public static int negateOpcode(Type type) {
		if (type == Type.INT_TYPE)
			return Opcodes.INEG;
		else if (type == Type.LONG_TYPE)
			return Opcodes.LNEG;
		else if (type == Type.FLOAT_TYPE)
			return Opcodes.FNEG;
		else if (type == Type.DOUBLE_TYPE)
			return Opcodes.DNEG;
		else
			throw new RuntimeException("WT");
	}
	
	/**
	 * Return's type onto which unary operation should be performed, as defined by:
	 * http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.6.2.
	 */
	public static Type unaryOperationType(Type t1) {
		if (t1.getSort() >= Type.BOOLEAN && t1.getSort() <= Type.INT)
			return Type.INT_TYPE;
		else if (t1 == Type.LONG_TYPE || t1 == Type.FLOAT_TYPE || t1 == Type.DOUBLE_TYPE)
			return t1;
		else
			throw new RuntimeException("WT");
	}
	
	/**
	 * Return's type onto which binary operation should be performed, as defined by:
	 * http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.6.2.
	 */
	public static Type binaryOperationType(Type t1, Type t2) {
		if (isObjectRef(t1) || isObjectRef(t2)) {
			if (isObjectRef(t1) != isObjectRef(t2))
				throw new RuntimeException("WT");
			return Type.getType("Ljava/lang/Object;");
		}
		else if (t1 == Type.DOUBLE_TYPE || t2 == Type.DOUBLE_TYPE)
			return Type.DOUBLE_TYPE;
		else if (t1 == Type.FLOAT_TYPE || t2 == Type.FLOAT_TYPE)
			return Type.FLOAT_TYPE;
		else if (t1 == Type.LONG_TYPE || t2 == Type.LONG_TYPE)
			return Type.LONG_TYPE;
		else if (t1.getSort() >= Type.BOOLEAN && t1.getSort() <= Type.INT && t2.getSort() >= Type.BOOLEAN && t2.getSort() <= Type.INT)
			return Type.INT_TYPE;
		else
			throw new RuntimeException("WT");
	}

	public static boolean isPrimitive(Type type) {
		return type.getSort() >= Type.BOOLEAN && type.getSort() <= Type.DOUBLE;
	}
	
	public static boolean isObjectRef(Type type) {
		return type.getSort() >= Type.ARRAY && type.getSort() <= Type.OBJECT;
	}
}

