package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class TableSwitchInstruction extends AbstractInstruction {
	/**
	 * Minimum case value.
	 */
	private int minimum;
	/**
	 * Maximum case value.
	 */
	private int maximum;
	/**
	 * Default switch label.
	 */
	private Label defaultLabel;
	/**
	 * Contains switch targets , array
	 * length is maximum-minimum.
	 */
	private Label[] labels;
	
	public TableSwitchInstruction(int minimum, int maximum, Label defaultLabel, Label[] labels) {
		super(Opcodes.TABLESWITCH);
		this.minimum = minimum;
		this.maximum = maximum;
		this.defaultLabel = defaultLabel;
		this.labels = labels;
	}

	public int getMinimum() {
		return minimum;
	}
	
	public int getMaximum() {
		return maximum;
	}
	
	public Label getDefault() {
		return defaultLabel;
	}
	
	public Label[] getLabels() {
		return labels;
	}
	
	@Override
	public String toString() {
		return super.toString(); // TODO
	}
}
