package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

/**
 * Instruments a JDBC connection so that on every commit/rollback it notifies
 * TransactionId.
 * 
 * @author jtamayo
 */
public class JdbcConnectionVisitor extends AdviceAdapter {

	public JdbcConnectionVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
	}

	@Override
	protected void onMethodEnter() {
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitMethodInsn(INVOKESTATIC, "edu/stanford/pepe/runtime/TransactionId", "onNewTransaction", "()V");
	}

}
