package edu.stanford.pepe;


import java.util.List;
import java.util.logging.Logger;

import edu.stanford.aj.prep.FrameAnalyzer;
import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.MethodNode;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Frame;

public class ShadowStackRewriter implements Opcodes {
	public static Logger logger = Logger.getLogger("edu.stanford.pepe.ShadowStackRewriter");
	
	@SuppressWarnings("unchecked")
	public static void rewrite(ClassNode cn, ClassVisitor output) {
		// Switch to version 1.5, to avoid generating stack frames
		cn.version = V1_5;
		
		// First output the entire class, except the methods and the visitEnd
		copyAllButMethods(cn, output);

		// Now instrument each method
		List<MethodNode> methods = cn.methods;

		for (MethodNode mn : methods) {
			Frame[] frames = FrameAnalyzer.getInstance().compute(cn.name, mn);
			MethodVisitor outputMethodVisitor = output.visitMethod(mn.access, mn.name, mn.desc, mn.signature,
					(String[]) mn.exceptions.toArray(new String[]{})); // This line would be called by the copyAllButMethods, but we're skipping it
			MethodVisitor v = buildMethodVisitor(cn, mn, frames, outputMethodVisitor);
			mn.accept(v);
		}
		
		// Every method has been visited, all that remains is to call visitEnd on the output
		output.visitEnd();
	}

	private static MethodVisitor buildMethodVisitor(ClassNode cn, MethodNode mn, Frame[] frames,
			MethodVisitor outputMethodVisitor) {
		MethodVisitor v;
		
		if ("org/postgresql/jdbc2/AbstractJdbc2Connection".equals(cn.name)) {
		    System.out.println(" YES! Instrumenting org/postgresql/jdbc2/AbstractJdbc2Connection");
		}
		
		if (InstrumentationPolicy.isPrimitive(cn.name) && mn.name.equals("<init>")) {
			logger.info("Instrumenting " + cn.name);
			v = new PrimitiveWrapperConstructorVisitor(outputMethodVisitor, mn.access, mn.name, mn.desc);
		} else if (
			("org/tranql/connector/jdbc/ConnectionHandle".equals(cn.name) 
				|| "org/h2/jdbc/JdbcConnection".equals(cn.name)
				|| "org/apache/derby/client/am/Connection".equals(cn.name)
				|| "org/apache/derby/impl/jdbc/EmbedConnection".equals(cn.name)
				|| "org/hsqldb/jdbc/jdbcConnection".equals(cn.name)
				|| "org/postgresql/jdbc2/AbstractJdbc2Connection".equals(cn.name)
				) 
			&& (("commit".equals(mn.name) || "rollback".equals(mn.name)) && "()V".equals(mn.desc))) 
//			&& (("commit".equals(mn.name) || "rollback".equals(mn.name)) && "()V".equals(mn.desc)) 
//			    || ("setAutoCommit".equals(mn.name)))
			{
			logger.warning("Instrumenting " + cn.name + " " + mn.name + mn.desc);
			v = new PlainMethodVisitor(new JdbcConnectionVisitor(outputMethodVisitor, mn.access, mn.name, mn.desc), mn, frames, cn);
		} else {
			v = new PlainMethodVisitor(outputMethodVisitor, mn, frames, cn);
		}
		return v;
	}

	/** Transfer all events except visitMethod and visitEnd. */
	private static void copyAllButMethods(final ClassNode root, final ClassVisitor output) {
		root.accept(new ClassAdapter(output) {
			@Override
			public MethodVisitor visitMethod(final int access, final String name, final String desc,
					final String signature, final String[] exceptions) {
				return null;
			}

			@Override
			public void visitEnd() {
			}
		});
	}
}
