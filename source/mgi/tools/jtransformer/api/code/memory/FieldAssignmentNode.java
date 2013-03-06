package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class FieldAssignmentNode extends AbstractCodeNode {

	/**
	 * Instance expression which represends 
	 * base of this field class.
	 */
	private ExpressionNode instanceExpression;
	/**
	 * Expression which is being stored.
	 */
	private ExpressionNode expression;
	/**
	 * Name of the class that owns this field.
	 */
	private String owner;
	/**
	 * Name of the field to store to.
	 */
	private String name;
	/**
	 * Descriptor of the field to store to.
	 */
	private String descriptor;
	
	public FieldAssignmentNode(ExpressionNode instanceExpression, ExpressionNode expr, String owner, String name, String descriptor) {
		this.instanceExpression = instanceExpression;
		this.expression = expr;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		
		overwrite(instanceExpression, 0);
		overwrite(expr, instanceExpression == null ? 0 : 1);
		
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return true;
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return n.altersLogic() || (instanceExpression != null && instanceExpression.affectedBy(n)) || expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		if (instanceExpression != null && addr == 0)
			setInstanceExpression((ExpressionNode)read(0));
		else if (instanceExpression == null && addr == 0)
			setExpression((ExpressionNode)read(0));
		else if (addr == 1)
			setExpression((ExpressionNode)read(1));
	}

	@Override
	public void accept(MethodVisitor visitor) {
		if (instanceExpression != null)
			instanceExpression.accept(visitor);
		expression.accept(visitor);
		if (Utilities.isPrimitive(Type.getType(descriptor))) {
			int[] cast = Utilities.primitiveCastOpcodes(expression.getType(), Type.getType(descriptor));
			for (int i = 0; i < cast.length; i++)
				visitor.visitInsn(cast[i]);
		}
		visitor.visitFieldInsn(instanceExpression != null ? Opcodes.PUTFIELD : Opcodes.PUTSTATIC, owner, name, descriptor);
	}

	@Override
	public void print(CodePrinter printer) {
		if (instanceExpression != null) {
			int selfPriority = ExpressionNode.PRIORITY_MEMBER_ACCESS;
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
		printer.print(" = ");
		expression.print(printer);
		printer.print(';');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	public void setInstanceExpression(ExpressionNode instanceExpression) {
		if (this.instanceExpression == null && instanceExpression != null) {
			this.instanceExpression = instanceExpression;
			overwrite(this.expression, 1);
			overwrite(this.instanceExpression, 0);
		}
		else if (this.instanceExpression != null && instanceExpression == null) {
			this.instanceExpression = instanceExpression;
			overwrite(this.expression, 0);
			overwrite(null, 1);
		}
		else {
			this.instanceExpression = instanceExpression;
			overwrite(this.instanceExpression, 0);
		}
	}

	public ExpressionNode getInstanceExpression() {
		return instanceExpression;
	}

	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(this.expression, instanceExpression == null ? 0 : 1);
	}

	public ExpressionNode getExpression() {
		return expression;
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
