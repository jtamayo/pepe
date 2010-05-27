package edu.stanford.pepe;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodAdapter;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;

/**
 * Instruments every instruction to insure the taint is propagated across
 * instructions.
 * 
 * @author jtamayo
 */
public class ShadowStackVisitor extends ClassAdapter implements Opcodes {
	public static final int SHADOW_FIELD_SIZE = 2;

	public static Logger logger = Logger.getLogger("edu.stanford.pepe.ShadowStackVisitor");

	/**
	 * Used to determine how many local variables are there in a method, and
	 * thus where should the shadow copies be located.
	 */
	private final MaxSizeVisitor maxSizeVisitor;
	{
		logger.setLevel(Level.ALL);
	}

	public ShadowStackVisitor(ClassVisitor cv, MaxSizeVisitor maxSizeVisitor) {
		super(cv);
		this.maxSizeVisitor = maxSizeVisitor;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return new ShadowStackMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
				maxSizeVisitor.getMaxLocals(name, desc), maxSizeVisitor.getMaxStack(name, desc));
	}

	/**
	 * The structure of the local vars will be as follows:
	 * |original_vars|shadow_local_vars|stack_workspace|
	 * 
	 * @author jtamayo
	 */
	class ShadowStackMethodVisitor extends MethodAdapter {

		private final int maxLocals;
		private final int maxStack;

		public ShadowStackMethodVisitor(MethodVisitor mv, int maxLocals, int maxStack) {
			super(mv);
			this.maxLocals = maxLocals;
			this.maxStack = maxStack;
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			assert maxStack == this.maxStack : "The maxStack value stored in the map does not match the received maxSTack value";
			assert maxLocals == this.maxLocals : "The maxLocals value stored in the map does not match the received maxLocals value";

			int newMaxStack = (1 + SHADOW_FIELD_SIZE) * maxStack;
			int newMaxLocals = (1 + SHADOW_FIELD_SIZE) * maxLocals + newMaxStack;
			logger.finest("Changing maxStack, maxLocals from (" + maxStack + "," + maxLocals + ") to (" + newMaxStack
					+ "," + newMaxLocals + ")");
			super.visitMaxs(maxStack, newMaxLocals);
		}

		/**
		 * Prepares the stack for an array store when the newValue is a
		 * single-word value. After preparing the stack invokes the final
		 * opcode given.
		 * 
		 * @param xStoreOpCode
		 *            the opcode with which to store the newValue in a local
		 *            variable
		 * @param xLoadOpCode
		 *            the opcode with which to load the newValue from a local
		 *            variable
		 */
		private void simpleArrayStore(int xStoreOpCode, int xLoadOpCode, int shadowStackStart, int opcode) {
			// before: ...,shadowArrayRef, arrayRef, shadowIndex, arrayIndex, shadowNewValue, newValue
			// after: ...,arrayRef, arrayIndex, newValue
			mv.visitVarInsn(xStoreOpCode, shadowStackStart + 0); // shadow[0] = newValue
			mv.visitVarInsn(LSTORE, shadowStackStart + 1); // shadow[1-2] = shadowNewValue
			mv.visitVarInsn(ISTORE, shadowStackStart + 3); // shadow[3] = arrayIndex
			mv.visitVarInsn(LSTORE, shadowStackStart + 4); // shadow[4-5] = shadowArrayIndex
			mv.visitVarInsn(ASTORE, shadowStackStart + 6); // shadow[6] = arrayRef
			mv.visitVarInsn(LSTORE, shadowStackStart + 7); // shadow[7-8] = shadowArrayRef
			mv.visitVarInsn(ALOAD, shadowStackStart + 6); // arrayRef
			mv.visitVarInsn(ILOAD, shadowStackStart + 3); // arrayIndex
			mv.visitVarInsn(ILOAD, shadowStackStart + 0); // newValue
			mv.visitInsn(opcode);
		}
		
		/**
		 * Prepares the stack for an array store when the newValue is a
		 * double-word value (like a long, or double). After preparing the stack invokes the final
		 * opcode given.
		 * 
		 * @param xStoreOpCode
		 *            the opcode with which to store the newValue in a local
		 *            variable
		 * @param xLoadOpCode
		 *            the opcode with which to load the newValue from a local
		 *            variable
		 */
		private void doubleArrayStore(int xStoreOpCode, int xLoadOpCode, int shadowStackStart, int opcode) {
			// before: ...,shadowArrayRef, arrayRef, shadowIndex, arrayIndex, shadowNewValue, newValue
			// after: ...,arrayRef, arrayIndex, newValue
			mv.visitVarInsn(xStoreOpCode, shadowStackStart + 0); // shadow[0-1] = newValue
			mv.visitVarInsn(LSTORE, shadowStackStart + 2); // shadow[2-3] = shadowNewValue
			mv.visitVarInsn(ISTORE, shadowStackStart + 4); // shadow[4] = arrayIndex
			mv.visitVarInsn(LSTORE, shadowStackStart + 5); // shadow[5-6] = shadowArrayIndex
			mv.visitVarInsn(ASTORE, shadowStackStart + 7); // shadow[7] = arrayRef
			mv.visitVarInsn(LSTORE, shadowStackStart + 8); // shadow[8-9] = shadowArrayRef
			mv.visitVarInsn(ALOAD, shadowStackStart + 7); // arrayRef
			mv.visitVarInsn(ILOAD, shadowStackStart + 4); // arrayIndex
			mv.visitVarInsn(ILOAD, shadowStackStart + 0); // newValue
			mv.visitInsn(opcode);
		}

		@Override
		public void visitInsn(int opcode) {
			final int shadowStackStart = (1 + SHADOW_FIELD_SIZE) * maxLocals;
			switch (opcode) {
			case NOP:
				// just pass it along
				mv.visitInsn(opcode);
				break;
			case ACONST_NULL:
			case ICONST_M1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_5:
			case LCONST_0:
			case LCONST_1:
			case FCONST_0:
			case FCONST_1:
			case FCONST_2:
			case DCONST_0:
			case DCONST_1:
				// Constants are never tainted
				// before: ...
				// after: ..., 0L, constant
				mv.visitInsn(LCONST_0);
				mv.visitInsn(opcode);
				break;
			case IALOAD:
			case LALOAD:
			case FALOAD:
			case DALOAD:
			case AALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
				// TODO: Instead of removing the taint, I should check the shadow array, and get the taint from there
				// For now, simply remove the taint, and mark the returned value as non tainted
				// before: ...,shadowArrayRef, arrayRef, shadowIndex, arrayIndex
				// intermediate: ...,0L, arrayRef, arrayIndex
				// after: ..., 0L, arrayRef[arrayIndex]
				mv.visitVarInsn(ISTORE, shadowStackStart + 0); // shadow[0] = arrayIndex
				mv.visitVarInsn(LSTORE, shadowStackStart + 1); // shadow[1-2] = shadowArrayIndex
				mv.visitVarInsn(ASTORE, shadowStackStart + 3); // shadow[3] = arrayRef
				mv.visitVarInsn(LSTORE, shadowStackStart + 4); // shadow[4-5] = shadowArrayRef
				mv.visitInsn(LCONST_0); // Taint for the array element to be retrieved.
				mv.visitVarInsn(ALOAD, shadowStackStart + 3); // arrayRef
				mv.visitVarInsn(ILOAD, shadowStackStart); // arrayIndex
				mv.visitInsn(opcode);
				break;
			case IASTORE:
			case BASTORE:
			case CASTORE:
			case SASTORE:
				// TODO: Instead of discarding the taint, store it in the shadow array
				// for now, simply remove the taint
				// before: ...,shadowArrayRef, arrayRef, shadowIndex, arrayIndex, shadowNewValue, newValue
				// intermediate: ...,arrayRef, arrayIndex, newValue
				// after: ...
				simpleArrayStore(ISTORE, ILOAD, shadowStackStart, opcode);
				break;
			case LASTORE:
				doubleArrayStore(LSTORE, LLOAD, shadowStackStart, opcode);
				break;
			case FASTORE:
				simpleArrayStore(FSTORE, FLOAD, shadowStackStart, opcode);
				break;
			case DASTORE:
				doubleArrayStore(DSTORE, DLOAD, shadowStackStart, opcode);
				break;
			case AASTORE:
				simpleArrayStore(ASTORE, ALOAD, shadowStackStart, opcode);
				break;
			case POP:
			case POP2:
				// before: ...,shadow, value
				// after: ...
				mv.visitInsn(opcode); // it pops either 1 or 2 slots
				mv.visitInsn(POP2);
				break;
			case DUP:
				// TODO: None of the stack instructions have been implemented
				// before: ..., shadow, value
				// after: ...,shadow, value, shadow, value
			case DUP_X1:
			case DUP_X2:
			case DUP2:
			case DUP2_X1:
			case DUP2_X2:
			case SWAP:
				break;
				
			case IADD:
			case LADD:
			case FADD:
			case DADD:
			case ISUB:
			case LSUB:
			case FSUB:
			case DSUB:
			case IMUL:
			case LMUL:
			case FMUL:
			case DMUL:
			case IDIV:
			case LDIV:
			case FDIV:
			case DDIV:
			case IREM:
			case LREM:
			case FREM:
			case DREM:
			case INEG:
			case LNEG:
			case FNEG:
			case DNEG:
			case ISHL:
			case LSHL:
			case ISHR:
			case LSHR:
			case IUSHR:
			case LUSHR:
			case IAND:
			case LAND:
			case IOR:
			case LOR:
			case IXOR:
			case LXOR:
			case I2L:
			case I2F:
			case I2D:
			case L2I:
			case L2F:
			case L2D:
			case F2I:
			case F2L:
			case F2D:
			case D2I:
			case D2L:
			case D2F:
			case I2B:
			case I2C:
			case I2S:
			case LCMP:
			case FCMPL:
			case FCMPG:
			case DCMPL:
			case DCMPG:
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case RETURN:
			case ARRAYLENGTH:
			case ATHROW:
			case MONITORENTER:
			case MONITOREXIT:
				break;

			default:
				break;
			}
			super.visitInsn(opcode);
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			final int shadowIndex = maxLocals + 2 * var;
			switch (opcode) {
			//ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE,
			//*        LSTORE, FSTORE, DSTORE, ASTORE or RET
			case ALOAD:
			case LLOAD:
			case FLOAD:
			case DLOAD:
			case ILOAD:
				// before: ...
				// after: ..., sA, A
				mv.visitVarInsn(LLOAD, shadowIndex);
				mv.visitVarInsn(opcode, var);
				break;
			case ISTORE:
			case LSTORE:
			case FSTORE:
			case DSTORE:
			case ASTORE:
				// before: ..., sA, A
				// after: ...
				mv.visitVarInsn(opcode, var);
				mv.visitVarInsn(LSTORE, shadowIndex);
				break;
			case RET:
				// This is a return from subroutine, and takes the return address from a local variable.
				// Since it makes no sense to taint a return address, we ignore the value.
				break;
			default:
				logger.severe("visitVarInsn should not receive the opcode " + opcode);
				throw new RuntimeException("visitVarInsn should not receive the opcode " + opcode);
			}
		}

	}
}
