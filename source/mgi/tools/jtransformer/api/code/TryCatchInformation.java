package mgi.tools.jtransformer.api.code;

import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;

public class TryCatchInformation {
	/**
	 * Label from which try block starts.
	 */
	private LabelAnnotation start;
	/**
	 * Label before which try block ended.
	 */
	private LabelAnnotation end;
	/**
	 * Label at which code will continue
	 * if exception was caught.
	 */
	private LabelAnnotation handler;
	/**
	 * Type of the exceptions to catch,
	 * may be null for all types of exceptions.
	 */
	private String type;
	
	public TryCatchInformation(LabelAnnotation start, LabelAnnotation end, LabelAnnotation handler, String type) {
		this.start = start;
		this.end = end;
		this.handler = handler;
		this.type = type;
	}

	public void setStart(LabelAnnotation start) {
		this.start = start;
	}

	public LabelAnnotation getStart() {
		return start;
	}

	public void setEnd(LabelAnnotation end) {
		this.end = end;
	}

	public LabelAnnotation getEnd() {
		return end;
	}

	public void setHandler(LabelAnnotation handler) {
		this.handler = handler;
	}

	public LabelAnnotation getHandler() {
		return handler;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "TryCatch[l_" + start.getLabel().hashCode() + ",l_" + end.getLabel().hashCode() + ",l_" + handler.getLabel().hashCode() + "," + type + "]";
	}
}
