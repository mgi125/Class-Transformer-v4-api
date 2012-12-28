package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;

public class LineNumber {
	/**
	 * Line ID.
	 */
	private int line;
	/**
	 * Label where the line started.
	 */
	private Label start;
	
	public LineNumber(int line, Label start) {
		this.line = line;
		this.start = start;
	}
	
	public int getLine() {
		return line;
	}

	public Label getStart() {
		return start;
	}
	
	@Override
	public String toString() {
		return "Line[" + line + "," + "l_" + start.hashCode() + "]";
	}
}
