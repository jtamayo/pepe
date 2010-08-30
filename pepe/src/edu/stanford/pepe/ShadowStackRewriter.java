package edu.stanford.pepe;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.aj.prep.FrameAnalyzer;
import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.MethodAdapter;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.MethodNode;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Frame;

public class ShadowStackRewriter implements Opcodes {
	public static Logger logger = Logger.getLogger(ShadowStackRewriter.class.getName());
	{
		logger.setLevel(Level.INFO);
	}

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
		if (InstrumentationPolicy.isPrimitive(cn.name) && mn.name.equals("<init>")) {
			System.out.println("Instrumenting " + cn.name);
			v = new PrimitiveWrapperConstructorVisitor(outputMethodVisitor, mn.access, mn.name, mn.desc);
		} else if ("commit".equals(mn.name)){
			System.out.println("Instrumenting TradeDirect");
			v = new PlainMethodVisitor(new TradeDirectCommitMethodVisitor(cn.name, outputMethodVisitor, mn.access, mn.name, mn.desc), mn, frames, cn);
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
