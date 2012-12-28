package tests;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Random;

import mgi.tools.jtransformer.api.serialization.UnsafeSerializer;
import mgi.tools.jtransformer.utilities.ByteBuffer;

@SuppressWarnings("unused")
public class ReflectionTest {
 
	public static void main(String[] args) throws Throwable {
		System.err.println(byte.class.getName());
		System.err.println(Class.forName("[Ljava.lang.String;").getName());
		
		UnsafeSerializer porter = new UnsafeSerializer(); 
		
		{
			ByteBuffer buffer = porter.writeObject(BigInteger.valueOf(-4588556));
			BigInteger rr = (BigInteger)porter.readObject(buffer);
			System.err.println("Readed:" + rr);
		}
		
		{
			Object floatTest = new Object() {
				float f1, f2, f3;
				double d1, d2, d3;
				boolean initialized;
				Object o1, o2, o3;
				byte[] b1, b2, b3;
				
				@Override
				public String toString() {
					if (!initialized) {
						initialized = true;
						Random random = new Random();
						f3 = random.nextFloat() / (f2 = random.nextFloat() * (f1 = random.nextFloat()));
						d3 = random.nextDouble() / (d2 = random.nextDouble() * (d1 = random.nextDouble()));
						o1 = this;
						o2 = "[" + f1 + "," + f2 + "," + f3 + "," + d1 + "," + d2 + "," + d3 + "]";
						o3 = new Object();
						byte[] a = b1 = b2 = b3 = new byte[65535];
						random.nextBytes(a);
					}
					return this.hashCode() + "," + f1 + "," + f2 + "," + f3 + "," + d1 + "," + d2 + "," + d3 + "," + o1.hashCode() + "," + o2 + "," + o3;
				}
			};
			System.err.println(floatTest.toString());
			
			long ms = System.currentTimeMillis();
			ByteBuffer buffer = porter.writeObject(floatTest);
			long took = System.currentTimeMillis() - ms;
			System.err.println("Writing took:" + took + " ms.");
			ms = System.currentTimeMillis();
			Object floatTest2 = porter.readObject(buffer);
			took = System.currentTimeMillis() - ms;
			System.err.println("Reading took:" + took + " ms.");
			System.err.println(floatTest2.toString());
			
			FileOutputStream fos = new FileOutputStream("testport.po");
			fos.write(buffer.getBuffer());
			fos.close();
		}
	}
}
