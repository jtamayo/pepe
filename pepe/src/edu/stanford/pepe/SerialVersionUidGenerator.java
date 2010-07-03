package edu.stanford.pepe;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class for generating serialVersionUid without taking into account shadow
 * fields. The code was shamelessly stolen from {@link ObjectStreamClass}.
 * 
 * @author jtamayo
 */
public class SerialVersionUidGenerator {

	/**
	 * Class for computing and caching field/constructor/method signatures
	 * during serialVersionUID calculation.
	 */
	private static class MemberSignature {

		public final Member member;
		public final String name;
		public final String signature;

		public MemberSignature(Field field) {
			member = field;
			name = field.getName();
			signature = getClassSignature(field.getType());
		}

		public MemberSignature(Constructor cons) {
			member = cons;
			name = cons.getName();
			signature = getMethodSignature(cons.getParameterTypes(), Void.TYPE);
		}

		public MemberSignature(Method meth) {
			member = meth;
			name = meth.getName();
			signature = getMethodSignature(meth.getParameterTypes(), meth.getReturnType());
		}
	}

	/**
	 * Returns JVM type signature for given list of parameters and return type.
	 */
	private static String getMethodSignature(Class[] paramTypes, Class retType) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append('(');
		for (int i = 0; i < paramTypes.length; i++) {
			sbuf.append(getClassSignature(paramTypes[i]));
		}
		sbuf.append(')');
		sbuf.append(getClassSignature(retType));
		return sbuf.toString();
	}

	/**
	 * Computes the default serial version UID value for the given class.
	 */
	public static long computeDefaultSUID(Class cl) {
		if (!Serializable.class.isAssignableFrom(cl) || Proxy.isProxyClass(cl)) {
			return 0L;
		}

		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);

			dout.writeUTF(cl.getName());

			int classMods = cl.getModifiers()
					& (Modifier.PUBLIC | Modifier.FINAL | Modifier.INTERFACE | Modifier.ABSTRACT);

			/*
			 * compensate for javac bug in which ABSTRACT bit was set for an
			 * interface only if the interface declared methods
			 */
			Method[] methods = cl.getDeclaredMethods();
			if ((classMods & Modifier.INTERFACE) != 0) {
				classMods = (methods.length > 0) ? (classMods | Modifier.ABSTRACT) : (classMods & ~Modifier.ABSTRACT);
			}
			dout.writeInt(classMods);

			if (!cl.isArray()) {
				/*
				 * compensate for change in 1.2FCS in which
				 * Class.getInterfaces() was modified to return Cloneable and
				 * Serializable for array classes.
				 */
				Class[] interfaces = cl.getInterfaces();
				String[] ifaceNames = new String[interfaces.length];
				for (int i = 0; i < interfaces.length; i++) {
					ifaceNames[i] = interfaces[i].getName();
				}
				Arrays.sort(ifaceNames);
				for (int i = 0; i < ifaceNames.length; i++) {
					dout.writeUTF(ifaceNames[i]);
				}
			}

			Field[] fields = cl.getDeclaredFields();
			//// Begin pepe hack: remove taint fields
			List<Field> filteredFields = new ArrayList<Field>();
			for (Field field : fields) {
				if (!field.getName().endsWith(ShadowFieldRewriter.TAINT_SUFFIX)) {
					filteredFields.add(field);
				}
			}
			fields = filteredFields.toArray(new Field[] {});
			/// End pepe hack
			MemberSignature[] fieldSigs = new MemberSignature[fields.length];
			for (int i = 0; i < fields.length; i++) {
				fieldSigs[i] = new MemberSignature(fields[i]);
			}
			Arrays.sort(fieldSigs, new Comparator() {
				public int compare(Object o1, Object o2) {
					String name1 = ((MemberSignature) o1).name;
					String name2 = ((MemberSignature) o2).name;
					return name1.compareTo(name2);
				}
			});
			for (int i = 0; i < fieldSigs.length; i++) {
				MemberSignature sig = fieldSigs[i];
				int mods = sig.member.getModifiers()
						& (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
								| Modifier.VOLATILE | Modifier.TRANSIENT);
				if (((mods & Modifier.PRIVATE) == 0) || ((mods & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)) {
					dout.writeUTF(sig.name);
					dout.writeInt(mods);
					dout.writeUTF(sig.signature);
				}
			}

			/// Begin pepe hack: hasStaticInitializer is private native in ObjectStreamClass
			Class<?> c;
			boolean hasClinit;
			try {
				c = Class.forName("java.io.ObjectStreamClass");
				Method m = c.getDeclaredMethod("hasStaticInitializer", Class.class);
				hasClinit = (Boolean) m.invoke(null, cl);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			/// end pepe hack

			if (hasClinit) {
				dout.writeUTF("<clinit>");
				dout.writeInt(Modifier.STATIC);
				dout.writeUTF("()V");
			}

			Constructor[] cons = cl.getDeclaredConstructors();
			MemberSignature[] consSigs = new MemberSignature[cons.length];
			for (int i = 0; i < cons.length; i++) {
				consSigs[i] = new MemberSignature(cons[i]);
			}
			Arrays.sort(consSigs, new Comparator() {
				public int compare(Object o1, Object o2) {
					String sig1 = ((MemberSignature) o1).signature;
					String sig2 = ((MemberSignature) o2).signature;
					return sig1.compareTo(sig2);
				}
			});
			for (int i = 0; i < consSigs.length; i++) {
				MemberSignature sig = consSigs[i];
				int mods = sig.member.getModifiers()
						& (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
								| Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT | Modifier.STRICT);
				if ((mods & Modifier.PRIVATE) == 0) {
					dout.writeUTF("<init>");
					dout.writeInt(mods);
					dout.writeUTF(sig.signature.replace('/', '.'));
				}
			}

			MemberSignature[] methSigs = new MemberSignature[methods.length];
			for (int i = 0; i < methods.length; i++) {
				methSigs[i] = new MemberSignature(methods[i]);
			}
			Arrays.sort(methSigs, new Comparator() {
				public int compare(Object o1, Object o2) {
					MemberSignature ms1 = (MemberSignature) o1;
					MemberSignature ms2 = (MemberSignature) o2;
					int comp = ms1.name.compareTo(ms2.name);
					if (comp == 0) {
						comp = ms1.signature.compareTo(ms2.signature);
					}
					return comp;
				}
			});
			for (int i = 0; i < methSigs.length; i++) {
				MemberSignature sig = methSigs[i];
				int mods = sig.member.getModifiers()
						& (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
								| Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT | Modifier.STRICT);
				if ((mods & Modifier.PRIVATE) == 0) {
					dout.writeUTF(sig.name);
					dout.writeInt(mods);
					dout.writeUTF(sig.signature.replace('/', '.'));
				}
			}

			dout.flush();

			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hashBytes = md.digest(bout.toByteArray());
			long hash = 0;
			for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
				hash = (hash << 8) | (hashBytes[i] & 0xFF);
			}
			return hash;
		} catch (IOException ex) {
			throw new InternalError();
		} catch (NoSuchAlgorithmException ex) {
			throw new SecurityException(ex.getMessage());
		}
	}

	/**
	 * Returns JVM type signature for given class.
	 */
	static String getClassSignature(Class cl) {
		StringBuffer sbuf = new StringBuffer();
		while (cl.isArray()) {
			sbuf.append('[');
			cl = cl.getComponentType();
		}
		if (cl.isPrimitive()) {
			if (cl == Integer.TYPE) {
				sbuf.append('I');
			} else if (cl == Byte.TYPE) {
				sbuf.append('B');
			} else if (cl == Long.TYPE) {
				sbuf.append('J');
			} else if (cl == Float.TYPE) {
				sbuf.append('F');
			} else if (cl == Double.TYPE) {
				sbuf.append('D');
			} else if (cl == Short.TYPE) {
				sbuf.append('S');
			} else if (cl == Character.TYPE) {
				sbuf.append('C');
			} else if (cl == Boolean.TYPE) {
				sbuf.append('Z');
			} else if (cl == Void.TYPE) {
				sbuf.append('V');
			} else {
				throw new InternalError();
			}
		} else {
			sbuf.append('L' + cl.getName().replace('.', '/') + ';');
		}
		return sbuf.toString();
	}
}
