package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class RawVariableLoadExpression extends ExpressionNode {

	/**
	 * Type of the expression which is being loaded.
	 */
	private Type variableType;
	/**
	 * Index to which this expression was dumped 
	 * instead of stack.
	 */
	private int index;
	
	public RawVariableLoadExpression(Type variableType, int index) {
		this.variableType = variableType;
		this.index = index;
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
		for (int addr = 0; n.read(addr) != null; addr++)
			if (affectedBy(n.read(addr)))
				return true;
		if (n instanceof RawVariableAssignmentNode && ((RawVariableAssignmentNode)n).getIndex() == index)
			return true;
		else if (n instanceof RawVariableAssignmentExpression && ((RawVariableAssignmentExpression)n).getIndex() == index)
			return true;
		return false;
	}
	
	@Override
	public Type getType() {
		return variableType;
	}
	
	@Override
	public ExpressionNode copy() {
		return new RawVariableLoadExpression(variableType, index);
	}

	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitVarInsn(Utilities.variableLoadOpcode(getType()), index);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("var_" + index);
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public Type getVariableType() {
		return variableType;
	}
	
	public void setVariableType(Type variableType) {
		this.variableType = variableType;
	}



}
