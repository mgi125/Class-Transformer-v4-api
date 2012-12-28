package mgi.tools.jtransformer.utilities;

public class MutableReference<T> {

	/**
	 * Object itself.
	 */
	private T object;
	
	public MutableReference() {
		
	}
	
	public MutableReference(T object) {
		this.object = object;
	}

	public T get() {
		return object;
	}

	public void set(T object) {
		this.object = object;
	}

}
