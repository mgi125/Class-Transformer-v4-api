package mgi.tools.jtransformer.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Small class which holds class nodes instances and 
 * few utility methods.
 */
public class Tree implements Iterable<ClassNode> {

	/**
	 * Contains all classes in this tree.
	 */
	private List<ClassNode> classes = new ArrayList<ClassNode>();
	
	
	/**
	 * Add's given class to the tree.
	 */
	public void add(ClassNode clazz) {
		if (!classes.contains(clazz))
			classes.add(clazz);
	}
	
	/**
	 * Wheter this tree has given class.
	 */
	public boolean hasClass(ClassNode clazz) {
		return classes.contains(clazz);
	}
	
	/**
	 * Remove's given class from classes list.
	 */
	public boolean remove(ClassNode clazz) {
		return classes.remove(clazz);
	}
	
	/**
	 * Find's class with given name.
	 */
	public ClassNode findClass(String name) {
		for (ClassNode clazz : classes)
			if (clazz.getName().equals(name))
				return clazz;
		return null;
	}
	
	/**
	 * Find's field with given details.
	 */
	public FieldNode findField(String classname, String fieldname) {
		ClassNode clazz = findClass(classname);
		if (clazz == null)
			return null;
		for (FieldNode field : clazz.getFields())
			if (field.getName().equals(fieldname))
				return field;
		return null;
	}
	
	/**
	 * Find's field with given details.
	 */
	public FieldNode findField(String classname, String fieldname, String descriptor) {
		ClassNode clazz = findClass(classname);
		if (clazz == null)
			return null;
		for (FieldNode field : clazz.getFields())
			if (field.getName().equals(fieldname) && field.getDescriptor().equals(descriptor))
				return field;
		return null;
	}
	
	/**
	 * Find's method with given details.
	 */
	public MethodNode findMethod(String classname, String methodname) {
		ClassNode clazz = findClass(classname);
		if (clazz == null)
			return null;
		for (MethodNode method : clazz.getMethods())
			if (method.getName().equals(methodname))
				return method;
		return null;
	}
	
	/**
	 * Find's method with given details.
	 */
	public MethodNode findMethod(String classname, String methodname,String descriptor) {
		ClassNode clazz = findClass(classname);
		if (clazz == null)
			return null;
		for (MethodNode method : clazz.getMethods())
			if (method.getName().equals(methodname) && method.getDescriptor().equals(descriptor))
				return method;
		return null;
	}
	

	@Override
	public Iterator<ClassNode> iterator() {
		return classes.iterator();
	}
	
	
	
	
}
