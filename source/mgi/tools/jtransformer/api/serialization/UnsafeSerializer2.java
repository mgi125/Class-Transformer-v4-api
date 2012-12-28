package mgi.tools.jtransformer.api.serialization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import sun.misc.Unsafe;
import mgi.tools.jtransformer.utilities.BITBuffer;
import mgi.tools.jtransformer.utilities.ByteBuffer;

public class UnsafeSerializer2 implements ISerializer {

	/**
	 * Instance of unsafe.
	 */
	private Unsafe unsafe;
	
	public UnsafeSerializer2() {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
			field.setAccessible(false);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Failed to obtain instance of unsafe.");
		}
	}

	@Override
	public Object readObject(ByteBuffer buff) {
		BITBuffer buffer = new BITBuffer(buff.getBuffer());
		buffer.setPosition(0);
		if (buffer.readInt() != 0x12345678) {
			throw new RuntimeException("Bad magic");
		}
		int prop = buffer.readUByte();
		boolean poolCompressed = (prop & 0x1) != 0;
		boolean primitiveArraysCompressed = (prop & 0x2) != 0;
		int version = prop >> 2;
		Pool<String> stringsPool = new Pool<String>();
		Pool<Object> objectsPool = new Pool<Object>();
		byte[] data = depack(buffer, poolCompressed);
		
		ByteBuffer b = new ByteBuffer(data);
		int count = b.readInt();
		for (int i = 0; i < count; i++)
			stringsPool.insert(b.readStringUTF());
		
		return readObjectImpl(version, stringsPool, objectsPool, primitiveArraysCompressed, buffer);
	}
	
	private Object readObjectImpl(int fVersion, Pool<String> stringsPool, Pool<Object> objectsPool, boolean primitiveArraysCompressed, BITBuffer buffer) {
		int objectID = buffer.readInt();
		if (objectsPool.find(objectID) != null)
			return objectsPool.find(objectID);
		
		
		Class<?> type = findClass(stringsPool.find(stringsPool.readIndex(buffer)));
		Object object = null;
		if (type.isArray()) {
			object = Array.newInstance(type.getComponentType(), buffer.readInt());
		}
		else {
			try {
				object = unsafe.allocateInstance(type);
			}
			catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		
		if (objectsPool.insertNew(object) != objectID)
			throw new RuntimeException();
		
		if (type.isArray()) {
			Object array = object;
			Class<?> element = type.getComponentType();
			int length = Array.getLength(array);
			if (length > 0) {
				long base = unsafe.arrayBaseOffset(type);
				long size = element == long.class || element == double.class ? 8 : (element == boolean.class || element == byte.class ? 1 : (element == short.class || element == char.class ? 2 : 4));			
				if (element.isPrimitive()) {
					byte[] data = depack(buffer, primitiveArraysCompressed);
					ByteBuffer b = new ByteBuffer(data);
					for (int i = 0; i < length; i++) {
						for (long a = 0; a < size; a++)
							unsafe.putByte(array, base + ((long)i * size) + a , (byte)b.readUByte());
					}
				}
				else {
					for (int i = 0; i < length; i++) {
						if (buffer.readUByte() == 1) // != null
							Array.set(array, i, readObjectImpl(fVersion, stringsPool, objectsPool, primitiveArraysCompressed, buffer));
					}
				}
			}
		}
		
		return null; // TODO finish..
		
	}

	@Override
	public ByteBuffer writeObject(Object object) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private byte[] depack(BITBuffer buffer, boolean compressed) {
		byte[] data = null;
		if (compressed) {
			int compressedSize = buffer.readInt();
			int decompressedSize = buffer.readInt();
			int tableSize = buffer.readInt();
			byte[][] table = new byte[tableSize][];
			buffer.startBIT();
			for (int i = 0; i < tableSize; i++) {
				int size = buffer.readBITS(2) + 1;
				table[i] = new byte[size * 8];
				for (int x = 0; x < size; x++)
					table[i][x] = (byte)buffer.readBITS(8);
			}
			buffer.endBIT();
			
			data = new byte[compressedSize];
			buffer.readBytes(data, 0, data.length);
			data = decompressData(data, table, decompressedSize);
		}
		else {
			int size = buffer.readInt();
			data = new byte[size];
			buffer.readBytes(data, 0, data.length);
		}
		return data;
	}
	
	public static class Pool<K> {
		/**
		 * Contains pool.
		 */
		private Map<Integer, K> pool;
		
		public Pool() {
			pool = new HashMap<Integer, K>();
		}
		
		public int insert(K obj) {
			if (obj == null)
				return 0;
			for (Integer idx : pool.keySet()) {
				if (pool.get(idx).equals(obj))
					return idx.intValue();
			}
			pool.put(pool.size() + 1, obj);
			return pool.size();
		}
		
		public int insertNew(K obj) {
			if (obj == null)
				throw new RuntimeException();
			pool.put(pool.size() + 1, obj);
			return pool.size();
		}
		
		public K find(int idx) {
			return pool.get(idx);
		}
		
		public int readIndex(ByteBuffer buffer) {
			int size = pool.size() + 1;
			if (size < 256)
				return buffer.readUByte();
			else if (size < 65536)
				return buffer.readUnsignedShort();
			else if (size < 16777216)
				return buffer.readUnsignedMedInt();
			else
				return buffer.readInt();
			
		}
		
		public void writeIndex(ByteBuffer buffer, int index) {
			int size = pool.size() + 1;
			if (size < 256)
				buffer.writeByte(index);
			else if (size < 65536)
				buffer.writeShort(index);
			else if (size < 16777216)
				buffer.writeMedInt(index);
			else
				buffer.writeInt(index);
		}
	}
	
	
	
	private static byte[] compressData(byte[] data, byte[][] table) {
		BigInteger compressed = BigInteger.ZERO;
		BigInteger table_length = BigInteger.valueOf(table.length);
		
		for (int offset = 0; offset < data.length;) { 
			int entry = findBestTableEntry(data, table, offset);
			if (entry == -1)
				throw new RuntimeException("Invalid compression table!");
			compressed = compressed.multiply(table_length).add(BigInteger.valueOf(entry));
			offset += table[entry].length;
		}
		return compressed.toByteArray();
	}
	
	
	private static byte[] decompressData(byte[] data, byte[][] table, int decompressedLength) {
		byte[] buffer = new byte[decompressedLength];
		BigInteger table_length = BigInteger.valueOf(table.length);
		BigInteger b_data = new BigInteger(data);
		for (int write = buffer.length - 1; write >= 0;) {
			if (b_data.compareTo(table_length) >= 0) {
				BigInteger nondiv = b_data;
				b_data = b_data.divide(table_length);
				int tableIndex = nondiv.subtract(b_data.multiply(table_length)).intValue();
				byte[] value = table[tableIndex];
				for (int i = value.length - 1; i >= 0; i--)
					buffer[write--] = value[i];
			}
			else {
				byte[] value = table[b_data.intValue()];
				for (; write >= 0;)
					for (int i = value.length - 1; i >= 0; i--)
						buffer[write--] = value[i];
				break;
			}
		}
		return buffer;
	}
	
	private static int findBestTableEntry(byte[] data,byte[][] table,int dataOffset) {
		int bestEntry = -1;
		int bestLength = -1;
		loop: for (int i = 0; i < table.length; i++) {
			byte[] entry = table[i];
			if ((data.length - dataOffset) < entry.length)
				continue;
			for (int a = 0; a < entry.length; a++)
				if (data[a + dataOffset] != entry[a])
					continue loop;
			if (entry.length > bestLength) {
				bestEntry = i;
				bestLength = entry.length;
			}
		}
		return bestEntry;
	}
	
	private static Class<?> findClass(String utfName) {
		if (utfName.equals("void"))
			return void.class;
		else if (utfName.equals("boolean"))
			return boolean.class;
		else if (utfName.equals("byte"))
			return byte.class;
		else if (utfName.equals("short"))
			return short.class;
		else if (utfName.equals("char"))
			return char.class;
		else if (utfName.equals("int"))
			return int.class;
		else if (utfName.equals("long"))
			return long.class;
		else if (utfName.equals("float"))
			return float.class;
		else if (utfName.equals("double"))
			return double.class;
		else {
			try {
				return Class.forName(utfName);
			}
			catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	}
	
	private static Field findField(Class<?> declaredOn, String name, Class<?> type) {
		for (Field field : declaredOn.getDeclaredFields())
			if (field.getName().equals(name) && field.getType().equals(type))
				return field;
		return null;
	}

}
