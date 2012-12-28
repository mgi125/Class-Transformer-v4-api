package mgi.tools.jtransformer.utilities;

public class MutableInteger {

	/**
	 * Integer itself.
	 */
	private int integer;
	
	public MutableInteger() {
		this(0);
	}
	
	public MutableInteger(int integer) {
		this.integer = integer;
	}
	
	public int inc() {
		return integer++;
	}
	
	public int dec() {
		return integer--;
	}
	
	
	public int get() {
		return integer;
	}
	
	public void set(int value) {
		integer = value;
	}
}
