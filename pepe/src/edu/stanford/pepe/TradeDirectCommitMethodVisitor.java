package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

public class TradeDirectCommitMethodVisitor extends AdviceAdapter {

	private final String className;

	public TradeDirectCommitMethodVisitor(String className, MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
		this.className = className;
	}

	@Override
	protected void onMethodEnter() {
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(51, l0);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitLdcInsn("Commit " + className);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
	}

}
