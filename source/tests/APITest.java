package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.objectweb.asm.Type;

import mgi.tools.jtransformer.api.ClassNode;
import mgi.tools.jtransformer.api.FieldNode;
import mgi.tools.jtransformer.api.MethodNode;
import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.tools.VariablesAnalyzer;
import mgi.tools.jtransformer.api.serialization.UnsafeSerializer;
import mgi.tools.jtransformer.utilities.ByteBuffer;

public class APITest {

	public static void main(String[] args) throws Throwable {
		File file = new File("bin/tests/testclass.class");
		byte[] data = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(data);
		fis.close();

		new ClassNode(data, 0, data.length, ClassNode.OPT_GENERATION_SKIP_DEBUG_INFORMATION); // warm up jvm
		
		System.err.println(Type.getType(Object.class).getDescriptor() + ":" + Type.getType(Object.class).getInternalName());
		
		long readbegin = System.currentTimeMillis();
		ClassNode clazz = new ClassNode(data, 0, data.length, ClassNode.OPT_GENERATION_SKIP_DEBUG_INFORMATION);
		long readend = System.currentTimeMillis() - readbegin;
		System.err.println("Class loading took - " + readend + " ms.");
		System.err.println("Class:" + clazz.toString());
		//for (FieldNode field : clazz.getFields())
		//	System.err.println("Field:" + field);
		for (MethodNode method : clazz.getMethods()) {
			System.err.println("Method:" + method);
			System.err.println(method.getCode());
		}
		byte[] saved = clazz.toByteArray();
		FileOutputStream out = new FileOutputStream(new File("workspace/tests/testclass.class"));
		out.write(saved, 0, saved.length);
		out.close();
		
		UnsafeSerializer serializer = new UnsafeSerializer();
		serializer.writeObject(new Object()); // warm up jvm
		
		long ms = System.currentTimeMillis();
		ByteBuffer buffer = serializer.writeObject(clazz);
		long savingTook = System.currentTimeMillis() - ms;
		System.err.println("Saving took - " + savingTook + " ms.");
		

		FileOutputStream fos = new FileOutputStream(new File("testclass.po"));
		fos.write(buffer.getBuffer());
		fos.close();
		
		
		for (MethodNode mn : clazz.getMethods()) {		
			VariablesAnalyzer analyzer = new VariablesAnalyzer(mn, mn.getCode());
			analyzer.analyze();
			
			System.err.println("vars{" + mn + "}");
			for (int i = 0; i < 100; i++) {
				List<VariablesAnalyzer.VariableInformation> vars = analyzer.find(i);
				if (vars == null)
					continue;

				int s = 0;
				for (VariablesAnalyzer.VariableInformation info : vars) {
					System.err.println("(" + i + ":" + (s++) + ")");
					for (AbstractCodeNode read : info.getReads())
						System.err.println(read);
					for (AbstractCodeNode write : info.getWrites())
						System.err.println(write);
					System.err.println("()");
				}

			}
		}
		
		System.gc();
		System.err.println("Ram usage:" + (((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024) + " mb.");
		System.err.println(clazz);
	}
}
