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
	public static final int PARAMETER_NUMBER = 30;
	public static final String PARAMETER_FIELD_PREFIX = "__$$PARAMETER";
	public static final String RETURN_VALUE_NAME = "__$$RETURN_VAL";
	public static final String GET_RETURN_VALUE = "get" + RETURN_VALUE_NAME;
//	public static final String ARRAY_SHADOW_MAP = "__$$ARRAY_SHADOW_MAP";
//	public static final String GET_SHADOW_ARRAY = "getShadowArray";
//	private static final Type ARRAY_SHADOW_TYPE = Type.getType(ArrayShadowMap.class);
	public static final Type ARRAY_SHADOW_HOLDER = Type.getType(ArrayShadowMapHolder.class);

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

//	@Override
//	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//		if (name.equals("<init>")) {
//			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
//			return new ThreadConstructorVisitor(mv, access, name, desc);
//		} else {
//			return super.visitMethod(access, name, desc, signature, exceptions);
//		}
//	}

	@Override
	public void visitEnd() {
		emitFields();
		emitGetReturnValue();
		emitGetShadowArray();
		super.visitEnd();
	}

	private void emitFields() {
		for (int i = 0; i < ThreadInstrumenter.PARAMETER_NUMBER; i++) {
			int newAccess = ACC_PUBLIC;
			super.visitField(newAccess, ThreadInstrumenter.PARAMETER_FIELD_PREFIX + i, ShadowFieldRewriter.TAINT_TYPE
					.getDescriptor(), null, new Long(0));
		}
		super.visitField(ACC_PUBLIC, ThreadInstrumenter.RETURN_VALUE_NAME, ShadowFieldRewriter.TAINT_TYPE
				.getDescriptor(), null, new Long(0));
//		super.visitField(ACC_PUBLIC, ARRAY_SHADOW_MAP, ARRAY_SHADOW_TYPE.getDescriptor(), null, null);
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

	private void emitGetShadowArray() {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "getShadowArray", "(Ljava/lang/Object;)[J", null,
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
//	private class ThreadConstructorVisitor extends AdviceAdapter {
//
//		protected ThreadConstructorVisitor(MethodVisitor mv, int access, String name, String desc) {
//			super(mv, access, name, desc);
//			this.mv = mv;
//		}
//
//		private final MethodVisitor mv;
//		
//		protected void onMethodEnter() {
//			// Invoked only for the constructor of java.lang.Thread, initializes the ArrayShadowMap
////			mv.visitVarInsn(ALOAD, 0); // Loads "this"
////			mv.visitTypeInsn(NEW, "edu/stanford/pepe/ArrayShadowMap"); // Initializes the array
////			mv.visitInsn(DUP);
////			mv.visitMethodInsn(INVOKESPECIAL, "edu/stanford/pepe/ArrayShadowMap", "<init>", "()V");
////			mv.visitFieldInsn(PUTFIELD, "java/lang/Thread", "__$$ARRAY_SHADOW_MAP",
////					"Ledu/stanford/pepe/ArrayShadowMap;"); // Store the ArrayShadowMap in the field
//		}
//
//	}

}
