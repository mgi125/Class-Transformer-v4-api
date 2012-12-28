package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;

import org.objectweb.asm.MethodVisitor;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class EnterSubroutineNode extends AbstractCodeNode {

	/**
	 * Jump target.
	 */
	private LabelAnnotation target;
	
	public EnterSubroutineNode(LabelAnnotation target) {
		this.target = target;
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
		return false;
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitJumpInsn(Opcodes.JSR, target.getLabel());
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("JSR\tl_" + target.getLabel().hashCode());
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	
	public LabelAnnotation getTarget() {
		return target;
	}
	
	public void setTarget(LabelAnnotation target) {
		this.target = target;
	}

}
