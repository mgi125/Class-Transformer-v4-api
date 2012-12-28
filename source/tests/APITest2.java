package tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import mgi.tools.jtransformer.api.ClassNode;
import mgi.tools.jtransformer.api.Tree;

public class APITest2 {

	public static void main(String[] args) throws Throwable {
		long totalRead = 0;
		long totalWrite = 0;
		Tree tree = new Tree();
		
		JarFile infile = new JarFile(args.length > 0 ? args[0] : "test.jar");
		JarOutputStream outfile = new JarOutputStream(new FileOutputStream(new File((args.length > 0 ? args[0] : "test.jar").replace(".jar", "_trs.jar"))));
		Enumeration<JarEntry> entries = infile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			outfile.putNextEntry(new JarEntry(entry.getName()));
			if (!entry.isDirectory() && !entry.getName().endsWith(".class")) {
				InputStream in = infile.getInputStream(entry);
				for (int val = in.read(); val != -1; val = in.read())
					outfile.write(val);
			}
			else if (!entry.isDirectory()) {
				System.err.println("Processing " + entry.getName());
				byte[] buffer = new byte[500 * 1024];
				InputStream stream = infile.getInputStream(entry);
				int length = 0;
				for (int val = stream.read(); val != -1; val = stream.read())
					buffer[length++] = (byte) val;
				try {
					long rStart = System.currentTimeMillis();
					ClassNode n = new ClassNode(buffer, 0, length, ClassNode.OPT_GENERATION_SKIP_DEBUG_INFORMATION | ClassNode.OPT_OPTIMIZATION_SKIP_NONSYNTH_LOCALS);
					long rEnd = System.currentTimeMillis();
					totalRead += (rEnd - rStart);
					tree.add(n);
					long wStart = System.currentTimeMillis();
					byte[] out = n.toByteArray();
					long wEnd = System.currentTimeMillis();
					totalWrite += (wEnd - wStart);
					outfile.write(out);
				}
				catch (Throwable t) {
					t.printStackTrace();
					System.err.println("Failed to add:" + entry.getName());
					outfile.write(buffer, 0, length);
				}
			}
			outfile.closeEntry();
		}
		infile.close();
		outfile.close();
		
		
		
		for (int i = 0; i < 10; i++)
			System.gc();
		
		System.err.println("Ram usage:" + (((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024) + " mb.");
		System.err.println("Reading classes took:" + totalRead + " ms.");
		System.err.println("Compiling ast took:" + totalWrite + " ms.");
		System.err.println(tree);
	}

}
