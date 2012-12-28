package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class LookupSwitchInstruction extends AbstractInstruction {
	/**
	 * Default switch label.
	 */
	private Label defaultLabel;
	/**
	 * Contains cases values.
	 */
	private int[] cases;
	/**
	 * Contains switch targets.
	 */
	private Label[] labels;
	
	public LookupSwitchInstruction(Label defaultLabel, int[] cases, Label[] labels) {
		super(Opcodes.LOOKUPSWITCH);
		this.defaultLabel = defaultLabel;
		this.cases = cases;
		this.labels = labels;
	}

	public Label getDefault() {
		return defaultLabel;
	}
	
	public int[] getCases() {
		return cases;
	}
	
	public Label[] getLabels() {
		return labels;
	}
	
	@Override
	public String toString() {
		return super.toString(); // TODO
	}
}
