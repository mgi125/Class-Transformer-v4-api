package mgi.tools.jtransformer.api.code;

import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;

public class LineNumberInformation {
	
	private int line;
	private LabelAnnotation start;
	
	public LineNumberInformation(int line, LabelAnnotation start) {
		this.line = line;
		this.start = start;
	}
	
	public int getLine() {
		return line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}
	
	public LabelAnnotation getStart() {
		return start;
	}
	
	public void setStart(LabelAnnotation start) {
		this.start = start;
	}
	
	@Override
	public String toString() {
		return "Line[" + line + "," + "l_" + start.getLabel().hashCode() + "]";
	}
}
