package mgi.tools.jtransformer;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import mgi.tools.jtransformer.api.TypesLoader;

public class ClassTransformer {

	/**
	 * Contains version of this api.
	 */
	private String version;
	/**
	 * Contains types loader.
	 */
	private TypesLoader typesLoader;
	
	private ClassTransformer(TypesLoader typesLoader) {
		this.version = "Class Transformer CT4[Alpha 0.41]";
		this.typesLoader = typesLoader;
	}
	
	/**
	 * Load's api with default implementation of types loader.
	 */
	public static ClassTransformer loadAPI() {
		return loadAPI(new DefaultTypesLoaderImpl());
	}
	
	/**
	 * Load's api with specific implementation of types loader.
	 */
	public static ClassTransformer loadAPI(TypesLoader typesLoader) {
		ClassTransformer ct = new ClassTransformer(typesLoader);
		System.err.println(ct.getVersion() + " (C) mgi125 2011-2013");
		return ct;
	}
	
	
	
	public String getVersion() {
		return version;
	}

	public TypesLoader getTypesLoader() {
		return typesLoader;
	}
	
	/**
	 * Default implementation of types loader.
	 */
	private static class DefaultTypesLoaderImpl extends TypesLoader {
		/**
		 * Contains first types cache used 
		 * for reference and array types.
		 */
		private Map<Type, Class<?>> cache1;
		/**
		 * Contains second types cache used 
		 * for caching resolved common types.
		 */
		private Map<String, Type> cache2;
		
		public DefaultTypesLoaderImpl() {
			cache1 = new HashMap<Type, Class<?>>();
			cache2 = new HashMap<String, Type>();
		}
		
		@Override
		public Class<?> loadType(Type t) {
			try {
				switch (t.getSort()) {
					case Type.BOOLEAN:
						return boolean.class;
					case Type.BYTE:
						return byte.class;
					case Type.CHAR:
						return char.class;
					case Type.SHORT:
						return short.class;
					case Type.INT:
						return int.class;
					case Type.LONG:
						return long.class;
					case Type.FLOAT:
						return float.class;
					case Type.DOUBLE:
						return double.class;
					case Type.VOID:
						return void.class;
					case Type.OBJECT:
					case Type.ARRAY:
						Class<?> c = cache1.get(t);
						if (c != null)
							return c;
						cache1.put(t, c = Class.forName(t.getInternalName().replace('/', '.')));
						return c;
					default:
						throw new ClassNotFoundException();
				}
			}
			catch (ClassNotFoundException e) {
				if (Constants.CORE_DEBUG) {
					System.err.println("Err, type not found:" + t.getInternalName().replace('/', '.'));
				}
				return null;
			}
		}
		
		@Override
		public Type getCommonReferenceType(Type t1, Type t2) {
			String dsc1 = t1.getDescriptor();
			String dsc2 = t2.getDescriptor();
			if (dsc1.equals(dsc2))
				return t1;
			StringBuilder keyBuilder = new StringBuilder(dsc1.length() + dsc2.length() + 1);
			keyBuilder.append(dsc1);
			keyBuilder.append('@');
			keyBuilder.append(dsc2);
			String key = keyBuilder.toString();
			Type common = cache2.get(key);
			if (common != null)
				return common;
			cache2.put(key, common = super.getCommonReferenceType(t1, t2));
			return common;
		}
		
	}

}
