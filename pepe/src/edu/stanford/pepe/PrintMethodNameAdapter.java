package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.FieldVisitor;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

/**
 * Prints the name of each method invoked.
 * 
 * @author juanmtamayo
 */
public class PrintMethodNameAdapter extends ClassAdapter implements Opcodes {

	private String className;
	private boolean isInterface;
	private boolean isAbstract;

	public PrintMethodNameAdapter(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, makePublic(access), name, signature, superName, interfaces);
		this.className = name;
		isInterface = (access & ACC_INTERFACE) != 0;
		isAbstract = (access & ACC_ABSTRACT) != 0;

	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// First visit the new field, call visitEnd, and then return the
		// FieldVisitor created by super
		boolean isFinal = (access & ACC_FINAL) != 0;
		boolean isStatic = (access & ACC_STATIC) != 0;
		if (!isInterface && !isAbstract && !isFinal & !isStatic) {
			String fieldName = name + "$$__PEPE__";
			FieldVisitor fv = cv.visitField(ACC_PRIVATE & ACC_TRANSIENT, fieldName, "I", null, null);
			fv.visitEnd();
			// fv.visitEnd(); TODO: Should I call this method or not?
		}
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
		if (this.className != null && !isInterface) {
			try {
				System.err.println("PEPE: Instrumenting " + className + " method " + name);
				visitor = new PrintNameAdapter(visitor, access, name, desc);
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return visitor;
	}

	class PrintNameAdapter extends AdviceAdapter {

		private String methodName;

		protected PrintNameAdapter(MethodVisitor mv, int access, String name, String desc) {
			super(mv, access, name, desc);
			this.methodName = name;
		}

		@Override
		protected void onMethodEnter() {
			mv.visitFieldInsn(GETSTATIC, "edu/stanford/pepe/Holder", "listener", "Ledu/stanford/pepe/AgentListener;");
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methodName);
			mv.visitMethodInsn(INVOKEINTERFACE, "edu/stanford/pepe/AgentListener", "onMethodEntry",
					"(Ljava/lang/String;Ljava/lang/String;)V");
//			mv.visitFieldInsn(GETSTATIC, "test4/WeirdObject", "listener", "Ltest4/WeirdInterface;");
//			mv.visitLdcInsn(className);
//			mv.visitLdcInsn(methodName);
//			mv.visitMethodInsn(INVOKEINTERFACE, "test4/WeirdInterface", "onMethodEntry",
//					"(Ljava/lang/String;Ljava/lang/String;)V");
		}

		@Override
		protected void onMethodExit(int opcode) {
//			mv.visitFieldInsn(GETSTATIC, "stanford/cs/pepe/PepeAgent", "listener", "Lstanford/cs/pepe/AgentListener;");
//			mv.visitLdcInsn(className);
//			mv.visitLdcInsn(methodName);
//			mv.visitMethodInsn(INVOKEINTERFACE, "stanford/cs/pepe/AgentListener", "onMethodExit",
//					"(Ljava/lang/String;Ljava/lang/String;)V");
		}
	}

	public static int makePublic(int access) {
		return (access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
	}

}
