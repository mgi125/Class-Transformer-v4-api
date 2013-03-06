package tests;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.objectweb.asm.Type;

import mgi.tools.jtransformer.ClassTransformer;
import mgi.tools.jtransformer.api.TypesLoader;

public class TypeLoaderTest {

	public TypeLoaderTest() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		doBenchmark(10, Type.getType(ClassTransformer.class), Type.getType(MulTest.class), Type.getType(StackFramesTest.class), Type.getType(StackFramesTest2.class)); // warmup
		
		double[] result = doBenchmark(2, Type.getType(ByteArrayOutputStream.class), Type.getType(OutputStream.class));
		for (int i = 0; i < result.length; i++)
			System.err.println("Attempt #" + i + " took " + result[i] + " ms.");
		
		TypesLoader loader = ClassTransformer.loadAPI().getTypesLoader();
		
		System.err.println(loader.getCommonReferenceType(Type.getType(x[][][].class), Type.getType(y[][].class)).getInternalName().replace('/', '.'));
		
		System.err.println((x() ? new x[10][10][10] : new y[10][10])[0]);
		System.err.println((x() ? new x[100][100][100] : new y[100])[0]);
	}
	
	
	public static double[] doBenchmark(int times, Type... types) {
		TypesLoader loader = ClassTransformer.loadAPI().getTypesLoader();
		double[] benchmark = new double[times];
		for (int i = 0; i < times; i++) {
			long start = System.nanoTime();
			for (int x = 0; x < types.length; x += 2)
				loader.getCommonReferenceType(types[x], types[x + 1]);
			long end = System.nanoTime();
			benchmark[i] = (double)(end - start) / 1000000D;
		}
		return benchmark;
	}
	
	static boolean x() {
		return true;
	}
	
	static class x {
		
	}
	
	static class y extends x {
		
	}

}
