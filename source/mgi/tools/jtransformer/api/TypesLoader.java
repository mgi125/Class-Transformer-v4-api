package mgi.tools.jtransformer.api;

import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.Type;

public abstract class TypesLoader {

	/**
	 * Load's Class<?> instance which represents given type.
	 * Loading mechanism depends on the implementation however
	 * this method will never throw any exceptions, if the type
	 * cannot be found, it will return null.
	 */
	public abstract Class<?> loadType(Type t);
	
	/**
	 * Find's first common type.
	 * Both types must be either both reference or both
	 * primitive.
	 */
	public Type getCommonType(Type t1, Type t2) {
		int st1 = t1.getSort();
		int st2 = t2.getSort();
		if (st1 >= Type.ARRAY && st1 <= Type.OBJECT && st2 >= Type.ARRAY && st2 <= Type.OBJECT)
			return getCommonReferenceType(t1, t2);
		else
			return getCommonPrimitiveType(t1, t2);
	}
	
	/**
	 * Find's first common primitive type.
	 * Return cannot be null.
	 */
	public Type getCommonPrimitiveType(Type t1, Type t2) {
		if (t1.getSort() < Type.BOOLEAN || t1.getSort() > Type.DOUBLE || t2.getSort() < Type.BOOLEAN || t2.getSort() > Type.DOUBLE)
			throw new RuntimeException("WT");
		
		if (t1 == t2) // equals
			return t1;
		
		Type base;
		if ((base = Utilities.baseType(t1)) != Utilities.baseType(t2))
			throw new RuntimeException("WT");
		
		if (base == Type.INT_TYPE) {
			if (t1 == Type.BOOLEAN_TYPE || t2 == Type.BOOLEAN_TYPE || t1 == Type.CHAR_TYPE || t2 == Type.CHAR_TYPE)
				return Type.INT_TYPE; // in case of boolean or char, we clearly have type mismatch so better make it int
			int sizeT1 = t1 == Type.BYTE_TYPE ? 1 : (t1 == Type.SHORT_TYPE ? 2 : 4);
			int sizeT2 = t2 == Type.BYTE_TYPE ? 1 : (t2 == Type.SHORT_TYPE ? 2 : 4);
			if (sizeT1 > sizeT2)
				return t1;
			else if (sizeT2 > sizeT1)
				return t2;
			else
				throw new RuntimeException("WT");
		}
		else {
			throw new RuntimeException("WT");
		}
	
		
	}
	
	/**
	 * Find's first common reference supertype.
	 * Return cannot be null , as any object can be at least 
	 * assigned to "Ljava/lang/Object;".
	 */
	public Type getCommonReferenceType(Type t1, Type t2) {
		if (t1.equals(t2))
			return t1; // simple check, to save some cost.
		
		int sT1 = t1.getSort();
		int sT2 = t2.getSort();
		
		if (sT1 < Type.ARRAY || sT1 > Type.OBJECT || sT2 < Type.ARRAY || sT2 > Type.OBJECT)
			throw new RuntimeException("WT");
		
		if ((sT1 == Type.ARRAY || sT2 == Type.ARRAY) && sT1 != sT2) {
			// one of the types is array and other is object
			return Type.getType("Ljava/lang/Object;");
		}
		else if (sT1 == Type.ARRAY && sT2 == Type.ARRAY) {
			// both of the types are array types
			Type e1 = t1.getElementType(), e2 = t2.getElementType();
			int d1s = t1.getDimensions(), d2s = t2.getDimensions();
			int e1s = e1.getSort(), e2s = e2.getSort();
			if (e1s == e2s && d1s == d2s) {
				// array element types match, dimensions also match.
				if (e1s == Type.OBJECT) {
					// we got something like x[][][] and y[][][] so merge the x type with y
					int dims = d1s;
					String element = getCommonReferenceType(e1, e2).getDescriptor();
					StringBuilder dsc = new StringBuilder(element.length() + dims);
					while (dims-- > 0)
						dsc.append("[");
					dsc.append(element);
					return Type.getType(dsc.toString());	
				}
				else {
					// we got something like byte[][][][] and byte[][][][] so they are same
					return t1;
				}
			}
			else if (e1s == Type.OBJECT && e2s == Type.OBJECT) {
				// both of the elements are ref's so our result will be
				// Object[][]..[] while num of dims is the lowest dim's amount.
				int dims = Math.min(d1s, d2s);
				StringBuilder dsc = new StringBuilder(18 + dims);
				while (dims-- > 0)
					dsc.append("[");
				dsc.append("Ljava/lang/Object;");
				return Type.getType(dsc.toString());
			}
			else {
				// one or both of the element's are primitive array
				// so we merge it to Object[][]..[] or Object where num of dimensions are lower by 1.
				int dims = Math.min(d1s, d2s) - 1;
				if (dims > 0) {
					StringBuilder dsc = new StringBuilder(18 + dims);
					while (dims-- > 0)
						dsc.append("[");
					dsc.append("Ljava/lang/Object;");
					return Type.getType(dsc.toString());
				}
				else {
					return Type.getType("Ljava/lang/Object;");
				}
			}
		}
		else {
			// both of the types are reference types.
			Class<?> c1 = loadType(t1);
			Class<?> c2 = loadType(t2);
			
			if (c1 == null || c2 == null) // we couldn't load one of the types?
				return Type.getType("Ljava/lang/Object;");
			
			if (c1.isAssignableFrom(c2))
				return t1;
			if (c2.isAssignableFrom(c1))
				return t2;
			
			if (c1.isInterface() || c2.isInterface())
				return Type.getType("Ljava/lang/Object;");
			else {
				do {
					c1 = c1.getSuperclass();
				} while (!c1.isAssignableFrom(c2));
				return Type.getType(c1);
			}
		}

	}
	

	
}
