package edu.stanford.pepe;

import java.lang.instrument.ClassFileTransformer;

public class IgnoreList {
	
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
	public static boolean ignoreClass(String className, ClassLoader loader) {
		if (className.startsWith("java"))
			return true;
		if (className.startsWith("org/objectweb/asm"))
			return true;
		if (className.startsWith("edu/stanford/pepe")) {
			return true;
		}
		if (className.startsWith("sun/security")) {
			return true;
		}
		if (className.startsWith("sun/reflect")) {
			return true;
		}
		if (loader == null) {
			// Also ignore classes from the bootstrap classloader
			return true;
		}
		
		// TODO: Separate ignore lists common to all application from ignore list particular to each application
		// There classes are serialized, but do not declare SerialVersionUid, so they fail when loaded.
		String[] ignore = {"org/apache/geronimo/connector/outbound/connectionmanagerconfig/XATransactions", 
				"org/apache/geronimo/security/jaas/LoginModuleControlFlag",
				"org/apache/geronimo/connector/outbound/connectionmanagerconfig/SinglePool",
				"org/apache/geronimo/connector/outbound/connectionmanagerconfig/NoTransactions",
				"org/apache/xbean/naming/reference/SimpleReference",
				"org/apache/geronimo/axis/client/AxisServiceReference",
				"org/apache/geronimo/axis/client/SEIFactoryImpl",
				"org/apache/geronimo/axis/client/OperationInfo",
				"org/apache/geronimo/security/realm/providers/GeronimoGroupPrincipal",
				"org/apache/geronimo/security/jacc/ComponentPermissions",
				"org/apache/axis/description/OperationDesc",
				"org/apache/axis/description/ParameterDesc",
				"org/apache/geronimo/j2ee/annotation/Holder",
				"org/apache/axis/constants", // I don't care about any class in this package
				"org/apache/axis/soap/SOAP11Constants",
				"org/apache/geronimo/axis/client", // TODO: Check if all the classes in this package can be safely ignored
				"org/apache/axis/encoding/ser" // TODO: can the entire package be ignored?
				, "org/apache/axis" // TODO: Arrrgh!! it's so annoying
				,"org/apache/geronimo/naming/reference/SimpleAwareReference"
				,"org/apache/geronimo/naming"
				,"org/apache/openejb/assembler/classic"
				,"org/apache/geronimo"
				,"org/apache/openejb"
				};
		for (String s : ignore) {
			if (className.startsWith(s))  return true;
		}
		
		// All classes are instrumented by default
		return false;
	}

}
