package mgi.tools.jtransformer.api.code.synthetic;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class CaughtExceptionExpression extends ExpressionNode {

	@Override
	public boolean canTrim() {
		return false;
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
		return true;
	}

	@Override
	public ExpressionNode copy() {
		return new CaughtExceptionExpression();
	}

	@Override
	public Type getType() {
		return Type.getType("Ljava/lang/Throwable;");
	}

	@Override
	public void accept(MethodVisitor visitor) {
		// we don't need to write anything here
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("catch()");
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

}
