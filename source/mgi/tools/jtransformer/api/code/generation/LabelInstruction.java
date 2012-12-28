package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;

public class LabelInstruction extends AbstractInstruction {
	/**
	 * Label of this instruction.
	 */
	private Label label;
	
	public LabelInstruction(Label label) {
		super(-1);
		this.label = label;
	}

	public Label getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return "l_" + label.hashCode() + ":";
	}
}
