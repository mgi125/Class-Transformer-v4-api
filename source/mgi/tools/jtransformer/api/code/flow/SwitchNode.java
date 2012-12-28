package mgi.tools.jtransformer.api.code.flow;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class SwitchNode extends AbstractCodeNode {
	
	/**
	 * Switch expression.
	 */
	private ExpressionNode expression;
	/**
	 * Contains cases.
	 */
	private int[] cases;
	/**
	 * Contains targets.
	 */
	private LabelAnnotation[] targets;
	/**
	 * Default target.
	 */
	private LabelAnnotation tDefault;
	
	public SwitchNode(ExpressionNode expression, int[] cases, LabelAnnotation[] targets, LabelAnnotation tDefault) {
		this.expression = expression;
		this.cases = cases;
		this.targets = targets;
		this.tDefault = tDefault;
	
		overwrite(expression, 0);
		
		if (cases.length != targets.length)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		return expression.altersLogic();
	}

	@Override
	public boolean altersFlow() {
		return true;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		return expression.affectedBy(n);
	}
	
	@Override
	public void onChildUpdated(int addr) {
		if (addr == 0)
			setExpression((ExpressionNode)read(addr));
	}
	
	@Override
	public void accept(MethodVisitor visitor) {		
		if (needsSort())
			sort();
		
		Label[] labels = new Label[targets.length];
		for (int i = 0; i < labels.length; i++)
			labels[i] = targets[i].getLabel();
		
		expression.accept(visitor);
		int[] cast = Utilities.primitiveCastOpcodes(expression.getType(), Type.INT_TYPE); // widen
		for (int i = 0; i < cast.length; i++)
			visitor.visitInsn(cast[i]);
		boolean fitsIntoTable = fitsIntoTableSwitch();
		if (fitsIntoTable) {
			visitor.visitTableSwitchInsn(cases[0], cases[cases.length - 1], tDefault.getLabel(), labels);
		}
		else {
			visitor.visitLookupSwitchInsn(tDefault.getLabel(), cases, labels);
		}
	}

	@Override
	public void print(CodePrinter printer) {
		if (needsSort())
			sort();
		
		printer.print("SWITCH ");
		printer.print('(');
		expression.print(printer);
		printer.print(')');
		printer.print(" {");
		printer.tab();
		for (int i = 0; i < cases.length; i++) {
			printer.print("\ncase " + cases[i] + ":\n\t GOTO\tl_" + targets[i].getLabel().hashCode());
		}
		printer.print("\ndefault:\n\t GOTO\tl_" + tDefault.getLabel().hashCode());
		printer.untab();
		printer.print("\n}");
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}
	
	private boolean fitsIntoTableSwitch() {
		if (cases.length < 1)
			return false;
		
		for (int i = 1; i < cases.length; i++) {
			if (cases[i] != (cases[i - 1] + 1))
				return false;
		}
		
		return true;
	}
	
	private boolean needsSort() {
		if (cases.length <= 1)
			return false;
		
		for (int i = 1; i < cases.length; i++) {
			if (cases[i - 1] >= cases[i])
				return true;
		}
		
		return false;
	}
	
	private void sort() {
		int[] order = new int[cases.length];
		boolean[] checked = new boolean[cases.length];
		for (int i = 0; i < order.length; i++) {
			int lowest = -1;
			for (int x = 0; x < cases.length; x++) {
				if (!checked[x] && (lowest == -1 || cases[x] < cases[lowest]))
					lowest = x;
			}
			checked[lowest] = true;
			order[i] = lowest;
		}
		
		int[] newCases = new int[cases.length];
		LabelAnnotation[] newTargets = new LabelAnnotation[targets.length];
		for (int i = 0; i < order.length; i++) {
			int id = order[i];
			newCases[id] = cases[id];
			newTargets[id] = targets[id];
		}
		
		cases = newCases;
		targets = newTargets;
	}
	

	public ExpressionNode getExpression() {
		return expression;
	}

	public void setExpression(ExpressionNode expression) {
		this.expression = expression;
		overwrite(this.expression, 0);
	}

	public int[] getCases() {
		return cases;
	}

	public void setCases(int[] cases) {
		this.cases = cases;
	}

	public LabelAnnotation[] getTargets() {
		return targets;
	}

	public void setTargets(LabelAnnotation[] targets) {
		this.targets = targets;
	}

	public LabelAnnotation getDefault() {
		return tDefault;
	}

	public void setDefault(LabelAnnotation tDefault) {
		this.tDefault = tDefault;
	}

}
