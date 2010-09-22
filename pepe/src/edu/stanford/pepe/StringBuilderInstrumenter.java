package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.Type;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

public class StringBuilderInstrumenter extends ClassAdapter implements Opcodes {

	static final Method[] modifiers = new Method[] { new Method("setLength", "(I)V"), new Method("setCharAt", "(IC)V"),
			new Method("append", "(Ljava/lang/Object;)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(Ljava/lang/String;)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(Ljava/lang/StringBuffer;)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(Ljava/lang/CharSequence;)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(Ljava/lang/CharSequence;II)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "([C)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "([CII)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(Z)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(C)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(I)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(J)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(F)Ljava/lang/AbstractStringBuilder;"),
			new Method("append", "(D)Ljava/lang/AbstractStringBuilder;"),
			new Method("delete", "(II)Ljava/lang/AbstractStringBuilder;"),
			new Method("appendCodePoint", "(I)Ljava/lang/AbstractStringBuilder;"),
			new Method("deleteCharAt", "(I)Ljava/lang/AbstractStringBuilder;"),
			new Method("replace", "(IILjava/lang/String;)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(I[CII)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(ILjava/lang/Object;)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(ILjava/lang/String;)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(I[C)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(ILjava/lang/CharSequence;)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(ILjava/lang/CharSequence;II)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(IZ)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(IC)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(II)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(IJ)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(IF)Ljava/lang/AbstractStringBuilder;"),
			new Method("insert", "(ID)Ljava/lang/AbstractStringBuilder;"),
			new Method("reverse", "()Ljava/lang/AbstractStringBuilder;"),
			new Method("<init>", "(Ljava/lang/String;)V")};

	static final Method[] readers = new Method[] { new Method("length", "()I"), new Method("capacity", "()I"),
			new Method("charAt", "(I)C"), new Method("codePointAt", "(I)I"), new Method("codePointBefore", "(I)I"),
			new Method("codePointCount", "(II)I"), new Method("offsetByCodePoints", "(II)I"),
			new Method("substring", "(I)Ljava/lang/String;"),
			new Method("subSequence", "(II)Ljava/lang/CharSequence;"),
			new Method("subSequence", "(II)Ljava/lang/CharSequence;"),
			new Method("substring", "(II)Ljava/lang/String;"), new Method("indexOf", "(Ljava/lang/String;)I"),
			new Method("indexOf", "(Ljava/lang/String;I)I"), new Method("lastIndexOf", "(Ljava/lang/String;)I"),
			new Method("toString", "()Ljava/lang/String;"),
			new Method("lastIndexOf", "(Ljava/lang/String;I)I") };

	static final String SINGLE_OBJECT_TAINT = "__$OBJECT_TAINT$__";
	/*
	 * So, there's a few things I need to do: - Add the taint to the super class
	 * only. That means, add it to AbstractStringBuilder. - Modify every method
	 * in both the super class and the subclasses depending on whether they're
	 * read or write.
	 */

	private String name;

	public StringBuilderInstrumenter(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (!name.equals("java/lang/StringBuffer") && !name.equals("java/lang/StringBuilder")
				&& !name.equals("java/lang/AbstractStringBuilder")) {
			throw new IllegalArgumentException(
					"StringBuilderInstrumenter is meant for instrumenting only StringBuilder or StringBuffer, not " + name + ".");
		}
		super.visit(version, access, name, signature, superName, interfaces);

		this.name = name;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if ((access & ACC_ABSTRACT) != 0) {
			System.out.println("Method " + name + desc + "is abstract.");
			return mv;
		} else if (isModifier(name, desc)) {
			return new ModifierMethodVisitor(mv, access, name, desc);
		} else if (isReader(name, desc)) {
			return new ReaderMethodVisitor(mv, access, name, desc);
		} else {
			return mv;
		}
	}

	@Override
	public void visitEnd() {
		emitFields();
		super.visitEnd();
	}

	private boolean isReader(String name, String desc) {
		for (int i = 0; i < readers.length; i++) {
			if (readers[i].matches(name, desc)) {
				return true;
			}
		}
		return false;
	}

	private boolean isModifier(String name, String desc) {
		if ("append".equals(name) || "insert".equals(name)) {
			return true;
		}
		for (int i = 0; i < modifiers.length; i++) {
			if (modifiers[i].matches(name, desc)) {
				return true;
			}
		}
		return false;
	}

	private void emitFields() {
		// Only the super class keeps the taint
		if (this.name.equals("java/lang/AbstractStringBuilder")) {
			super.visitField(ACC_PUBLIC, SINGLE_OBJECT_TAINT, ShadowFieldRewriter.TAINT_TYPE.getDescriptor(), null,
					new Long(0));
		}
	}

	private static class Method {
		String name;
		String signature;

		public Method(String name, String signature) {
			this.name = name;
			this.signature = signature;
		}

		public boolean matches(String name, String signature) {
			return this.name.equals(name) && this.signature.equals(signature);
		}

	}

}

/**
 * Instruments a method that modifies the object. Merges the taint of the
 * parameters with the taint of the object.
 * 
 * @author jtamayo
 */
class ModifierMethodVisitor extends AdviceAdapter {

	private final String name;

	protected ModifierMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
		this.name = name;
	}

	/**
	 * Merge the taint of every parameter with the object taint.
	 */
	@Override
	protected void onMethodEnter() {
		System.out.println("Instrumenting " + name + methodDesc);
		
		Type[] arguments = Type.getArgumentTypes(methodDesc);
		MethodVisitor output = mv;

		// Thread t = Thread.currentThread();
		Label l0 = new Label();
		output.visitLabel(l0);
		int currentThreadIndex = newLocal(Type.getType(Thread.class));
		output.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
		output.visitVarInsn(ASTORE, currentThreadIndex);

		// if (t != null)
		Label l1 = new Label();
		output.visitLabel(l1);
		output.visitVarInsn(ALOAD, currentThreadIndex);
		Label l2 = new Label();
		output.visitJumpInsn(IFNONNULL, l2);
		Label l3 = new Label();
		output.visitJumpInsn(GOTO, l3);
		output.visitLabel(l2);
		for (int i = 0; i < arguments.length; i++) {
			// this.ObjectTaint = meet(this.ObjectTaint, parameterTaint[i]);
			output.visitVarInsn(ALOAD, 0); // this
			output.visitVarInsn(ALOAD, currentThreadIndex); // this, t
			output.visitFieldInsn(GETFIELD, "java/lang/Thread", ThreadInstrumenter.PARAMETER_FIELD_PREFIX + i, "J"); // this, parameterTaint[i]
			output.visitVarInsn(ALOAD, 0); // this, parameterTaint[i], this
			output.visitFieldInsn(GETFIELD, "java/lang/AbstractStringBuilder",
					StringBuilderInstrumenter.SINGLE_OBJECT_TAINT, "J"); // this, parameterTaint[i], this.ObjectTaint
			output.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "meet", "(JJ)J"); // this, meet(x,y)
			output.visitFieldInsn(PUTFIELD, "java/lang/AbstractStringBuilder",
					StringBuilderInstrumenter.SINGLE_OBJECT_TAINT, "J");
		}
		output.visitLabel(l3);
		output.visitLocalVariable("currentThread__PEPE__", "Ljava/lang/Thread;", null, l0, l3, currentThreadIndex);
	}
}

/**
 * Instruments a method that reads data from the object. Taints the return
 * values with the taint from the object.
 * 
 * @author jtamayo
 */
class ReaderMethodVisitor extends AdviceAdapter {

	protected ReaderMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
	}

	@Override
	protected void onMethodExit(int opcode) {
		MethodVisitor output = mv;

		// Thread t = Thread.currentThread();
		int currentThreadIndex = newLocal(Type.getType(Thread.class));
		output.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
		output.visitVarInsn(ASTORE, currentThreadIndex);

		// if (t != null)
		Label l1 = new Label();
		output.visitLabel(l1);
		output.visitVarInsn(ALOAD, currentThreadIndex);
		Label l2 = new Label();
		output.visitJumpInsn(IFNONNULL, l2);
		Label l3 = new Label();
		output.visitJumpInsn(GOTO, l3);
		output.visitLabel(l2);
		// t.returnVal = this.ObjectTaint
		output.visitVarInsn(ALOAD, currentThreadIndex); // t
		output.visitVarInsn(ALOAD, 0); // t, this
		output.visitFieldInsn(GETFIELD, "java/lang/AbstractStringBuilder",
				StringBuilderInstrumenter.SINGLE_OBJECT_TAINT, "J"); // t, this.ObjectTaint
//		
//		// HACK
//		output.visitInsn(POP2);
//		output.visitLdcInsn(324243l);
//		// ENDHACK
//		
		output.visitFieldInsn(PUTFIELD, "java/lang/Thread", ThreadInstrumenter.RETURN_VALUE_NAME, "J");
		output.visitLabel(l3);
	}

}
