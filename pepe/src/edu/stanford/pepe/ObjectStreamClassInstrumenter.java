package edu.stanford.pepe;

import java.io.ObjectStreamClass;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;

/**
 * Special instrumenter for {@link ObjectStreamClass}. We need to modify the
 * serialVersionUid algorithm so that it ignores the shadow fields. This is
 * because certain applications store serialized objects without using a
 * serialVersionUid, and the injection of the shadow fields breaks them.
 * 
 * @author jtamayo
 */
public class ObjectStreamClassInstrumenter extends ClassAdapter implements Opcodes {

	public ObjectStreamClassInstrumenter(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (!name.equals("java/io/ObjectStreamClass")) {
			throw new IllegalArgumentException(
					"ObjectStreamClassInstrumenter is meant for instrumenting only ObjectStreamClass");
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		boolean isStatic = (access & ACC_STATIC) != 0;
		if (name.equals("computeDefaultSUID") && isStatic) {
			emitComputeDefaultSUID(access, name, desc, signature, exceptions);
			return null;
		} else if (name.equals("hasStaticInitializer") && isStatic) {
			int newAccess = (access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
			return super.visitMethod(newAccess, name, desc, signature, exceptions);
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	private void emitComputeDefaultSUID(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, "edu/stanford/pepe/SerialVersionUidGenerator", "computeDefaultSUID",
				"(Ljava/lang/Class;)J");
		mv.visitInsn(LRETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

}
