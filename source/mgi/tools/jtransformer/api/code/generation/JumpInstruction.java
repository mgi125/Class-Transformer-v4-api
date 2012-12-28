package mgi.tools.jtransformer.api.code.generation;

import org.objectweb.asm.Label;

public class JumpInstruction extends AbstractInstruction {

	/**
	 * Contains target of the jump.
	 */
	private Label target;
	
	public JumpInstruction(int opcode, Label target) {
		super(opcode);
		this.target = target;
	}
	
	public Label getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\tl_" + target.hashCode(); 
	}

}
