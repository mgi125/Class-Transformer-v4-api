package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Opcodes;

public class Constants {

	private static final int POSTFIX_INC = 0;
	private static final int POSTFIX_DEC = 1;
	
	public static final int[] INSTANCED_FIELD_POSTFIX_TYPES = new int[]
	{
		POSTFIX_INC, // i+
		POSTFIX_DEC, // i-
		POSTFIX_INC, // l+
		POSTFIX_DEC, // l-
		POSTFIX_INC, // f+
		POSTFIX_DEC, // f-
		POSTFIX_INC, // d+
		POSTFIX_DEC, // d-
	};
	
	public static final int[][] INSTANCED_FIELD_POSTFIX = new int[][]
	{
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP_X1,
			Opcodes.ICONST_1,
			Opcodes.IADD,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP_X1,
			Opcodes.ICONST_1,
			Opcodes.ISUB,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP2_X1,
			Opcodes.LCONST_1,
			Opcodes.LADD,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP2_X1,
			Opcodes.LCONST_1,
			Opcodes.LSUB,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP_X1,
			Opcodes.FCONST_1,
			Opcodes.FADD,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP_X1,
			Opcodes.FCONST_1,
			Opcodes.FSUB,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP2_X1,
			Opcodes.DCONST_1,
			Opcodes.DADD,
			Opcodes.PUTFIELD,
		},
		{
			Opcodes.DUP,
			Opcodes.GETFIELD,
			Opcodes.DUP2_X1,
			Opcodes.DCONST_1,
			Opcodes.DSUB,
			Opcodes.PUTFIELD,
		},
	};
	
	public static final int[][] ARRAY_POSTFIX = new int[][]
	{
		{
			Opcodes.DUP2,
			Opcodes.BALOAD,
			Opcodes.DUP_X2,
			Opcodes.ICONST_1,
			
		}
	};
}
