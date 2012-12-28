package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LabelAnnotation extends AbstractCodeNode {

	/**
	 * Label which should be used by
	 * other code nodes.
	 */
	private Label label = new Label();
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		throw new RuntimeException();
	}

	@Override
	public boolean altersFlow() {
		throw new RuntimeException();
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		throw new RuntimeException();
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitLabel(label);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("l_" + label.hashCode() + ":");
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	
	public Label getLabel() {
		return label;
	}

}
