package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;

import org.objectweb.asm.MethodVisitor;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class ExitSubroutineNode extends AbstractCodeNode {

	/**
	 * Index of the variable containing address.
	 */
	private int index;
	
	public ExitSubroutineNode(int index) {
		this.index = index;
	}
	
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
		return true;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return n.altersLogic(); // TODO better way: check if n writes the specific index.
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitVarInsn(Opcodes.RET, index);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("RET var_" + index);
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

}
