package edu.stanford.pepe;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import edu.stanford.pepe.org.objectweb.asm.ClassReader;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;

/**
 * Class for instrumenting rt.jar, the container for all core JVM classes.
 * 
 * @author jtamayo
 */
public class JREInstrumenter {

	private static final String INPUT = "jre/macos_1.6/classes.jar";
	private static final String OUTPUT = "jre/rt_instrumented.jar";

	public static void main(String[] args) throws IOException {
		ZipInputStream is = new ZipInputStream(new FileInputStream(INPUT));
		ZipEntry je;
		ZipOutputStream os = new ZipOutputStream(new FileOutputStream(OUTPUT));
		while ((je = is.getNextEntry()) != null) {
			byte[] byteArray = read(is);
			if (je.getName().endsWith(".class")) {
				ClassNode cn = new ClassNode();
				ClassReader cr = new ClassReader(byteArray);
				cr.accept(cn, 0); // Makes the ClassReader visit the ClassNode

				if (InstrumentationPolicy.isTypeInstrumentable(cn.name)
						|| InstrumentationPolicy.isSpecialJavaClass(cn.name)) {
					byteArray = PepeAgent.instrumentClass(cn);
				} else {
					System.out.println("Skipping " + cn.name);
				}

			}
			JarEntry newJarEntry = new JarEntry(je.getName());
			os.putNextEntry(newJarEntry);
			os.write(byteArray);
		}
		is.close();
		os.close();
	}

	private static byte[] read(ZipInputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		int count;
		byte data[] = new byte[2048];
		while ((count = is.read(data, 0, 2048)) != -1) {
			baos.write(data, 0, count);
		}
		return baos.toByteArray();
	}

}
