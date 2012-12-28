package mgi.tools.jtransformer.api.code.synthetic;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;

import org.objectweb.asm.MethodVisitor;

public class CommentNode extends AbstractCodeNode {

	private String comment;
    
    public CommentNode(String comment) {
    	this.comment = comment;
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
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return false;
	}
    
    public int numLines() {
    	int total = 0;
    	for (int i = 0; i < comment.length(); i++)
    		if (comment.charAt(i) == '\n')
    			total++;
    	return total;
    }

	public String getComment() {
		return comment;
	}

	@Override
	public void print(CodePrinter printer) {
		if (numLines() > 0) {
			printer.print("/* \n * ");
			for (int i = 0; i < comment.length(); i++) {
				if (comment.charAt(i) == '\n')
					printer.print("\n * ");
				else
					printer.print(comment.charAt(i));
			}
			printer.print("\n */");
		}
		else {
			printer.print("// " + comment);
		}
	}

	@Override
	public void accept(MethodVisitor visitor) {
	}

	@Override
	public String toString() {
		return CodePrinter.print(this);
	}



}
