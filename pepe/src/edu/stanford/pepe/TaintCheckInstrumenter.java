package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;

public class TaintCheckInstrumenter extends ClassAdapter implements Opcodes {

	public TaintCheckInstrumenter(ClassVisitor cv) {
		super(cv);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("getTaint") && desc.equals("(I)J")) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			mv.visitCode();
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitFieldInsn(GETFIELD, "java/lang/Thread", ThreadInstrumenter.PARAMETER_FIELD_PREFIX + "0", null);
			mv.visitInsn(LRETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
			return null;
		} else {
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}

}
