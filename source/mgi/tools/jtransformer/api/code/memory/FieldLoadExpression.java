package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.Type;

public class FieldLoadExpression extends ExpressionNode {
	/**
	 * Expression of the field base instance.
	 */
	private ExpressionNode instanceExpression;
	/**
	 * Name of the class that owns this field.
	 */
	private String owner;
	/**
	 * Name of the field.
	 */
	private String name;
	/**
	 * Descriptor of the field.
	 */
	private String descriptor;
	
	public FieldLoadExpression(ExpressionNode instanceExpression, String owner,String name,String descriptor) {
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		
		overwrite(instanceExpression, 0);
	}
	
	@Override
	public boolean canTrim() {
		return instanceExpression != null ? instanceExpression.canTrim() : true;
	}
	
	@Override
	public boolean altersLogic() {
		return instanceExpression != null && instanceExpression.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return n.altersLogic() || (instanceExpression != null && instanceExpression.affectedBy(n));
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setInstanceExpression((ExpressionNode)read(addr));
	}
	
	@Override
	public ExpressionNode copy() {
		return new FieldLoadExpression(instanceExpression != null ? instanceExpression.copy() : null, owner, name, descriptor);
	}
	
	@Override
	public Type getType() {
		return Type.getType(descriptor);
	}
	
	@Override
	public int getPriority() {
		return instanceExpression != null ? ExpressionNode.PRIORITY_MEMBER_ACCESS : 0;
	}

	@Override
	public void accept(MethodVisitor visitor) {
		if (instanceExpression != null) {
			instanceExpression.accept(visitor);
		}
		visitor.visitFieldInsn(instanceExpression != null ? Opcodes.GETFIELD : Opcodes.GETSTATIC, owner, name, descriptor);
	}

	@Override
	public void print(CodePrinter printer) {
		if (instanceExpression != null) {
			int selfPriority = getPriority();
			int basePriority = instanceExpression.getPriority();
			if (basePriority > selfPriority)
				printer.print('(');
			instanceExpression.print(printer);
			if (basePriority > selfPriority)
				printer.print(')');
		}
		else
			printer.print(owner.replace('/', '.'));
		printer.print('.');
		printer.print(name);
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}

	public void setInstanceExpression(ExpressionNode instanceExpression) {
		this.instanceExpression = instanceExpression;
		overwrite(this.instanceExpression, 0);
	}

	public ExpressionNode getInstanceExpression() {
		return instanceExpression;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}
	


}
