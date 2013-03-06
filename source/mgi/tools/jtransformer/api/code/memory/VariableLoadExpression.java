package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.LocalVariableDeclaration;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class VariableLoadExpression extends ExpressionNode {

	/**
	 * Contains declaration of the variable.
	 */
	private LocalVariableDeclaration declaration;
	
	public VariableLoadExpression(LocalVariableDeclaration declaration) {
		this.declaration = declaration;
	}
	
	@Override
	public boolean canTrim() {
		return true;
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
		for (int addr = 0; n.read(addr) != null; addr++)
			if (affectedBy(n.read(addr)))
				return true;
		if (!(n instanceof VariableAssignmentExpression) && !(n instanceof VariableAssignmentNode))
			return false;
		LocalVariableDeclaration declaration = n instanceof VariableAssignmentNode ? ((VariableAssignmentNode)n).getDeclaration() : ((VariableAssignmentExpression)n).getDeclaration();
		if (declaration == this.declaration || declaration.getIndex() == this.declaration.getIndex())
			return true; // index collision
		return false;
	}
	
	@Override
	public Type getType() {
		return declaration.getType();
	}
	
	@Override
	public ExpressionNode copy() {
		return new VariableLoadExpression(declaration);
	}

	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitVarInsn(Utilities.variableLoadOpcode(getType()), declaration.getIndex());
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print(declaration.getName());
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public LocalVariableDeclaration getDeclaration() {
		return declaration;
	}

	public void setDeclaration(LocalVariableDeclaration declaration) {
		this.declaration = declaration;
	}
	




}
