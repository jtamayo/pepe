package edu.stanford.pepe;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.pepe.org.objectweb.asm.ClassReader;
import edu.stanford.pepe.org.objectweb.asm.ClassWriter;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;

/**
 * Main agent. Registers itself as a ClassFileTransformer with the JVM at
 * startup, and handles the top level instrumentation routines.
 * 
 * @author jtamayo
 */
public class PepeAgent implements ClassFileTransformer {

	public static final Logger logger = Logger.getLogger("edu.stanford.pepe");
	{
		for (Handler h : Logger.getLogger("").getHandlers()) {
			h.setLevel(Level.ALL);
		}
		logger.setLevel(Level.INFO);
	}

	/**
	 * Invoked by the JVM before starting the main program.
	 * 
	 * @param agentArgs
	 *            arguments passed to the agent
	 * @param inst
	 *            Object for registering a class transformer
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		logger.info("Pepe agent started");
		logger.info("Using classloader " + PepeAgent.class.getClassLoader());

		inst.addTransformer(new PepeAgent());
		printLoadedClasses(inst);
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.severe("Death of thread " + t.getName());
				e.printStackTrace();
				logger.severe("System class loader: " + printClassLoader(ClassLoader.getSystemClassLoader()));
				logger.severe("Context class loader: " + printClassLoader(t.getContextClassLoader()));
			}
		});
	}

	private static void printLoadedClasses(Instrumentation inst) {
		System.out.println(" --- All loaded classes --- ");
		Class[] allLoadedClasses = inst.getAllLoadedClasses();
		
		for (Class c : allLoadedClasses) {
			System.out.println(c);
		}
		
	}

	/**
	 * Returns a pretty description of a classloader for debugging purposes.
	 */
	public static String printClassLoader(ClassLoader cl) {
		StringBuilder sb = new StringBuilder();
		if (cl instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader) cl;
			sb.append(ucl);
			sb.append("\n");
			for (Object o : ucl.getURLs()) {
				sb.append(o);
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Invoked by the JVM when a class is first loaded. Initiates the bytecode
	 * transformation.
	 */
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (IgnoreList.ignoreClass(className, loader)) {
			return null;
		}

		try {
			logger.fine("Transforming class " + className + " with ClassLoader " + loader);
			byte[] b = instrumentClass(classfileBuffer);
			return b;
		} catch (Throwable t) {
			logger.severe("PEPE: Exception while transforming " + className);
			t.printStackTrace();
			System.exit(1);
			throw new RuntimeException(t);
		}
	}

	public static byte[] instrumentClass(byte[] classfileBuffer) {
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(classfileBuffer);
		cr.accept(cn, 0); // Makes the ClassReader visit the ClassNode
		
		ShadowFieldRewriter.rewrite(cn);

		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		byte[] b = cw.toByteArray();
		return b;
	}

}
