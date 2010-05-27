package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.MethodAdapter;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;

/**
 * Inserts a NO-OP instruction at the beginning of every method.
 * 
 * @author juanmtamayo
 */
public class NoOpAdapter extends MethodAdapter implements Opcodes {

	public NoOpAdapter(MethodVisitor mv) {
		super(mv);
	}

	@Override
	public void visitCode() {
		mv.visitCode();
		mv.visitInsn(NOP);
	}

}
