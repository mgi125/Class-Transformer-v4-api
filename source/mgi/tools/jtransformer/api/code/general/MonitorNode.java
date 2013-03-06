package mgi.tools.jtransformer.api.code.general;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MonitorNode extends AbstractCodeNode {
	public static final int TYPE_MONITORENTER = 0;
	public static final int TYPE_MONITOREXIT = 1;
	
	/**
	 * Expression of object which is going to be
	 * monitored.
	 */
	private ExpressionNode expression;
	/**
	 * Type of the monitor.
	 */
	private int type;
	
	public MonitorNode(ExpressionNode expr, int type) {
		this.expression = expr;
		this.type = type;
		
		overwrite(expression, 0);
	
		if (type < TYPE_MONITORENTER || type > TYPE_MONITOREXIT)
			throw new RuntimeException("WT");
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
		return n.altersLogic() || expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		setExpression((ExpressionNode)read(addr));
	}
	
	@Override
	public void accept(MethodVisitor visitor) {
		expression.accept(visitor);
		visitor.visitInsn(type == TYPE_MONITORENTER ? Opcodes.MONITORENTER : Opcodes.MONITOREXIT);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print(type == TYPE_MONITORENTER ? "MONITORENTER" : "MONITOREXIT");
		printer.print('(');
		expression.print(printer);
		printer.print(')');
		printer.print(';');
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}


	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(this.expression, 0);
	}

	public ExpressionNode getExpression() {
		return expression;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		if (type < TYPE_MONITORENTER || type > TYPE_MONITOREXIT)
			throw new RuntimeException("WT");
		this.type = type;
	}
	
	

}
