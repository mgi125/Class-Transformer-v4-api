package mgi.tools.jtransformer.api.code.generation;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jtransformer.api.ClassNode;
import mgi.tools.jtransformer.api.MethodNode;
import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodeNode;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.LineNumberInformation;
import mgi.tools.jtransformer.api.code.LocalVariableInformation;
import mgi.tools.jtransformer.api.code.TryCatchInformation;
import mgi.tools.jtransformer.api.code.flow.ConditionalJumpNode;
import mgi.tools.jtransformer.api.code.flow.LabelAnnotation;
import mgi.tools.jtransformer.api.code.flow.ReturnNode;
import mgi.tools.jtransformer.api.code.flow.SwitchNode;
import mgi.tools.jtransformer.api.code.flow.ThrowNode;
import mgi.tools.jtransformer.api.code.flow.UnconditionalJumpNode;
import mgi.tools.jtransformer.api.code.general.CastExpression;
import mgi.tools.jtransformer.api.code.general.ConstantExpression;
import mgi.tools.jtransformer.api.code.general.InstanceofExpression;
import mgi.tools.jtransformer.api.code.general.InvokeExpression;
import mgi.tools.jtransformer.api.code.general.LowLevelCompareExpression;
import mgi.tools.jtransformer.api.code.general.MonitorNode;
import mgi.tools.jtransformer.api.code.general.PopableNode;
import mgi.tools.jtransformer.api.code.math.MathematicalExpression;
import mgi.tools.jtransformer.api.code.math.NegateExpression;
import mgi.tools.jtransformer.api.code.memory.ArrayAssignationNode;
import mgi.tools.jtransformer.api.code.memory.ArrayLengthExpression;
import mgi.tools.jtransformer.api.code.memory.ArrayLoadExpression;
import mgi.tools.jtransformer.api.code.memory.FieldAssignationNode;
import mgi.tools.jtransformer.api.code.memory.FieldLoadExpression;
import mgi.tools.jtransformer.api.code.memory.NewUninitializedArrayExpression;
import mgi.tools.jtransformer.api.code.memory.NewUninitializedObjectExpression;
import mgi.tools.jtransformer.api.code.memory.VariableAssignationNode;
import mgi.tools.jtransformer.api.code.memory.VariableLoadExpression;
import mgi.tools.jtransformer.api.code.synthetic.CaughtExceptionExpression;
import mgi.tools.jtransformer.api.code.synthetic.CommentNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;
import mgi.tools.jtransformer.utilities.ArrayQueue;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.Type;

public class CodeGenerator extends MethodVisitor {

	/**
	 * Method for which we are generating the code.
	 */
	private MethodNode parent;
	/**
	 * Contains all instructions.
	 */
	private AbstractInstruction[] instructions;
	/**
	 * Contains instructions write buffer.
	 */
	private ArrayList<AbstractInstruction> instructionsBuffer;
	/**
	 * Contains all try catches.
	 */
	private ArrayList<TryCatch> tryCatches;
	/**
	 * Contains all line numbers.
	 */
	private ArrayList<LineNumber> lineNumbers;
	/**
	 * Contains all local variables.
	 */
	private ArrayList<LocalVariable> localVariables;
	/**
	 * Contains all code blocks.
	 */
	private Block[] blocks;
	/**
	 * Contains procession queue.
	 */
	private ArrayQueue<Block> queue;
	/**
	 * Contains list of blocks that were already processed.
	 */
	private List<Block> processedBlocks;
	/**
	 * Contains first free local variable slot in use for 
	 * stack dumps.
	 */
	private int stackDumpsStartAddress;
	/**
	 * Contains first free local variable slot in use for
	 * for other information or new local variables.
	 */
	private int miscDumpsStartAddress;
	
	public CodeGenerator(MethodNode parent) {
		super(Opcodes.ASM4);
		this.parent = parent;
	}
	
	
	@Override
	public void visitCode() {
		if (instructions != null || instructionsBuffer != null)
			throw new RuntimeException("Illegal use.");
		instructionsBuffer = new ArrayList<AbstractInstruction>();
		tryCatches = new ArrayList<TryCatch>();
		lineNumbers = new ArrayList<LineNumber>();
		localVariables = new ArrayList<LocalVariable>();
		visitLabel(new Label()); // main label
	}
	
	@Override
	public void visitInsn(int opcode) {
		instructionsBuffer.add(new Instruction(opcode));
	}
	
	@Override
	public void visitIntInsn(int opcode, int operand) {
		instructionsBuffer.add(new IntInstruction(opcode,operand));
	}
	
	@Override
	public void visitVarInsn(int opcode, int index) {
		instructionsBuffer.add(new VariableInstruction(opcode,index));
	}
	
	@Override
	public void visitTypeInsn(int opcode, String desc) {
		instructionsBuffer.add(new TypeInstruction(opcode,desc));
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		instructionsBuffer.add(new FieldInstruction(opcode,owner,name,desc));
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		instructionsBuffer.add(new MethodInstruction(opcode,owner,name,desc));
	}
	
	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		instructionsBuffer.add(new DynamicMethodInstruction(name,desc,bsm,bsmArgs));
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label target) {
		instructionsBuffer.add(new JumpInstruction(opcode,target));
	}
	
	@Override
	public void visitLabel(Label label) {
		instructionsBuffer.add(new LabelInstruction(label));
	}
	
	@Override
	public void visitLdcInsn(Object cst) {
		instructionsBuffer.add(new LoadConstantInstruction(cst));
	}
	
	@Override
	public void visitIincInsn(int index, int incrementor) {
		instructionsBuffer.add(new IntIncrementInstruction(index,incrementor));
	}
	
	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		instructionsBuffer.add(new TableSwitchInstruction(min,max,dflt,labels));
	}
	
	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		instructionsBuffer.add(new LookupSwitchInstruction(dflt,keys,labels));
	}
	
	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		instructionsBuffer.add(new MultiArrayInstruction(desc,dims));
	}
	
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,String type) {
		tryCatches.add(new TryCatch(start,end,handler,type));
	}
	
	@Override
	public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
		localVariables.add(new LocalVariable(name,descriptor,signature,start,end,index));
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		lineNumbers.add(new LineNumber(line,start));
	}
	
	@Override
	public void visitMaxs(int ms, int ml) {
		instructions = instructionsBuffer.toArray(new AbstractInstruction[0]);
		instructionsBuffer = null; // dispose
		prepareGeneration(ms, ml);
		processGeneration();
		performTransformations();
		writeCode();
		instructions = null;
		processedBlocks = null;
		queue = null;
		tryCatches = null;
		lineNumbers = null;
		localVariables = null;
		blocks = null;
	}
	
	
	/**
	 * Prepare's for code generation process.
	 */
	private void prepareGeneration(int maxstack, int maxlocals) {
		int numBlocks = 0;
		for (AbstractInstruction instr : instructions)
			if (instr instanceof LabelInstruction)
				numBlocks++;
		calculateSlots(maxstack, maxlocals);
		processedBlocks = new ArrayList<Block>();
		queue = new ArrayQueue<Block>(10);
		blocks = new Block[numBlocks];
		blocks[0] = new Block(0,new Stack(maxstack + 2));
		
		queue.insert(blocks[0]);
	}

	/**
	 * Generates high level code from low level instructions.
	 */
	private void processGeneration() {	
		// TODO better trycatches handling
		for (TryCatch tryCatch : tryCatches) {
			int startID = blockID(tryCatch.getStart());
			int endID = blockID(tryCatch.getEnd());
			int handlerID = blockID(tryCatch.getHandler());
			if (blocks[startID] == null)
				blocks[startID] = new Block(labelAddress(tryCatch.getStart()));
			if (blocks[endID] == null)
				blocks[endID] = new Block(labelAddress(tryCatch.getEnd()));
			if (blocks[handlerID] == null) {
				Stack stack = new Stack();
				stack.push(new CaughtExceptionExpression());
				blocks[handlerID] = new Block(labelAddress(tryCatch.getHandler()),stack);
				queue.insert(blocks[handlerID]);
			}
			parent.getCode().getTryCatches().add(new TryCatchInformation(blocks[startID].getAnnotation(),blocks[endID].getAnnotation(),blocks[handlerID].getAnnotation(),tryCatch.getType()));
		}
		
		while (queue.size() > 0) {
			Block block = queue.take();
			if (processedBlocks.contains(block))
				continue;
			processedBlocks.add(block);
			processBlock(block);
		}
		
		for (LineNumber number : lineNumbers) {
			int id = blockID(number.getStart());
			if (blocks[id] == null)
				blocks[id] = new Block(labelAddress(number.getStart()));
			parent.getCode().getLineNumbers().add(new LineNumberInformation(id,blocks[id].getAnnotation()));
		}
		
		for (LocalVariable variable : localVariables) {
			int startID = blockID(variable.getStart());
			int endID = blockID(variable.getEnd());
			if (blocks[startID] == null)
				blocks[startID] = new Block(labelAddress(variable.getStart()));
			if (blocks[endID] == null)
				blocks[endID] = new Block(labelAddress(variable.getEnd()));
			parent.getCode().getLocalVariables().add(new LocalVariableInformation(variable.getName(), variable.getDescriptor(), variable.getSignature(), blocks[startID].getAnnotation(), blocks[endID].getAnnotation(), variable.getIndex()));
		}
	}
	
	/**
	 * Perform's various transformations to simplify the code.
	 */
	private void performTransformations() {
	}
	
	
	/**
	 * Processes given block.
	 */
	private void processBlock(Block block) {
		int ptr = block.getStartAddress() + 1;
		Stack stack = block.getEntryStack().copy();
		
		for (;;ptr++) {
			if (ptr >= instructions.length)
				throw new RuntimeException("Code goes outside it's bounds");
			AbstractInstruction instruction = instructions[ptr];
			//AbstractInstruction next  = (ptr + 1) < instructions.length ? instructions[ptr + 1] : null;
			//AbstractInstruction next2 = (ptr + 2) < instructions.length ? instructions[ptr + 2] : null; 
			//AbstractInstruction prev  = (ptr - 1) >= 0				    ? instructions[ptr - 1] : null;
			//AbstractInstruction prev2 = (ptr - 2) >= 0                  ? instructions[ptr - 2] : null;
			int op = instruction.getOpcode();
			if (instruction instanceof LabelInstruction) {
				dumpStack(block, stack);
				generateBlock(((LabelInstruction)instruction).getLabel(), stack.copy());
				break;
			}
			else if (op == Opcodes.GOTO) {
				dumpStack(block, stack);
				Block target = generateBlock(((JumpInstruction)instruction).getTarget(), stack.copy());
				block.getCode().add(new UnconditionalJumpNode(target.getAnnotation()));
				break;
			}
			else if (op == Opcodes.JSR || op == Opcodes.RET) {
				throw new RuntimeException("JSR/RET not yet implemented.");
			}
			else if (op == Opcodes.IFNULL || op == Opcodes.IFNONNULL) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				Block target = generateBlock(((JumpInstruction)instruction).getTarget(), stack.copy());
				block.getCode().add(new ConditionalJumpNode(expr, new ConstantExpression(null), op == Opcodes.IFNULL ? ConditionalJumpNode.COMPARE_EQ : ConditionalJumpNode.COMPARE_NE, target.getAnnotation()));
			}
			else if (op >= Opcodes.IF_ICMPEQ && op <= Opcodes.IF_ACMPNE) {
				if (stack.getSize() > 2)
					dumpStack(block, stack);
				ExpressionNode right = stack.pop();
				ExpressionNode left = stack.pop();
				Block target = generateBlock(((JumpInstruction)instruction).getTarget(), stack.copy());
				block.getCode().add(new ConditionalJumpNode(left, right, Utilities.conditionalCompareType(op), target.getAnnotation()));
			}
			else if (op >= Opcodes.IFEQ && op <= Opcodes.IFLE) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode left = stack.pop();
				Block target = generateBlock(((JumpInstruction)instruction).getTarget(), stack.copy());
				block.getCode().add(new ConditionalJumpNode(left, new ConstantExpression(0), Utilities.conditionalCompareType(op), target.getAnnotation()));
			}
			else if (op >= Opcodes.LCMP && op <= Opcodes.DCMPG) {
				ExpressionNode right = stack.pop();
				ExpressionNode left = stack.pop();
				stack.push(new LowLevelCompareExpression(left, right, (op == Opcodes.FCMPG || op == Opcodes.DCMPG) ? LowLevelCompareExpression.TYPE_CMPG : LowLevelCompareExpression.TYPE_CMPL));
			}
			else if (op == Opcodes.TABLESWITCH) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				TableSwitchInstruction instr = (TableSwitchInstruction)instruction;
				ExpressionNode expr = stack.pop();
				LabelAnnotation[] targets = new LabelAnnotation[instr.getLabels().length];
				int[] cases = new int[instr.getLabels().length];
				for (int i = 0; i < cases.length; i++)
					cases[i] = instr.getMinimum() + i;
				for (int i = 0; i < targets.length; i++)
					targets[i] = generateBlock(instr.getLabels()[i], stack.copy()).getAnnotation();
				LabelAnnotation tDefault = generateBlock(instr.getDefault(), stack.copy()).getAnnotation();
				block.getCode().add(new SwitchNode(expr, cases, targets, tDefault));
				break;
			}
			else if (op == Opcodes.LOOKUPSWITCH) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				LookupSwitchInstruction instr = (LookupSwitchInstruction)instruction;
				ExpressionNode expr = stack.pop();
				LabelAnnotation[] targets = new LabelAnnotation[instr.getLabels().length];
				for (int i = 0; i < targets.length; i++)
					targets[i] = generateBlock(instr.getLabels()[i], stack.copy()).getAnnotation();
				LabelAnnotation tDefault = generateBlock(instr.getDefault(), stack.copy()).getAnnotation();
				block.getCode().add(new SwitchNode(expr, instr.getCases(), targets, tDefault));
				break;
			}
			else if (op == Opcodes.LDC)
				stack.push(new ConstantExpression(((LoadConstantInstruction)instruction).getConstant()));
			else if (op == Opcodes.ACONST_NULL)
				stack.push(new ConstantExpression(null));
			else if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5)
				stack.push(new ConstantExpression((op - Opcodes.ICONST_M1) - 1));
			else if (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH)
				stack.push(new ConstantExpression(((IntInstruction)instruction).getOperand()));
			else if (op == Opcodes.LCONST_0 || op == Opcodes.LCONST_1)
				stack.push(new ConstantExpression(op == Opcodes.LCONST_0 ? 0L : 1L));
			else if (op >= Opcodes.FCONST_0 && op <= Opcodes.FCONST_2)
				stack.push(new ConstantExpression((float)(op - Opcodes.FCONST_0)));
			else if (op == Opcodes.DCONST_0 || op == Opcodes.DCONST_1)
				stack.push(new ConstantExpression(op == Opcodes.DCONST_0 ? 0D : 1D));
			else if (op >= Opcodes.IRETURN && op <= Opcodes.ARETURN) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				block.getCode().add(new ReturnNode(Type.getReturnType(parent.getDescriptor()), expr));
				break;
			}
			else if (op == Opcodes.RETURN) {
				dumpStack(block,stack);
				block.getCode().add(new ReturnNode());
				break;
			}
			else if (op == Opcodes.ATHROW) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				block.getCode().add(new ThrowNode(expr));
				break;
			}
			else if (op == Opcodes.MONITORENTER || op == Opcodes.MONITOREXIT) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				block.getCode().add(new MonitorNode(expr, op - Opcodes.MONITORENTER));
			}
			else if (op >= Opcodes.IADD && op <= Opcodes.DREM)
				stack.push(new MathematicalExpression(stack.pop(), stack.pop(), ((op - Opcodes.IADD) / 4) + MathematicalExpression.TYPE_ADD));
			else if (op >= Opcodes.INEG && op <= Opcodes.DNEG)
				stack.push(new NegateExpression(stack.pop()));
			else if (op >= Opcodes.ISHL && op <= Opcodes.LUSHR)
				stack.push(new MathematicalExpression(stack.pop(), stack.pop(), ((op - Opcodes.ISHL) / 2) + MathematicalExpression.TYPE_SHL));
			else if (op == Opcodes.IAND || op == Opcodes.LAND)
				stack.push(new MathematicalExpression(stack.pop(), stack.pop(), MathematicalExpression.TYPE_AND));
			else if (op == Opcodes.IOR || op == Opcodes.LOR)
				stack.push(new MathematicalExpression(stack.pop(), stack.pop(), MathematicalExpression.TYPE_OR));
			else if (op == Opcodes.IXOR || op == Opcodes.LXOR)
				stack.push(new MathematicalExpression(stack.pop(), stack.pop(), MathematicalExpression.TYPE_XOR));
			else if (op >= Opcodes.ILOAD && op <= Opcodes.ALOAD)
				stack.push(new VariableLoadExpression(Utilities.variableLoadType(op), ((VariableInstruction)instruction).getIndex()));
			else if (op >= Opcodes.ISTORE && op <= Opcodes.ASTORE) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				block.getCode().add(new VariableAssignationNode(expr.getType(), ((VariableInstruction)instruction).getIndex(),expr));
			}
			else if (op == Opcodes.IINC) {
				dumpStack(block,stack);
				int index = ((IntIncrementInstruction)instruction).getIndex();
				int value = ((IntIncrementInstruction)instruction).getIncrementor();
				block.getCode().add(new VariableAssignationNode(Type.INT_TYPE, index,new MathematicalExpression(new ConstantExpression(value), new VariableLoadExpression(Type.INT_TYPE,index), MathematicalExpression.TYPE_ADD)));
			}
			else if (op == Opcodes.PUTSTATIC) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				FieldInstruction fieldInstr = (FieldInstruction)instruction;
				ExpressionNode expr = stack.pop();
				block.getCode().add(new FieldAssignationNode(null, expr, fieldInstr.getOwner(), fieldInstr.getName(), fieldInstr.getDescriptor()));
			}
			else if (op == Opcodes.PUTFIELD) {
				if (stack.getSize() > 2)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				ExpressionNode base = stack.pop();
				FieldInstruction fieldInstr = (FieldInstruction)instruction;
				block.getCode().add(new FieldAssignationNode(base,expr, fieldInstr.getOwner(), fieldInstr.getName(), fieldInstr.getDescriptor()));
			}
			else if (op == Opcodes.GETSTATIC || op == Opcodes.GETFIELD)
				stack.push(new FieldLoadExpression(op == Opcodes.GETFIELD ? stack.pop() : null, ((FieldInstruction)instruction).getOwner(), ((FieldInstruction)instruction).getName(), ((FieldInstruction)instruction).getDescriptor()));
			else if (op == Opcodes.ARRAYLENGTH)
				stack.push(new ArrayLengthExpression(stack.pop()));
			else if (op >= Opcodes.IALOAD && op <= Opcodes.SALOAD) {
				ExpressionNode index = stack.pop();
				ExpressionNode base = stack.pop();
				stack.push(new ArrayLoadExpression(Utilities.arrayLoadType(op), base, index));
			}
			else if (op >= Opcodes.IASTORE && op <= Opcodes.SASTORE) {
				if (stack.getSize() > 3)
					dumpStack(block, stack);
				ExpressionNode value = stack.pop();
				ExpressionNode index = stack.pop();
				ExpressionNode base = stack.pop();	
				block.getCode().add(new ArrayAssignationNode(Utilities.arrayStoreType(op), base, index, value));
			}
			else if (op >= Opcodes.I2L && op <= Opcodes.I2S)
				stack.push(new CastExpression(stack.pop(), Utilities.primitiveCastType(op)));
			else if (op == Opcodes.CHECKCAST)
				stack.push(new CastExpression(stack.pop(), Type.getType("L" + ((TypeInstruction)instruction).getType() + ";")));
			else if (op == Opcodes.INSTANCEOF)
				stack.push(new InstanceofExpression(stack.pop(), Type.getType("L" + ((TypeInstruction)instruction).getType() + ";")));
			else if (op == Opcodes.NEW)
				stack.push(new NewUninitializedObjectExpression(Type.getType("L" + ((TypeInstruction)instruction).getType() + ";")));
			else if (op == Opcodes.NEWARRAY)
				stack.push(new NewUninitializedArrayExpression(Utilities.primitiveArrayType(((IntInstruction)instruction).getOperand()), new ExpressionNode[] { stack.pop() }));
			else if (op == Opcodes.ANEWARRAY)
				stack.push(new NewUninitializedArrayExpression(Type.getType("[L" + ((TypeInstruction)instruction).getType() + ";"), new ExpressionNode[] { stack.pop() }));
			else if (op == Opcodes.MULTIANEWARRAY) {
				MultiArrayInstruction marray = (MultiArrayInstruction)instruction;
				ExpressionNode[] lengths = new ExpressionNode[marray.getDimensions()];
				for (int i = marray.getDimensions() - 1; i >= 0; i--)
					lengths[i] = stack.pop();
				stack.push(new NewUninitializedArrayExpression(Type.getType(marray.getDescriptor()), lengths));
			}
			else if (op == Opcodes.INVOKEDYNAMIC)
				throw new RuntimeException("TODO java7 stuff");
			else if (op == Opcodes.INVOKESTATIC || op == Opcodes.INVOKEVIRTUAL || op == Opcodes.INVOKEINTERFACE || op == Opcodes.INVOKESPECIAL) {
				MethodInstruction mtdInstr = (MethodInstruction)instruction;
				ExpressionNode[] arguments = new ExpressionNode[Type.getArgumentTypes(mtdInstr.getDescriptor()).length + (op != Opcodes.INVOKESTATIC ? 1 : 0)];
				for (int i = arguments.length - 1; i >= 0; i--)
					arguments[i] = stack.pop();
				int type = op == Opcodes.INVOKESTATIC ? InvokeExpression.TYPE_INVOKESTATIC : (op == Opcodes.INVOKEVIRTUAL ? InvokeExpression.TYPE_INVOKEVIRTUAL : (op == Opcodes.INVOKEINTERFACE ? InvokeExpression.TYPE_INVOKEINTERFACE : InvokeExpression.TYPE_INVOKESPECIAL));
				InvokeExpression invokeexpr = new InvokeExpression(type, arguments, mtdInstr.getOwner(), mtdInstr.getName(), mtdInstr.getDescriptor());
				if (invokeexpr.getType() == Type.VOID_TYPE) {
					dumpStack(block, stack);
					block.getCode().add(new PopableNode(invokeexpr));
				}
				else {
					stack.push(invokeexpr);
				}
			}
			else if (op == Opcodes.POP) {
				if (stack.getSize() > 1)
					dumpStack(block, stack);
				ExpressionNode expr = stack.pop();
				block.getCode().add(new PopableNode(expr));
			}
			else if (op == Opcodes.POP2) {
				if (stack.peek().size() == 64) {
					if (stack.getSize() > 1)
						dumpStack(block, stack);
					ExpressionNode expr = stack.pop();
					block.getCode().add(new PopableNode(expr));
				}
				else {
					if (stack.getSize() > 2)
						dumpStack(block, stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					block.getCode().add(new PopableNode(expr1));
					block.getCode().add(new PopableNode(expr2));
				}
			}
			else if (op == Opcodes.DUP) {
				dumpStack(block,stack);
				stack.push(stack.peek().copy());
			}
			else if (op == Opcodes.DUP2) {
				if (stack.peek(0).getType().getSize() == 1) {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					stack.push(expr1);
					stack.push(expr2);
					stack.push(expr1.copy());
					stack.push(expr2.copy());
				}
				else {
					dumpStack(block,stack);
					stack.push(stack.peek().copy());
				}
			}
			else if (op == Opcodes.DUP_X1) {
				dumpStack(block,stack);
				ExpressionNode expr2 = stack.pop();
				ExpressionNode expr1 = stack.pop();
				stack.push(expr2.copy());
				stack.push(expr1);
				stack.push(expr2);
			}
			else if (op == Opcodes.DUP_X2) {
				if (stack.peek(1).getType().getSize() == 2) {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					stack.push(expr2.copy());
					stack.push(expr1);
					stack.push(expr2);
				}
				else {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					ExpressionNode expr0 = stack.pop();
					stack.push(expr2.copy());
					stack.push(expr0);
					stack.push(expr1);
					stack.push(expr2);
				}
			}
			else if (op == Opcodes.DUP2_X1) {
				if (stack.peek(0).getType().getSize() == 1) {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					ExpressionNode expr0 = stack.pop();
					stack.push(expr1.copy());
					stack.push(expr2.copy());
					stack.push(expr0);
					stack.push(expr1);
					stack.push(expr2);
				}
				else {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					stack.push(expr2.copy());
					stack.push(expr1);
					stack.push(expr2);
				}
			}
			else if (op == Opcodes.DUP2_X2) {
				if (stack.peek(0).getType().getSize() == 2 && stack.peek(1).getType().getSize() == 2) {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					stack.push(expr2.copy());
					stack.push(expr1);
					stack.push(expr2);
				}
				else if (stack.peek(0).getType().getSize() == 2 && stack.peek(1).getType().getSize() == 1) {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					ExpressionNode expr0 = stack.pop();
					stack.push(expr2.copy());
					stack.push(expr0);
					stack.push(expr1);
					stack.push(expr2);
				}
				else if (stack.peek(0).getType().getSize() == 1 && stack.peek(1).getType().getSize() == 2) {
					dumpStack(block,stack);
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					ExpressionNode expr0 = stack.pop();
					stack.push(expr1.copy());
					stack.push(expr2.copy());
					stack.push(expr0);
					stack.push(expr1);
					stack.push(expr2);
				}
				else {
					dumpStack(block,stack);
					ExpressionNode expr3 = stack.pop();
					ExpressionNode expr2 = stack.pop();
					ExpressionNode expr1 = stack.pop();
					ExpressionNode expr0 = stack.pop();
					stack.push(expr2.copy());
					stack.push(expr3.copy());
					stack.push(expr0);
					stack.push(expr1);
					stack.push(expr2);
					stack.push(expr3);
				}
			}
			else if (op == Opcodes.SWAP) {
				dumpStack(block,stack);
				ExpressionNode expr2 = stack.pop();
				ExpressionNode expr1 = stack.pop();
				stack.push(expr2);
				stack.push(expr1);
			}
			else {
				throw new RuntimeException("Unknown:" + Utilities.getOpcodeName(op));
			}
		}
	}
	
	/**
	 * Write's code to parent method.
	 */
	private void writeCode() {
		CodeNode node = parent.getCode();
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null) {
				node.write(blocks[i].getAnnotation());
				for (AbstractCodeNode n : blocks[i].getCode())
					node.write(n);
			}
	}
	
	/**
	 * Find's first free slot to use for stack dumps.
	 */
	private void calculateSlots(int maxstack, int maxlocals) {
		stackDumpsStartAddress = maxlocals;
		miscDumpsStartAddress = maxlocals + maxstack;
	} 
	
	/**
	 * Find's block ID of given label.
	 */
	private int blockID(Label label) {
		int total = 0;
		for (int i = 0; i < instructions.length; i++)
			if (instructions[i] instanceof LabelInstruction) {
				if (((LabelInstruction)instructions[i]).getLabel() == label)
					return total;
				total++;
			}
		return -1;
	}
	
	/**
	 * Find's label address.
	 */
	private int labelAddress(Label label) {
		for (int i = 0; i < instructions.length; i++)
			if (instructions[i] instanceof LabelInstruction && ((LabelInstruction)instructions[i]).getLabel() == label)
				return i;
		return -1;
	}
	
	/**
	 * Generate's block for specific label with given input stack.
	 */
	private Block generateBlock(Label label, Stack inputStack) {
		int blockID = blockID(label);
		if (blocks[blockID] == null) {
			Block block = (blocks[blockID] = new Block(labelAddress(label),inputStack));
			queue.insert(block);
			return block;
		}
		else if (blocks[blockID].getEntryStack() == null) {
			blocks[blockID].setEntryStack(inputStack);
			queue.insert(blocks[blockID]);
			return blocks[blockID];
		}
		if (!canMerge(inputStack,blocks[blockID].getEntryStack()))
			throw new RuntimeException("Can't merge two stacks (Code is invalid).");
		return blocks[blockID];
	}
	
	/**
	 * Dump's stack to given block.
	 */
	private void dumpStack(Block block, Stack stack) {
		ExpressionNode[] expressions = new ExpressionNode[stack.getSize()];
		for (int i = expressions.length - 1; i >= 0; i--)
			expressions[i] = stack.pop();
		int store = stackDumpsStartAddress;
		if ((parent.getParent().getOptions() & ClassNode.OPT_GENERATION_PRINT_STACK_DUMPS) != 0) {
			String text = "Stack dump begin {\n";
			text += "\tbase addr:" + store + "\n";
			text += "\tstack:" + "\n";
			for (int i = 0; i < expressions.length; i++)
				text += "\t" + expressions[i].toString() + "\n";
			text += "}";		
			block.getCode().add(new CommentNode(text));
		}
		int miscStore = miscDumpsStartAddress;
		for (int i = 0; i < expressions.length; i++) { 
			if (expressions[i] instanceof VariableLoadExpression && ((VariableLoadExpression)expressions[i]).getIndex() >= store) {
				block.getCode().add(new VariableAssignationNode(expressions[i].getType(), miscStore, expressions[i]));
				expressions[i] = new VariableLoadExpression(expressions[i].getType(), miscStore);
				miscStore += expressions[i].getType().getSize();
			}
		}
		
		for (int i = 0; i < expressions.length; i++) {
			block.getCode().add(new VariableAssignationNode(expressions[i].getType(), store, expressions[i]));
			stack.push(new VariableLoadExpression(expressions[i].getType(), store));
			store += expressions[i].getType().getSize();
		}
		if ((parent.getParent().getOptions() & ClassNode.OPT_GENERATION_PRINT_STACK_DUMPS) != 0) {
			block.getCode().add(new CommentNode("Stack dump end {\n\t end addr:" + store + "\n}"));
		}
	}
	
	/**
	 * Check's if two stacks can be merged.
	 */
	private boolean canMerge(Stack s1, Stack s2) {
		if (s1.getSize() != s2.getSize())
			return false;
		Stack c0 = s1.copy();
		Stack c1 = s2.copy();
		while (c0.getSize() > 0) {
			ExpressionNode expr1 = c0.pop();
			ExpressionNode expr2 = c1.pop();
			if (!(expr1 instanceof VariableLoadExpression) || !(expr2 instanceof VariableLoadExpression))
				return false;
			if (((VariableLoadExpression)expr1).getIndex() != ((VariableLoadExpression)expr2).getIndex())
				return false;
			if (expr1.getType() != expr2.getType())
				return false;
		}
		return true;
	}


	public int getStackDumpsStartAddress() {
		return stackDumpsStartAddress;
	}
}
