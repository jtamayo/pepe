/**
 * 
 */
package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

/**
 * MethodVisitor for instrumenting the constructor of primitive types. Unlike
 * regular methods and constructors, primitive wrappers cannot store the taint
 * of their parameters on shadow fields; instead, they copy the taint from their
 * arguments to Thread.RETURN_VALUE. It is the caller's responsibility to then
 * take this value and taint the object reference to which it points to.
 * 
 * @author jtamayo
 */
public class PrimitiveWrapperConstructorVisitor extends AdviceAdapter {

	private final MethodVisitor mv;

	protected PrimitiveWrapperConstructorVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
		this.mv = mv;
	}

	@Override
	protected void onMethodEnter() {
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;"); // ...,currentThread
		mv.visitInsn(DUP); // ...,currentThread, currentThread
		Label l2 = new Label();
		mv.visitJumpInsn(IFNONNULL, l2); // ...,currentThread
		Label l3 = new Label();
		mv.visitInsn(POP); // ...,
		mv.visitJumpInsn(GOTO, l3);
		// For primitive wrappers there's always only one parameter, and it's in the local variable 1
		mv.visitLabel(l2);
		mv.visitInsn(DUP); // ..., currentthread, currentthread
		mv.visitFieldInsn(GETFIELD, "java/lang/Thread", ThreadInstrumenter.PARAMETER_FIELD_PREFIX + 1, "J"); //...,currentThread,parameter1
		mv.visitFieldInsn(PUTFIELD, "java/lang/Thread", ThreadInstrumenter.RETURN_VALUE_NAME, "J"); //...,
		mv.visitLabel(l3);
		Label l4 = new Label();
		mv.visitLabel(l4);
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack + 2, maxLocals);
	}

}