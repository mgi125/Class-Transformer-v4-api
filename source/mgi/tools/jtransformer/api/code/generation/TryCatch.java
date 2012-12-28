package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;

public class TryCatch {
	/**
	 * Where try scope start.
	 */
	private Label start;
	/**
	 * Where try scope ends.
	 */
	private Label end;
	/**
	 * Where catch handler 
	 * starts.
	 */
	private Label handler;
	/**
	 * Type of the exceptions to catch.
	 */
	private String type;
	
	public TryCatch(Label start, Label end, Label handler,String type) {
		this.start = start;
		this.end = end;
		this.handler = handler;
		this.type = type;
	}

	public Label getStart() {
		return start;
	}

	public Label getEnd() {
		return end;
	}

	public Label getHandler() {
		return handler;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "TryCatch[l_" + start.hashCode() + ",l_" + end.hashCode() + ",l_" + handler.hashCode() + "," + (type == null ? "any" : type) + "]";
	}
}
