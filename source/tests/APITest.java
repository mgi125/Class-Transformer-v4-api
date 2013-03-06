package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.objectweb.asm.Type;

import mgi.tools.jtransformer.ClassTransformer;
import mgi.tools.jtransformer.api.ClassNode;
import mgi.tools.jtransformer.api.FieldNode;
import mgi.tools.jtransformer.api.MethodNode;
import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.tools.VariablesAnalyzer;
import mgi.tools.jtransformer.api.code.tools.VariablesList;
import mgi.tools.jtransformer.api.serialization.UnsafeSerializer;
import mgi.tools.jtransformer.utilities.ByteBuffer;

public class APITest {

	public static void main(String[] args) throws Throwable {
		ClassTransformer api = ClassTransformer.loadAPI();
		
		File file = new File("bin/tests/testclass.class");
		byte[] data = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(data);
		fis.close();

		new ClassNode(api, data, 0, data.length, ClassNode.OPT_GENERATION_SKIP_DEBUG_INFORMATION); // warm up jvm
		
		long readbegin = System.currentTimeMillis();
		ClassNode clazz = new ClassNode(api, data, 0, data.length, ClassNode.OPT_GENERATION_SKIP_DEBUG_INFORMATION | ClassNode.OPT_OPTIMIZATION_SKIP_NONSYNTH_LOCALS);
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
		
		
		for (MethodNode mn : clazz.getMethods()) {		
			VariablesList varslist = new VariablesList();
			varslist.generate(mn.getCode());
			
			System.err.println("vars{" + mn + "}");
			for (int i = 0; i < 100; i++) {
				List<VariablesList.VariableInformation> vars = varslist.find(i);
				if (vars == null)
					continue;

				int s = 0;
				for (VariablesList.VariableInformation info : vars) {
					System.err.println("(" + i + ":" + (s++) + " " + info.getDeclaration().getType()  + ")");
					for (AbstractCodeNode read : info.getReads())
						System.err.println(read);
					for (AbstractCodeNode write : info.getWrites())
						System.err.println(write);
				}

			}
		}
		
		System.gc();
		System.err.println("Ram usage:" + (((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024) + " mb.");
		System.err.println(clazz);
	}
}
