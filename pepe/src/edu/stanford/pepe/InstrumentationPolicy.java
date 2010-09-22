package edu.stanford.pepe;

import java.lang.instrument.ClassFileTransformer;

import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;

public class InstrumentationPolicy implements Opcodes {

	public static final String PEPE_PACKAGE = "edu/stanford/pepe";
	public static final String THROWABLE_TYPE = "java/lang/Throwable";
	public static final String STACK_TRACE_ELEMENT_TYPE = "java/lang/StackTraceElement";
	public static final String CLASS_TYPE = "java/lang/Class";
	
	public static boolean isFieldInstrumentable(String ownerType, String fieldName, boolean isStatic) {
		// CAUTION: At instrumentation time I cannot determine whether certain field is final or not. For that reason static final fields are also instrumented.
		return isTypeInstrumentable(ownerType) && !isSpecialJavaClass(ownerType) && !isSpecialPepeClass(ownerType);
	}

	/**
	 * Not very appropriately named, but it returns whether the given type is
	 * part of the core JVM classes and it should be instrumented differently
	 * from the others.
	 */
	public static boolean isSpecialJavaClass(String type) {
		return isPrimitiveWrapperOrString(type) || type.equals("java/lang/Thread")
				|| type.equals("java/io/ObjectStreamClass") || type.equals("java/lang/StringBuffer")
				|| type.equals("java/lang/StringBuilder") || type.equals("java/lang/AbstractStringBuilder");
	}
	
	/**
	 * As the name indicates, returns whether the given type is a primitive wrapper or a String.
	 */
	public static boolean isPrimitiveWrapperOrString(String type) {
		return type.startsWith("java/lang") && (
				type.equals("java/lang/Integer") ||
				type.equals("java/lang/String") ||
				type.equals("java/lang/Long") ||
				type.equals("java/lang/Byte") ||
				type.equals("java/lang/Short") ||
				type.equals("java/lang/Character") ||
				type.equals("java/lang/Float") ||
				type.equals("java/lang/Double")
				);
	}

	/**
	 * Also not very appropriately named, but it returns whether the given type
	 * is a special pepe class that should be modified at runtime.
	 */
	public static boolean isSpecialPepeClass(String type) {
		return type.equals("edu/stanford/pepe/TaintCheck");
	}

	/**
	 * Determines whether a given class should be instrumented or not.
	 * 
	 * @param className
	 *            name of the class, as given in
	 *            {@link ClassFileTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain, byte[])}
	 * @param loader
	 *            the defining loader of the class, null if it's the Bootstrap
	 *            classloader
	 * @return <code>true</code> if the class should be ignored,
	 *         <code>false</code> otherwise
	 */
	public static boolean isTypeInstrumentable(String type) {
		return !isPepeClass(type) && !isComplicatedSunClass(type) && !type.equals(THROWABLE_TYPE)
				&& !type.equals(STACK_TRACE_ELEMENT_TYPE) && !type.startsWith("java/lang/Thread")
				&& !type.equals("java/lang/String") && !type.equals("java/lang/System")
				&& !type.equals("java/lang/RuntimePermission") && !type.equals("java/lang/Object")
				&& !type.startsWith("java/security")
				&& !type.startsWith("java/lang/") //XXX This ignore list needs to be cleaned up
				&& !type.startsWith("java/io/ObjectStreamClass")
				&& !type.startsWith("javax/servlet/http/HttpServletResponse") // TODO: determine why this class breaks dacapo
				&& !type.equals("org/apache/xmlbeans/impl/piccolo/xml/Piccolo") // It breaks because of method length
				;
	}

	private static boolean isPepeClass(String ownerType) {
		return ownerType.startsWith(PEPE_PACKAGE);
	}

	private static boolean isComplicatedSunClass(final String classType) {
		return (classType.charAt(0) == 's' && (classType.startsWith("sun/util/resources/TimeZoneName") || classType.startsWith("sun/misc/Unsafe")
				|| classType.startsWith("sun/misc/AtomicLong") || classType.startsWith("sun/reflect/") || classType
				.startsWith("sun/instrument/")))
				|| (classType.charAt(0) == 'j' && (classType.equals(CLASS_TYPE)
						|| classType.startsWith(CLASS_TYPE + "$") || classType.equals("java/lang/ClassLoader")
						|| classType.startsWith("java/lang/ClassLoader$") || classType.startsWith("java/lang/reflect/")
						|| classType.startsWith("java/lang/ref/")
						|| classType.startsWith("java/util/concurrent/atomic/")
						|| classType.startsWith("java/util/concurrent/locks/") || (classType
						.startsWith("java/security/") && classType.endsWith("Permission"))))
				|| (classType.charAt(0) == 'c' && (classType.equals("com/sun/demo/jvmti/hprof/Tracker")));
	}

	/**
	 * Makes more stringent checks on whether a Class is instrumentable. In
	 * particular, it checks whether it is an interface, annotation or enum.
	 * 
	 * @param cn
	 *            the class to be instrumented
	 */
	public static boolean isTypeInstrumentable(ClassNode cn) {
		int classAccess = cn.access;
		boolean isInterface = (classAccess & ACC_INTERFACE) != 0;
		boolean isEnum = (classAccess & ACC_ENUM) != 0;
		boolean isAnnotation = (classAccess & ACC_ANNOTATION) != 0;

		if (isInterface || isAnnotation || isEnum) {
			return false;
		}
		return isTypeInstrumentable(cn.name);
	}

	static boolean isPrimitive(String type) {
		return type.equals("java/lang/Integer") ||
				type.equals("java/lang/String") ||
				type.equals("java/lang/Long") ||
				type.equals("java/lang/Byte") ||
				type.equals("java/lang/Short") ||
				type.equals("java/lang/Character") ||
				type.equals("java/lang/Float") ||
				type.equals("java/lang/Double")
				;
	}

}
