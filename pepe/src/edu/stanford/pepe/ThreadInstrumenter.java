package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.Type;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

/**
 * Instruments java.lang.Thread, adding the fields to store the array shadow
 * map, the parameter and return values for methods.
 * 
 * @author jtamayo
 */
public class ThreadInstrumenter extends ClassAdapter implements Opcodes {

	/** Maximum number of parameters in any method call */
	public static final int PARAMETER_NUMBER = 60;
	public static final String PARAMETER_FIELD_PREFIX = "__$$PARAMETER";
	public static final String RETURN_VALUE_NAME = "__$$RETURN_VAL";
	public static final String GET_RETURN_VALUE = "get" + RETURN_VALUE_NAME;
	public static final String SET_RETURN_VALUE = "set" + RETURN_VALUE_NAME;
	public static final String ARRAY_SHADOW_MAP = "__$$ARRAY_SHADOW_MAP";
	public static final String GET_SHADOW_ARRAY = "getShadowArray";
	public static final String MEET_OPERATOR = "meet";
	private static final Type ARRAY_SHADOW_TYPE = Type.getType(ArrayShadowMap.class);

	private final ClassVisitor cw;

	public ThreadInstrumenter(ClassVisitor cv) {
		super(cv);
		this.cw = cv;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (!name.equals("java/lang/Thread")) {
			throw new IllegalArgumentException("ThreadInstrumenter is meant for instrumenting only java.lang.Thread");
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("<init>")) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			return new ThreadConstructorVisitor(mv, access, name, desc);
		} else {
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}

	@Override
	public void visitEnd() {
		emitFields();
		emitGetReturnValue();
		emitSetReturnValue();
		emitGetShadowArray();
		emitMeet();
		super.visitEnd();
	}

	private void emitMeet() {
		// Assembly code for the meet(long,long) method in this class
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "meet", "(JJ)J", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(123, l0);
		mv.visitVarInsn(LLOAD, 0);
		mv.visitLdcInsn(new Long(4294967295L));
		mv.visitInsn(LAND);
		mv.visitVarInsn(LLOAD, 2);
		mv.visitLdcInsn(new Long(4294967295L));
		mv.visitInsn(LAND);
		mv.visitInsn(LSUB);
		mv.visitVarInsn(LSTORE, 4);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(124, l1);
		mv.visitVarInsn(LLOAD, 4);
		mv.visitInsn(LCONST_0);
		mv.visitInsn(LCMP);
		Label l2 = new Label();
		mv.visitJumpInsn(IFNE, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(125, l3);
		mv.visitVarInsn(LLOAD, 0);
		mv.visitVarInsn(LLOAD, 2);
		mv.visitInsn(LOR);
		mv.visitInsn(LRETURN);
		mv.visitLabel(l2);
		mv.visitLineNumber(126, l2);
		mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { Opcodes.LONG }, 0, null);
		mv.visitVarInsn(LLOAD, 4);
		mv.visitInsn(LCONST_0);
		mv.visitInsn(LCMP);
		Label l4 = new Label();
		mv.visitJumpInsn(IFGE, l4);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitLineNumber(127, l5);
		mv.visitVarInsn(LLOAD, 2);
		mv.visitInsn(LRETURN);
		mv.visitLabel(l4);
		mv.visitLineNumber(129, l4);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(LLOAD, 0);
		mv.visitInsn(LRETURN);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLocalVariable("a", "J", null, l0, l6, 0);
		mv.visitLocalVariable("b", "J", null, l0, l6, 2);
		mv.visitLocalVariable("dif", "J", null, l1, l6, 4);
		mv.visitMaxs(6, 6);
		mv.visitEnd();

	}
	
	/**
	 * This method is never called; its bytecode is instead injected in java.lang.Thread.
	 * @param a
	 * @param b
	 * @return
	 */
	public static long meet(long a, long b) {
		long dif = (a & 0xFFFFFFFFL) - (b & 0xFFFFFFFFL);
		if (dif == 0) {
			return a | b;
		} else if (dif < 0) { //a-b < 0 -> a < b
			return b;
		} else {
			return a;
		}
	}

	private void emitFields() {
		for (int i = 0; i < ThreadInstrumenter.PARAMETER_NUMBER; i++) {
			int newAccess = ACC_PUBLIC;
			super.visitField(newAccess, ThreadInstrumenter.PARAMETER_FIELD_PREFIX + i, ShadowFieldRewriter.TAINT_TYPE
					.getDescriptor(), null, new Long(0));
		}
		super.visitField(ACC_PUBLIC, ThreadInstrumenter.RETURN_VALUE_NAME, ShadowFieldRewriter.TAINT_TYPE
				.getDescriptor(), null, new Long(0));
		super.visitField(ACC_PUBLIC, ARRAY_SHADOW_MAP, ARRAY_SHADOW_TYPE.getDescriptor(), null, null);
	}

	private void emitGetReturnValue() {
		MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, ThreadInstrumenter.GET_RETURN_VALUE, "()J", null,
				null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
		mv.visitVarInsn(ASTORE, 0);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitVarInsn(ALOAD, 0);
		Label l2 = new Label();
		mv.visitJumpInsn(IFNONNULL, l2);
		mv.visitInsn(LCONST_0);
		Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "java/lang/Thread", ThreadInstrumenter.RETURN_VALUE_NAME, "J");
		mv.visitLabel(l3);
		mv.visitInsn(LRETURN);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
	}
	
	private void emitSetReturnValue() {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, ThreadInstrumenter.SET_RETURN_VALUE, "(J)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(7, l0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
		mv.visitVarInsn(ASTORE, 2);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(8, l1);
		mv.visitVarInsn(ALOAD, 2);
		Label l2 = new Label();
		mv.visitJumpInsn(IFNULL, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(9, l3);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(LLOAD, 0);
		mv.visitFieldInsn(PUTFIELD, "java/lang/Thread", "__$$RETURN_VAL", "J");
		mv.visitLabel(l2);
		mv.visitLineNumber(11, l2);
		mv.visitInsn(RETURN);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
		
}

	private void emitGetShadowArray() {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_SHADOW_ARRAY, "(Ljava/lang/Object;)[J", null,
				null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(13, l0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
		mv.visitVarInsn(ASTORE, 1);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitVarInsn(ALOAD, 1);
		Label l2 = new Label();
		mv.visitJumpInsn(IFNULL, l2);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(GETFIELD, "java/lang/Thread", "__$$ARRAY_SHADOW_MAP", "Ledu/stanford/pepe/ArrayShadowMap;");
		mv.visitJumpInsn(IFNULL, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(15, l3);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(GETFIELD, "java/lang/Thread", "__$$ARRAY_SHADOW_MAP", "Ledu/stanford/pepe/ArrayShadowMap;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "edu/stanford/pepe/ArrayShadowMap", "getShadow", "(Ljava/lang/Object;)[J");
		mv.visitInsn(ARETURN);
		mv.visitLabel(l2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/reflect/Array", "getLength", "(Ljava/lang/Object;)I");
		mv.visitIntInsn(NEWARRAY, T_LONG);
		mv.visitInsn(ARETURN);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	/**
	 * For initializing the ArrayShadowMap in each thread.
	 * 
	 * @author jtamayo
	 */
	private class ThreadConstructorVisitor extends AdviceAdapter {

		protected ThreadConstructorVisitor(MethodVisitor mv, int access, String name, String desc) {
			super(mv, access, name, desc);
			this.mv = mv;
		}

		private final MethodVisitor mv;
		
		protected void onMethodEnter() {
			// Invoked only for the constructor of java.lang.Thread, initializes the ArrayShadowMap
			mv.visitVarInsn(ALOAD, 0); // Loads "this"
			mv.visitTypeInsn(NEW, "edu/stanford/pepe/ArrayShadowMap"); // Initializes the array
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "edu/stanford/pepe/ArrayShadowMap", "<init>", "()V");
			mv.visitFieldInsn(PUTFIELD, "java/lang/Thread", "__$$ARRAY_SHADOW_MAP",
					"Ledu/stanford/pepe/ArrayShadowMap;"); // Store the ArrayShadowMap in the field
		}

	}

}
