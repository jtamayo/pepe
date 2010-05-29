package edu.stanford.pepe;

import java.lang.instrument.ClassFileTransformer;

public class InstrumentationPolicy {
	
	public static final String PEPE_PACKAGE = "edu/stanford/pepe";
    public static final String THROWABLE_TYPE = "java/lang/Throwable";
    public static final String STACK_TRACE_ELEMENT_TYPE = "java/lang/StackTraceElement";
    public static final String CLASS_TYPE = "java/lang/Class";
	
    public static boolean isFieldInstrumentable(String ownerType, String fieldName, boolean isStatic) {
    	return !(isStatic &&
        ownerType.equals("java/security/AccessControlContext") &&
        fieldName.startsWith("debug"));
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
        return 
//        !(type.compareTo("java/lang/I") >= 0 &&
//          type.compareTo("java/lang/N") < 0) && 
        !isPepeClass(type) &&
        !isComplicatedSunClass(type) &&
        !type.equals(THROWABLE_TYPE) &&
        !type.equals(STACK_TRACE_ELEMENT_TYPE) &&
        !type.startsWith("java/lang/Thread") &&
        !type.equals("java/lang/String") &&
        !type.equals("java/lang/System") 
        && !type.equals("java/lang/RuntimePermission")
        && !type.equals("java/lang/Object")
        && !type.startsWith("java/security")
        && !type.startsWith("java/lang/") //XXX This ignore list needs to be cleaned up
        ;        
//		String[] ignore = {"org/apache/geronimo/connector/outbound/connectionmanagerconfig/XATransactions", 
//				"org/apache/geronimo/security/jaas/LoginModuleControlFlag",
//				"org/apache/geronimo/connector/outbound/connectionmanagerconfig/SinglePool",
//				"org/apache/geronimo/connector/outbound/connectionmanagerconfig/NoTransactions",
//				"org/apache/xbean/naming/reference/SimpleReference",
//				"org/apache/geronimo/axis/client/AxisServiceReference",
//				"org/apache/geronimo/axis/client/SEIFactoryImpl",
//				"org/apache/geronimo/axis/client/OperationInfo",
//				"org/apache/geronimo/security/realm/providers/GeronimoGroupPrincipal",
//				"org/apache/geronimo/security/jacc/ComponentPermissions",
//				"org/apache/axis/description/OperationDesc",
//				"org/apache/axis/description/ParameterDesc",
//				"org/apache/geronimo/j2ee/annotation/Holder",
//				"org/apache/axis/constants", // I don't care about any class in this package
//				"org/apache/axis/soap/SOAP11Constants",
//				"org/apache/geronimo/axis/client", // TODO: Check if all the classes in this package can be safely ignored
//				"org/apache/axis/encoding/ser" // TODO: can the entire package be ignored?
//				, "org/apache/axis" // TODO: Arrrgh!! it's so annoying
//				,"org/apache/geronimo/naming/reference/SimpleAwareReference"
//				,"org/apache/geronimo/naming"
//				,"org/apache/openejb/assembler/classic"
//				,"org/apache/geronimo"
//				,"org/apache/openejb"
//				};
//		for (String s : ignore) {
//			if (ownerType.startsWith(s))  return true;
//		}
//		
//		// All classes are instrumented by default
//		return false;
	}

    private static boolean isPepeClass(String ownerType) {
    	return ownerType.startsWith(PEPE_PACKAGE);
	}

	private static boolean isComplicatedSunClass(final String classType) {
        return (classType.charAt(0) == 's' &&
                (classType.startsWith("sun/misc/Unsafe") ||
                 classType.startsWith("sun/misc/AtomicLong") ||
                 classType.startsWith("sun/reflect/") ||
                 classType.startsWith("sun/instrument/"))) ||
               (classType.charAt(0) == 'j' &&
                (classType.equals(CLASS_TYPE) ||
                 classType.startsWith(CLASS_TYPE + "$") ||
                 classType.equals("java/lang/ClassLoader") ||
                 classType.startsWith("java/lang/ClassLoader$") ||
                 classType.startsWith("java/lang/reflect/") ||
                 classType.startsWith("java/lang/ref/") ||
                 classType.startsWith("java/util/concurrent/atomic/") ||
                 classType.startsWith("java/util/concurrent/locks/") ||
                 (classType.startsWith("java/security/") &&
                  classType.endsWith("Permission")))) ||
               (classType.charAt(0) == 'c' &&
                (classType.equals("com/sun/demo/jvmti/hprof/Tracker")));
    }
    
    
}
