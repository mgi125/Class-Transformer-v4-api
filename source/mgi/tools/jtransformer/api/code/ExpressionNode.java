package mgi.tools.jtransformer.api.code;

import org.objectweb.asm.Type;

public abstract class ExpressionNode extends AbstractCodeNode {

	public static final int PRIORITY_STANDART = 0;
	public static final int PRIORITY_ARRAY_INDEX = 1;
	public static final int PRIORITY_CALL = 1;
	public static final int PRIORITY_MEMBER_ACCESS = 1;
	public static final int PRIORITY_UNARYPLUSMINUS = 2;
	public static final int PRIORITY_PLUSMINUSPREFIXPOSTFIX = 2;
	public static final int PRIORITY_UNARYLOGICALNOT = 2;
	public static final int PRIORITY_UNARYBITWISENOT = 2;
	public static final int PRIORITY_CAST = 2;
	public static final int PRIORITY_NEWOPERATOR = 2;
	public static final int PRIORITY_MULDIVREM = 3;
	public static final int PRIORITY_ADDSUB = 4;
	public static final int PRIORITY_CONTACTSTRING = 4;
	public static final int PRIORITY_BITSHIFTS = 5;
	public static final int PRIORITY_LELTGEGTINSTANCEOF = 6;
	public static final int PRIORITY_EQNE = 7;
	public static final int PRIORITY_BITAND = 8;
	public static final int PRIORITY_BITXOR = 9;
	public static final int PRIORITY_BITOR = 10;
	public static final int PRIORITY_LOGICALAND = 11;
	public static final int PRIORITY_LOGICALOR = 12;
	public static final int PRIORITY_TERNARY = 13;
	public static final int PRIORITY_ASSIGNMENT = 14;
	
    
    /**
     * Return's type of this expression.
     */
    public abstract Type getType();
    
    /**
     * Copies this expression node.
     */
    public abstract ExpressionNode copy();
    
    /**
     * Get's priority of this expression.
     * This method does return default priority:0
     */
    public int getPriority() {
    	return 0;
    }

}
