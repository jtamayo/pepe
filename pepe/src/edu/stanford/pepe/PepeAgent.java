package edu.stanford.pepe;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassReader;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.ClassWriter;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.FieldNode;
import edu.stanford.pepe.org.objectweb.asm.util.ASMifierClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.util.CheckClassAdapter;

/**
 * Main agent. Registers itself as a ClassFileTransformer with the JVM at
 * startup, and handles the top level instrumentation routines.
 * 
 * @author jtamayo
 */
public class PepeAgent implements ClassFileTransformer, Opcodes {

	public static final Logger logger = Logger.getLogger("edu.stanford.pepe");
	{
		for (Handler h : Logger.getLogger("").getHandlers()) {
			h.setLevel(Level.ALL);
		}
		logger.setLevel(Level.WARNING);
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
	@SuppressWarnings("unchecked")
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if ((!InstrumentationPolicy.isTypeInstrumentable(className) || InstrumentationPolicy
				.isSpecialJavaClass(className))
				&& !InstrumentationPolicy.isSpecialPepeClass(className)) {
			// Types in the ignore list, and special java classes must be ignored.
			// Special Pepe classes, even if in the ignore list, must be instrumented
			return null;
		}

		try {
			ClassNode cn = new ClassNode();
			ClassReader cr = new ClassReader(classfileBuffer);			
			cr.accept(cn, 0); // Makes the ClassReader visit the ClassNode
			
			for (FieldNode fn : (List<FieldNode>) cn.fields) {
				if (fn.name.equals(ShadowFieldRewriter.TAINT_MARK)) {
					logger.info("Skipping already instrumented class " + cn.name);
					return null;
				}
			}
			logger.info("Transforming class " + className + " with ClassLoader " + loader);
			return instrumentClass(cn);
		} catch (Exception t) {
			logger.severe("PEPE: Exception while transforming " + className);
			t.printStackTrace();
			throw new RuntimeException(t);
		} catch (Error e) {
			logger.severe("PEPE: Error while transforming " + className);
			e.printStackTrace();
			throw new Error(e);
		}
	}

	public static byte[] instrumentClass(ClassNode cn) {
		if (cn.name.equals("java/lang/Thread")) {
			final ClassWriter cw = new ClassWriter(0);
			ClassAdapter ca = new ThreadInstrumenter(cw);
			cn.accept(ca);
			return cw.toByteArray();
		} else if (cn.name.equals("java/io/ObjectStreamClass")) {
			final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassAdapter ca = new ObjectStreamClassInstrumenter(cw);
			cn.accept(ca);
			return cw.toByteArray();
		} else if (cn.name.equals("edu/stanford/pepe/TaintCheck")) {
			final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassAdapter ca = new TaintCheckInstrumenter(cw);
			cn.accept(ca);
			return cw.toByteArray();
		} else if (cn.name.equals("java/lang/StringBuffer") || cn.name.equals("java/lang/StringBuilder")
				|| cn.name.equals("java/lang/AbstractStringBuilder")) {
			System.out.println("Instrumenting " + cn.name);
			final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassAdapter ca = new StringBuilderInstrumenter(cw);
			cn.accept(ca);
			return cw.toByteArray();
		} else if (cn.name.equals("java/lang/String")) {
			final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassAdapter ca = new StringInstrumenter(cw);
			cn.accept(ca);
			return cw.toByteArray();
		} else if (InstrumentationPolicy.isPrimitive(cn.name)) {
			ClassWriter cw = new ClassWriter(0);
			ClassVisitor verifier = new CheckClassAdapter(cw); // For debugging purposes, the bytecode should be as sane as possible
			ShadowStackRewriter.rewrite(cn, verifier);
			return cw.toByteArray();
		} else {
			// First add the taint fields
			ShadowFieldRewriter.rewrite(cn);
			// Now add the shadow stack
			ClassWriter cw = new ClassWriter(0);
			ClassVisitor verifier = new CheckClassAdapter(cw); // For debugging purposes, the bytecode should be as sane as possible
			ShadowStackRewriter.rewrite(cn, verifier);
			return cw.toByteArray();
		}
	}

	/**
	 * For debugging purposes. Prints the disassembly list of the class,
	 * toghether with the inferred types in the stack and the local variables.
	 * 
	 * @param bs
	 */
	static void printClass(byte[] bs) {
		ClassReader cr = new ClassReader(bs);
		CheckClassAdapter.verify(cr, true, new PrintWriter(System.out));
	}

}
