package mgi.tools.jtransformer.api.code;


import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

public class CodeNode extends AbstractCodeNode {

	/**
	 * Contains all try catches in this code node.
	 */
	private List<TryCatchInformation> tryCatches = new ArrayList<TryCatchInformation>();
	/**
	 * Contains all line numbers information.
	 */
	private List<LineNumberInformation> lineNumbers = new ArrayList<LineNumberInformation>();
	/**
	 * Contains all local variables information.
	 */
	private List<LocalVariableInformation> localVariables = new ArrayList<LocalVariableInformation>();

	@Override
	public boolean canTrim() {
		return false;
	}
	
	@Override
	public boolean altersLogic() {
		throw new RuntimeException();
	}

	@Override
	public boolean altersFlow() {
		throw new RuntimeException();
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		throw new RuntimeException();
	}
	
	/**
	 * Strip's all debugging information.
	 */
	public void stripDebugInformation() {
		lineNumbers.clear();
		localVariables.clear();
	}
	
	/**
	 * Forward's information about this code contents to given
	 * visitor.
	 */
	@Override
	public void accept(MethodVisitor visitor) {
		visitor.visitCode();
		for (TryCatchInformation tryCatch : tryCatches)
			visitor.visitTryCatchBlock(tryCatch.getStart().getLabel(), tryCatch.getEnd().getLabel(), tryCatch.getHandler().getLabel(), tryCatch.getType());
		for (int addr = 0; read(addr) != null; addr++)
			read(addr).accept(visitor);
		for (LineNumberInformation info : lineNumbers)
			visitor.visitLineNumber(info.getLine(), info.getStart().getLabel());
		for (LocalVariableInformation info : localVariables)
			visitor.visitLocalVariable(info.getName(), info.getDescriptor(), info.getSignature(), info.getStart().getLabel(), info.getEnd().getLabel(), info.getIndex());
		visitor.visitMaxs(0, 0);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.tab();
		printer.print("{");
		for (TryCatchInformation tryCatch : tryCatches)
			printer.print("\n" + tryCatch);
		for (int addr = 0; read(addr) != null; addr++) {
			printer.print('\n');
			read(addr).print(printer);
		}
		printer.untab();
		printer.print("\n}");
	}
	
	public List<TryCatchInformation> getTryCatches() {
		return tryCatches;
	}
	
	public List<LineNumberInformation> getLineNumbers() {
		return lineNumbers;
	}
	
	public List<LocalVariableInformation> getLocalVariables() {
		return localVariables;
	}
	
	@Override
	public String toString() {
		return CodePrinter.print(this);
	}




	
}
