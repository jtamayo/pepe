package edu.stanford.pepe;

import static edu.stanford.pepe.TaintProperties.SHADOW_FIELD_SIZE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.aj.prep.FrameAnalyzer;
import edu.stanford.pepe.modifiedasm.EnhancedClassNode;
import edu.stanford.pepe.org.objectweb.asm.AnnotationVisitor;
import edu.stanford.pepe.org.objectweb.asm.Attribute;
import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.Type;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.MethodNode;
import edu.stanford.pepe.org.objectweb.asm.tree.TryCatchBlockNode;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Frame;

public class ShadowStackRewriter implements Opcodes {
	public static Logger logger = Logger.getLogger(ShadowStackRewriter.class.getName());
	{
		logger.setLevel(Level.INFO);
	}

	@SuppressWarnings("unchecked")
	public static void rewrite(EnhancedClassNode cn, ClassVisitor output) {
		// First output the entire class, except the methods and the visitEnd
		copyAllButMethods(cn, output);

		// Now instrument each method
		List<MethodNode> methods = cn.methods;

		for (MethodNode mn : methods) {
			Frame[] frames = FrameAnalyzer.getInstance().compute(cn.name, mn);
			MethodVisitor outputMethodVisitor = output.visitMethod(mn.access, mn.name, mn.desc, mn.signature,
					(String[]) mn.exceptions.toArray(new String[]{})); // This line would be called by the copyAllButMethods, but we're skipping it
			ShadowStackVisitor v = new ShadowStackVisitor(outputMethodVisitor, mn, frames, cn);
			mn.accept(v);
		}
		
		if (cn.name.equals("java/lang/Thread")) {
			emitGetReturnValue(cn, output);
		}
		
		// Every method has been visited, all that remains is to call visitEnd on the output
		output.visitEnd();
	}

	private static void emitGetReturnValue(EnhancedClassNode cn, ClassVisitor output) {
		MethodVisitor mv = output.visitMethod(ACC_PUBLIC + ACC_STATIC, ThreadReturnValuesRewriter.GET_RETURN_VALUE, "()J", null, null);
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
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Thread"}, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "java/lang/Thread", ThreadReturnValuesRewriter.RETURN_VALUE_NAME, "J");
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.LONG});
		mv.visitInsn(LRETURN);
		Label l4 = new Label();
		mv.visitLabel(l4);
//		mv.visitLocalVariable("t", "Ljava/lang/Thread;", null, l1, l4, 0);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
		
}

	/** Transfer all events except visitMethod and visitEnd. */
	private static void copyAllButMethods(final ClassNode root, final ClassVisitor output) {
		root.accept(new ClassAdapter(output) {
			@Override
			public MethodVisitor visitMethod(final int access, final String name, final String desc,
					final String signature, final String[] exceptions) {
				return null;
			}

			@Override
			public void visitEnd() {
			}
		});
	}
	
	static class ShadowStackVisitor implements MethodVisitor, Opcodes {
		
		public static final int WORKING_STACK_SIZE = 8; // DUP2_X2 uses 4 shadow vars

		private final MethodNode mn;
		private final MethodVisitor output;
		private final Frame[] frames;
		private final int newMaxStack;
		private final int newMaxLocals;
		private final int shadowStackStart;
		
		private final Map<Label, Label> newLabels = new HashMap<Label, Label>();

		private int inst = -1; // Start one before the instructions, because it's incremented before any visitXX method is executed

		// TODO: The EnhancedClassNode is no longer required, because I don't need field info by name
		private final EnhancedClassNode cn;

		public ShadowStackVisitor(MethodVisitor output, MethodNode mn, Frame[] frames, EnhancedClassNode cn) {
			this.output = output;
			this.mn = mn;
			this.frames = frames;
			this.cn = cn;
			// new locals will be: the old locals + shadow for old locals + shadow copy of the stack
			this.newMaxLocals = mn.maxLocals + SHADOW_FIELD_SIZE*mn.maxLocals + SHADOW_FIELD_SIZE*mn.maxStack;
			// The shadow stack will start right after the shadows for the locals
			this.shadowStackStart = mn.maxLocals + SHADOW_FIELD_SIZE*mn.maxLocals;
			// The stack is the old stack, plus our working variables
			this.newMaxStack = mn.maxStack + WORKING_STACK_SIZE;
		}
		
		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			output.visitMaxs(newMaxStack, newMaxLocals);
		}



		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return output.visitAnnotation(desc, visible);
		}



		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return output.visitAnnotationDefault();
		}



		@Override
		public void visitAttribute(Attribute attr) {
			output.visitAttribute(attr);
		}



		@Override
		public void visitCode() {
			output.visitCode();
			clearLocals(); // TODO: Load the values from the Thread instead of constant 0
		}



		private void clearLocals() {
			for (int i = 0; i < mn.maxLocals; i++) {
				clear(mn.maxLocals + SHADOW_FIELD_SIZE*i);
			}
			
		}

		private void clearShadowStack() {
			for (int i = 0; i < mn.maxStack; i++) {
				clear(shadowStackStart + SHADOW_FIELD_SIZE*i);
			}
		}

		@Override
		public void visitEnd() {
	        if (inst != (mn.instructions.size() - 1)) {
	            throw new RuntimeException(
	                    "index=" + inst + ", instr count=" + mn.instructions.size());
	        }
			output.visitEnd();
		}



		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			inst++;
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			switch (opcode) {
			case GETSTATIC:
				// Load the original field before the taint, in case the original field is volatile we preserve happens-before
				output.visitFieldInsn(opcode, owner, name, desc);
				if (InstrumentationPolicy.isTypeInstrumentable(owner) && InstrumentationPolicy.isFieldInstrumentable(owner, name, true)) {
					String shadowName = ShadowFieldRewriter.getShadowFieldName(name);
					output.visitFieldInsn(GETSTATIC, owner, shadowName, ShadowFieldRewriter.TAINT_TYPE.getDescriptor()); // Loaded shadow
					output.visitVarInsn(LSTORE, shadowStackIndex);
				} else {
					// If the field is not instrumentable, we assume it is not tainted
					clear(shadowStackIndex);
				}
				break;
			case PUTSTATIC:
				// Store the taint before the original field, in case the original field is volatile 
				if (InstrumentationPolicy.isTypeInstrumentable(owner) && InstrumentationPolicy.isFieldInstrumentable(owner, name, true)) {
					String shadowName = ShadowFieldRewriter.getShadowFieldName(name);
					output.visitVarInsn(LLOAD, shadowStackIndex - 1*SHADOW_FIELD_SIZE); // The taint is in the head of the stack
					output.visitFieldInsn(PUTSTATIC, owner, shadowName, ShadowFieldRewriter.TAINT_TYPE.getDescriptor());
				} 
				output.visitFieldInsn(opcode, owner, name, desc);
				break;
			case GETFIELD:
				if (InstrumentationPolicy.isTypeInstrumentable(owner) && InstrumentationPolicy.isFieldInstrumentable(owner, name, false)) {
					String shadowName = ShadowFieldRewriter.getShadowFieldName(name);
					// TODO: Load the taint after loading the field
					output.visitInsn(DUP); // Copy the object reference of the field
					output.visitFieldInsn(GETFIELD, owner, shadowName, ShadowFieldRewriter.TAINT_TYPE.getDescriptor()); // Loaded shadow
					output.visitVarInsn(LSTORE, shadowStackIndex - 1*SHADOW_FIELD_SIZE); // Return value goes on top of the stack
					// TODO: Meet the taint of the field and the taint of the reference
					output.visitFieldInsn(opcode, owner, name, desc);
				} else {
					clear(shadowStackIndex - 1*SHADOW_FIELD_SIZE);
					output.visitFieldInsn(opcode, owner, name, desc);
				}
				break;
			case PUTFIELD:
				if (InstrumentationPolicy.isTypeInstrumentable(owner) && InstrumentationPolicy.isFieldInstrumentable(owner, name, false)) {
					String shadowName = ShadowFieldRewriter.getShadowFieldName(name);
					// ...,objref, value -> ...
					// I need to determine whether value is long or int, to properly fix it
					if (frames[inst].getStack(stackSize - 1).getSize() == 1) {
						// Top of the stack contains a narrow value
						// ...,ref,V1
						output.visitInsn(DUP_X1); // ...,V1,ref,V1
						output.visitInsn(POP); // ...,V1,ref
						output.visitInsn(DUP_X1); // ...,ref,V1,ref						
						output.visitVarInsn(LLOAD, shadowStackIndex - 1*SHADOW_FIELD_SIZE); // ...,ref,V1,ref,taint
						output.visitFieldInsn(PUTFIELD, owner, shadowName, ShadowFieldRewriter.TAINT_TYPE.getDescriptor()); // ...,ref,V1
						output.visitFieldInsn(opcode, owner, name, desc); // store original field
					} else {
						// Top of the stack contains a long/double
						// ...,ref,W1
						output.visitInsn(DUP2_X1); // ...,W1,ref,W1
						output.visitInsn(POP2); // ...,W1,ref
						output.visitInsn(DUP_X2); // ...,ref,W1,ref
						output.visitVarInsn(LLOAD, shadowStackIndex - 1*SHADOW_FIELD_SIZE); // ...,ref,V1,ref,taint
						output.visitFieldInsn(PUTFIELD, owner, shadowName, ShadowFieldRewriter.TAINT_TYPE.getDescriptor()); // ...,ref,V1
						output.visitFieldInsn(opcode, owner, name, desc); // store original field
					}
				} else {
					output.visitFieldInsn(opcode, owner, name, desc);
				}
				break;
			default:
				throw new IllegalArgumentException("Method visitFieldInsn is not supposed to receive opcode " + opcode);

			}
		}



		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			inst++;
			output.visitFrame(type, nLocal, local, nStack, stack);
		}



		@Override
		public void visitIincInsn(int var, int increment) {
			inst++;
			// The local variable remains tainted like it was before
			output.visitIincInsn(var, increment);
		}



		@Override
		public void visitInsn(int opcode) {
			inst++;
			// The stack depth before instruction inst. Long and doubles count as only one value.
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			
			switch (opcode) {
			case NOP:
				// just pass it along
				output.visitInsn(opcode);
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
				output.visitInsn(LCONST_0);
				output.visitVarInsn(LSTORE, shadowStackIndex);
				output.visitInsn(opcode);
				break;
			case IALOAD:
			case LALOAD:
			case FALOAD:
			case DALOAD:
			case AALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
			case IASTORE:
			case BASTORE:
			case CASTORE:
			case SASTORE:
			case LASTORE:
			case FASTORE:
			case DASTORE:
			case AASTORE:
				// TODO: implement the array shadows
				output.visitInsn(opcode);
				break;
			case POP:
			case POP2:
				// The variable is discarded, as well as its taint
				output.visitInsn(opcode);
				break;
			case DUP:
				// Copy the taint
				output.visitVarInsn(LLOAD, shadowStackIndex - 1*SHADOW_FIELD_SIZE);
				output.visitVarInsn(LSTORE, shadowStackIndex);
				output.visitInsn(opcode);
				break;
			case DUP_X1:
				// ...,V2,V1 -> ...,V1,V2,V1
				//    ,-2,-1       ,-2,-1, 0
				output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
				output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
				output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
				output.visitInsn(DUP2); // V1 V1
				output.visitVarInsn(LSTORE, shadowStackIndex - 2*SHADOW_FIELD_SIZE); // V1
				output.visitVarInsn(LSTORE, shadowStackIndex); // V1
				output.visitInsn(opcode);
				break;
			case DUP_X2:
				if (frames[inst].getStack(stackSize - 1).getSize() == 1) {
					// Form 1 in the JVM spec 2nd ed: all values are narrow
					// ...,V3,V2,V1  ...,V1,V3,V2,V1
					//    ,-3,-2,-1      -3,-2,-1, 0
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
					output.visitVarInsn(LLOAD, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V3
					output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V3
					output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V2
					output.visitInsn(DUP2); // V1, V1
					output.visitVarInsn(LSTORE, shadowStackIndex - 3*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LSTORE, shadowStackIndex); // V1
				} else {
					// Form 2 in the JVM spec 2nd. Top is narrow, next one is long/double
					// ...,W2,V1 -> ...,V1,W2,V1
					// ...,-2,-1 -> ...,-2,-1, 0
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W2
					output.visitVarInsn(LSTORE, shadowStackIndex - 1*SHADOW_FIELD_SIZE); // W2
					output.visitInsn(DUP2); // V1
					output.visitVarInsn(LSTORE, shadowStackIndex - 2*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LSTORE, shadowStackIndex); // V1
				}
				output.visitInsn(opcode);
				break;
			case DUP2:
				if (frames[inst].getStack(stackSize - 1).getSize() == 1) {
					// The top of the stack has two values, and they're both being copied
					// ...,V2,V1 -> ...,V2,V1,V2,V1
					// ...,-2,-1 -> ...,-2,-1, 0, 1
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LSTORE, shadowStackIndex + 1*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
					output.visitVarInsn(LSTORE, shadowStackIndex); // V2
				} else {
					// The top of the stack contains a single long/double value
					// ...,W1 -> ...,W1,W1
					// ...,-1 -> ...,-1, 0
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W1
					output.visitVarInsn(LSTORE, shadowStackIndex); // W1
				}
				output.visitInsn(opcode);
				break;
			case DUP2_X1:
				if (frames[inst].getStack(stackSize -1).getSize() == 1) {
					// The top of the stack has two values
					// ...,V3,V2,V1 -> ...,V2,V1,V3,V2,V1
					// ...,-3,-2,-1 -> ...,-3,-2,-1, 0, 1
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
					output.visitVarInsn(LLOAD, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V3
					output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V3
					output.visitInsn(DUP2); // V2
					output.visitVarInsn(LSTORE, shadowStackIndex); // V2
					output.visitVarInsn(LSTORE, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V2
					output.visitInsn(DUP2); // V1
					output.visitVarInsn(LSTORE, shadowStackIndex+1*SHADOW_FIELD_SIZE); // V1
					output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V1
					output.visitInsn(opcode);
				} else {
					// The top of the stack has a single long/double value
					// ...,V2,W1 -> ...,W1,V2,W1
					// ...,-2,-1 -> ...,-2,-1, 0
					output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W1
					output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
					output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V2
					output.visitInsn(DUP2); // W1
					output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // W1
					output.visitVarInsn(LSTORE, shadowStackIndex); // W1
					output.visitInsn(opcode);
				}
				break;
			case DUP2_X2:
				if (frames[inst].getStack(stackSize-1).getSize() == 1) {
					// Top two elements are narrow. Form 1 or Form 3 in the JVM specification 2nd edition 
					if (frames[inst].getStack(stackSize-3).getSize() == 1) {
						// All elements are narrow. Form 1 in the JVM spec 2nd edition
						// ...,V4,V3,V2,V1 -> V2,V1,V4,V3,V2,V1
						// ...,-4,-3,-2,-1 -> -4,-3,-2,-1, 0, 1
						output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
						output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
						output.visitVarInsn(LLOAD, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V3
						output.visitVarInsn(LLOAD, shadowStackIndex-4*SHADOW_FIELD_SIZE); // V4
						output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V4
						output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V3
						output.visitInsn(DUP2); // V2
						output.visitVarInsn(LSTORE, shadowStackIndex); // V2
						output.visitVarInsn(LSTORE, shadowStackIndex-4*SHADOW_FIELD_SIZE); // V2
						output.visitInsn(DUP2); // V1
						output.visitVarInsn(LSTORE, shadowStackIndex+1*SHADOW_FIELD_SIZE); // V1
						output.visitVarInsn(LSTORE, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V1
					} else {
						// Top two elements are narrow, third element is long/double
						// ...,W3,V2,V1 -> V2,V1,W3,V2,V1
						// ...,-3,-2,-1 -> -3,-2,-1, 0, 1
						output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
						output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
						output.visitVarInsn(LLOAD, shadowStackIndex-3*SHADOW_FIELD_SIZE); // W3
						output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W3
						output.visitInsn(DUP2); // V2
						output.visitVarInsn(LSTORE, shadowStackIndex); // V2
						output.visitVarInsn(LSTORE, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V2
						output.visitInsn(DUP2); // V1
						output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V1
						output.visitVarInsn(LSTORE, shadowStackIndex+1*SHADOW_FIELD_SIZE); // V1
					}
				} else {
					// Top element is long or double. Form 2 or Form 4 in the JVM specification 2nd edition
					if (frames[inst].getStack(stackSize-2).getSize() == 1) {
						// Top element is long/double, next two elements are narrow
						// ...,V3,V2,W1 -> ...,W1,V3,V2,W1
						// ...,-3,-2,-1 -> ...,-3,-2,-1, 0
						output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W1
						output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
						output.visitVarInsn(LLOAD, shadowStackIndex-3*SHADOW_FIELD_SIZE); // V3
						output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V3
						output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V2
						output.visitInsn(DUP2); // W1
						output.visitVarInsn(LSTORE, shadowStackIndex); // W1
						output.visitVarInsn(LSTORE, shadowStackIndex-3*SHADOW_FIELD_SIZE); // W1
					} else {
						// Top two elements are long/double
						// ...,W2,W1 -> ...,W1,W2,W1
						// ...,-2,-1 -> ...,-2,-1, 0
						output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W1
						output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // W2
						output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // W2
						output.visitInsn(DUP2); // W1
						output.visitVarInsn(LSTORE, shadowStackIndex); // W1
						output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // W1
					}
				}
				output.visitInsn(opcode);
				break;
			case SWAP:
				// ...,V2,V1 -> ...,V1,V2
				output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
				output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
				output.visitVarInsn(LSTORE, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V2
				output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V1
				output.visitInsn(opcode);
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
			case LCMP:
			case FCMPL:
			case FCMPG:
			case DCMPL:
			case DCMPG:
				// Binary operations
				// I must meet both their taints, and store the result
				// TODO: What to do with ArithmeticException in div operations?
				// TODO: How to handle comparison operators whti NAN values?
				// ...,V2,V1 -> ...,V2+V1
				// ...,-2,-1 -> ..., -2
				output.visitVarInsn(LLOAD, shadowStackIndex-1*SHADOW_FIELD_SIZE); // V1
				output.visitVarInsn(LLOAD, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V2
				emitMeetOperator();
				output.visitVarInsn(LSTORE, shadowStackIndex-2*SHADOW_FIELD_SIZE); // V1+V2
				output.visitInsn(opcode);
				break;
			case INEG:
			case LNEG:
			case FNEG:
			case DNEG:
				// Unary operations
				// Do nothing, the taint is the same
				// ...,V1 -> ...,V1
				output.visitInsn(opcode);
				break;
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
				// Cast operations. No change is necessary, because they're essentially the
				// same as a unary operation, even if the type in the stack changes.
				output.visitInsn(opcode);
				break;
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case RETURN:
				// TODO: Store the taint in Thread.returnVal
				output.visitInsn(opcode);
				break;
			case ARRAYLENGTH:
				// TODO: If the reference to the array is tainted, should the length of the array be also tainted?
				output.visitInsn(opcode);
				break;
			case ATHROW:
				// There is no way of tainting a thrown exception.
				// When the exception is thrown, if there is a handler for the exception in the current method
				// the current method stack is cleared, and the exception reference is loaded onto the stack.
				// If there is no handler for the exception in the current method, the entire frame is discarded,
				// and the process is repeated until an exception handler is found or the thread exits.
				output.visitInsn(opcode);
				break;
			case MONITORENTER:
			case MONITOREXIT:
				// Don't really care
				output.visitInsn(opcode);
				break;
			default:
				throw new RuntimeException("visitInst should not receive opcode " + opcode);
			}
		}



		// Will load 0l at the indicated index
		private void clear(int i) {
			output.visitInsn(LCONST_0);
			output.visitVarInsn(LSTORE, i);
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
			inst++;
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			switch (opcode) {
			case BIPUSH:
			case SIPUSH:
				// Equivalent to a constant load.
				// ... -> ...,C
				clear(shadowStackIndex);
				output.visitIntInsn(opcode, operand);
				break;
			case NEWARRAY:
				// TODO: What should happen if the array length were to be tainted?
				// For now, treat it as a constant load
				// .., count -> ..., arrayref
				// ..., -1   -> ..., -1
				clear(shadowStackIndex -1*SHADOW_FIELD_SIZE);
				output.visitIntInsn(opcode, operand);
				break;
			default:
				throw new IllegalArgumentException("Method visitIntInsn does not expect opcode " + opcode);
			}
		}



		@Override
		public void visitJumpInsn(int opcode, Label label) {
			inst++;
			// Right now we're not tracking control flow dependencies, and thus we don't care about jump instructions
			output.visitJumpInsn(opcode, getNewLabel(label));
		}



		@SuppressWarnings("unchecked")
		@Override
		public void visitLabel(Label label) {
			inst++;
			// I need to check whether this is the start of a catch block, so I can clear the stack
			List tryCatchBlocks = mn.tryCatchBlocks;
			boolean isCatchBlock = false;
			for (TryCatchBlockNode tc : (List<TryCatchBlockNode>) tryCatchBlocks) {
				if (tc.handler == label.info) {
					isCatchBlock = true;
					break;
				}
			}
			output.visitLabel(getNewLabel(label));
			if (isCatchBlock) {
				// The stack will be cleared before we reach here, and a ref to the thrown exception will be loaded
				// to the first position. I must clear it after the label
				// ... -> exceptionRef
				// ... -> stackStart
				clear(shadowStackStart);
			}
		}
		
		private Label getNewLabel(Label oldLabel) {
			if (newLabels.containsKey(oldLabel)) {
				return newLabels.get(oldLabel);
			} else {
				Label newLabel = new Label();
				newLabels.put(oldLabel, newLabel);
				return newLabel;
			}
		}



		@Override
		public void visitLdcInsn(Object cst) {
			inst++;
			// Constants are untainted
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			clear(shadowStackIndex);
			output.visitLdcInsn(cst);
		}



		@Override
		public void visitLineNumber(int line, Label start) {
			inst++;
			output.visitLineNumber(line, getNewLabel(start));
		}



		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			output.visitLocalVariable(name, desc, signature, getNewLabel(start), getNewLabel(end), index);
		}



		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			inst++;
			// Because we're not tracking control flow, we don't care about switch instructions
			output.visitLookupSwitchInsn(getNewLabel(dflt), keys, getNewLabels(labels));
		}



		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			inst++;
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			final int argSize = Type.getArgumentTypes(desc).length;
			
			switch (opcode) {
			case INVOKESTATIC:
				output.visitMethodInsn(opcode, owner, name, desc);
				if (!Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
					// ...,arg1,arg2,...,arg_(argSize-1) -> ...,retvalue
					loadReturnValueTaint();
//					output.visitInsn(LCONST_0);
					output.visitVarInsn(LSTORE, shadowStackIndex - SHADOW_FIELD_SIZE*argSize);
				}
				break;
			case INVOKEVIRTUAL:
			case INVOKESPECIAL:
			case INVOKEINTERFACE:
				// This is a problem, I need to know how many values will be popped out of the stack after the method invocation
				// I also need to taint it AFTER invoking the method, not before.
				output.visitMethodInsn(opcode, owner, name, desc);
				if (!Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
					// The method will return a value
					// TODO: Get the value from Thread instead of a constant
					loadReturnValueTaint();
//					output.visitInsn(LCONST_0);
					output.visitVarInsn(LSTORE, shadowStackIndex - SHADOW_FIELD_SIZE*(argSize+1)); // Plus one for the "this" parameter
				}
				break;
			case INVOKEDYNAMIC:
				throw new UnsupportedOperationException("Method visitMethodInsn does not yet support INVOKEDYNAMIC");
			default:
				throw new IllegalArgumentException("Method visitMethodInsn should not receive opcode " + opcode);

			}
		}



		private void loadReturnValueTaint() {
			output.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", ThreadReturnValuesRewriter.GET_RETURN_VALUE, "()J");
			//			output.visitFieldInsn(GETFIELD, "java/lang/Thread", ThreadReturnValuesRewriter.RETURN_VALUE_NAME, ShadowFieldRewriter.TAINT_TYPE.getDescriptor());
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			inst++;
			// TODO: At this point I need to create the shadow array
			// Say dims = 3
			// ..., count1, count2, count3 -> ..., arrayref
			// ..., -3    , -2    , -1     -> ..., -3
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			clear(shadowStackIndex -dims*SHADOW_FIELD_SIZE);
			output.visitMultiANewArrayInsn(desc, dims);
		}



		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			return output.visitParameterAnnotation(parameter, desc, visible);
		}



		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
			inst++;
			// Because we're not tracking control flow, we don't care about switch instructions
			output.visitTableSwitchInsn(min, max, getNewLabel(dflt), getNewLabels(labels));
		}
		
		private Label[] getNewLabels(Label[] oldLabels) {
			Label[] newLabels = new Label[oldLabels.length];
			for (int i = 0; i < oldLabels.length; i++) {
				Label oldLabel = oldLabels[i];
				newLabels[i] = getNewLabel(oldLabel);
			}
			return newLabels;
		}



		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			output.visitTryCatchBlock(getNewLabel(start), getNewLabel(end), getNewLabel(handler), type);
		}



		@Override
		public void visitTypeInsn(int opcode, String type) {
			inst++;
			// The stack depth before instruction inst. Long and doubles count as only one value.
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			switch (opcode) {
			case NEW:
				// Just object creation cannot taint an object (maybe the constructor can). Thus, treat it as a load constant.
				// ... -> ...,ref
				clear(shadowStackIndex);
				output.visitTypeInsn(opcode, type);
				break;
			case ANEWARRAY:
				// TODO: Create the shadow array at this point.
				// For now, treat it as a constant load.
				// ..., count -> ..., arrayref
				clear(shadowStackIndex - 1*SHADOW_FIELD_SIZE);
				output.visitTypeInsn(opcode, type);
				break;
			case CHECKCAST:
				// It leaves the stack unchanged if the cast is allowed, and throws an exception if it's not
				// Thus, the taint remains unchanged
				output.visitTypeInsn(opcode, type);
				break;
			case INSTANCEOF:
				// ..., objectref -> ..., result
				// ..., -1        -> ..., -1
				// We assume the result of this instruction is untainted
				clear(shadowStackIndex - 1*SHADOW_FIELD_SIZE);
				output.visitTypeInsn(opcode, type);
				break;
			default:
				throw new IllegalArgumentException("Method visitVarInsn cannot receive opcode " + opcode);
			}
		}



		@Override
		public void visitVarInsn(int opcode, int var) {
			inst++;
			// Unlike the stack, where we keep one shadow per value, whether the value
			// is a long/double or a int/float/ref, here we just keep an exact 2 to 1 map
			// between locals and shadowLocals. If locals[var] contains a double, we'll
			// just have 2 shadows, even though we only really need one.
			int shadowVar = mn.maxLocals + SHADOW_FIELD_SIZE*var;
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			switch (opcode) {
			case ILOAD:
			case LLOAD:
			case FLOAD:
			case DLOAD:
			case ALOAD:
				// ... -> ...,V1
				// ... -> ..., 0
				output.visitVarInsn(LLOAD, shadowVar);
				output.visitVarInsn(LSTORE, shadowStackIndex);
				output.visitVarInsn(opcode, var);
				break;
			case ISTORE:
			case LSTORE:
			case FSTORE:
			case DSTORE:
			case ASTORE:
				// ...,V1 -> ...
				// ...,-1 -> ...
				output.visitVarInsn(LLOAD, shadowStackIndex - 1*SHADOW_FIELD_SIZE);
				output.visitVarInsn(LSTORE, shadowVar);
				output.visitVarInsn(opcode, var);
				break;
			case RET:
				// Simply changes the PC, we don't care
				output.visitVarInsn(opcode, var);
				break;
			default:
				throw new IllegalArgumentException("Method visitVarInsn cannot receive opcode " + opcode);

			}
		}

		/**
		 * Emits bytecode equivalent to the invocation of a static method call.
		 * The descriptor of the "method call" is (JJ)J.
		 */
		private void emitMeetOperator() {
			// XXX: For now, simply discard one of the two values
			output.visitInsn(POP2); // XXX: Remove when the merge operator actually works
		}
		
	}
}
