package mgi.tools.jtransformer.utilities;

import java.util.Arrays;


public class ArrayQueue<T> {

	public static final int INITIAL_CAPACITY = 1000;
	
	private Object[] queue;
	private int top;
	
	
	public ArrayQueue() {
		this(INITIAL_CAPACITY);
	}
	
	public ArrayQueue(int initialCapacity) {
		this.queue = new Object[initialCapacity];
	}

	
	public void insert(T obj) {
		if (top >= queue.length) {
			Object[] newQueue = new Object[queue.length * 2];
			System.arraycopy(queue, 0, newQueue, 0, queue.length);
			queue = newQueue;
		}
		queue[top++] = obj;
	}
	
	@SuppressWarnings("unchecked")
	public T take() {
		if (top <= 0)
			throw new RuntimeException("Nothing to take.");
		return (T)queue[--top];
	}
	
	public int size() {
		return top;
	}
	
	public void clear() {
		top = 0;
		Arrays.fill(queue, null);
	}
	
	/**
	 * Iterate's thru all elements and checks if 
	 * there's at least one element that 
	 * obj.equals(element);
	 */
	public boolean lookup(T obj) {
		for (int i = 0; i < top; i++)
			if (queue[i] != null && obj.equals(queue[i]))
				return true;
		return false;
	}
	
	public void dispose() {
		queue = null;
	}

}
