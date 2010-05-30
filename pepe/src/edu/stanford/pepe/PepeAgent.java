package edu.stanford.pepe;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.pepe.modifiedasm.EnhancedClassNode;
import edu.stanford.pepe.org.objectweb.asm.ClassReader;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.ClassWriter;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.FieldNode;
import edu.stanford.pepe.org.objectweb.asm.util.CheckClassAdapter;

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
		if (!InstrumentationPolicy.isTypeInstrumentable(className)) {
			return null;
		}
		
		try {
			logger.fine("Transforming class " + className + " with ClassLoader " + loader);
			byte[] b = instrumentClass(classfileBuffer);
			return b;
		} catch (Throwable t) {
			logger.severe("PEPE: Exception while transforming " + className);
			t.printStackTrace();
			throw new RuntimeException(t);
		}
	}

	@SuppressWarnings("unchecked")
	public static byte[] instrumentClass(byte[] classfileBuffer) {
		EnhancedClassNode cn = new EnhancedClassNode();
		if (!InstrumentationPolicy.isTypeInstrumentable(cn)) {
			// TODO: Optimize: there are two calls to isTypeInstrumentable. The problem is, if I don't do two calls I'd have to split the instrumentation, or I'd have to build a classnode without need
			return null;
		}
		for (FieldNode fn : (List<FieldNode>)cn.fields) {
			if (fn.name.equals(ShadowFieldRewriter.TAINT_MARK)) {
				System.out.println("Skipping already instrumented class " + cn.name);
				return null;
			}
		}
		ClassReader cr = new ClassReader(classfileBuffer);
		cr.accept(cn, 0); // Makes the ClassReader visit the ClassNode
		return instrumentClass(cn);
	}

	public static byte[] instrumentClass(EnhancedClassNode cn) {
		// First add the taint fields
		ShadowFieldRewriter.rewrite(cn);
		// Now add the shadow stack
		ClassWriter cw = new ClassWriter(0);
		ClassVisitor verifier = new CheckClassAdapter(cw); // For debugging purposes, the bytecode should be as sane as possible
		ShadowStackRewriter.rewrite(cn, verifier);
		return cw.toByteArray();
	}

}
