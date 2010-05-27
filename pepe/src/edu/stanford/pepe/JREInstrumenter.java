package edu.stanford.pepe;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JREInstrumenter {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ZipInputStream is = new ZipInputStream(new FileInputStream("jre/rt_macos_1_6.jar"));
		ZipEntry je;
		ZipOutputStream os = new ZipOutputStream(new FileOutputStream("jre/rt_instrumented.jar"));
		while ((je = is.getNextEntry()) != null) {
			System.out.println(je.getName());
				byte[] byteArray = read(is);
				if (je.getName().endsWith(".class")) {
					byteArray = PepeAgent.instrumentClass(byteArray);
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
