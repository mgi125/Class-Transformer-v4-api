package mgi.tools.jtransformer.api.code.generation;

import mgi.tools.jtransformer.api.code.ExpressionNode;

public class Stack {

	private ExpressionNode[] stack;
	private int size;
	public static final int BUFFER_SIZE = 500;
	
	public Stack() {
		this(BUFFER_SIZE);
	}
	
	public Stack(int bufferSize) {
		stack = new ExpressionNode[bufferSize];
	}

	public ExpressionNode pop() {
		if (size <= 0)
			throw new RuntimeException("Stack underflow");
		return stack[--size];
	}
	
	public ExpressionNode peek() {
		if (size <= 0)
			throw new RuntimeException("Stack underflow");
		return stack[size - 1];
	}
	
	public ExpressionNode peek(int depth) {
		if ((size - depth) <= 0)
			throw new RuntimeException("Stack underflow");
		return stack[size - depth - 1];
	}
	
	public void push(ExpressionNode expr) {
		if ((size + 1) >= stack.length)
			throw new RuntimeException("Stack overflow");
		stack[size++] = expr;
	}
	
	public Stack copy() {
		Stack stack = new Stack(this.stack.length);
		stack.size = this.size;
		for (int i = 0; i < this.stack.length; i++)
			if (this.stack[i] != null)
				stack.stack[i] = this.stack[i].copy();
		return stack;
	}
	
	
	public int getSize() {
		return size;
	}

	
	public void clear() {
		size = 0;
	}
	
	

}
