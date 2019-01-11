package no.valg.eva.admin.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.persistence.Column;

import org.apache.log4j.Logger;

public final class EqualsHashCodeUtil {

	private static final int PRIME = 31;
	private static Logger log = Logger.getLogger(EqualsHashCodeUtil.class);

	private EqualsHashCodeUtil() {
	}

	public static int genericHashCode(final Object obj) {
		int hashCode = 1;

		Method[] methods = findMethodsToUseInEqualsAndHashCode(obj.getClass());
		Object[] values = invokeMethods(obj, methods);

		for (Object value : values) {
			hashCode = PRIME * hashCode + ((value == null) ? 0 : value.hashCode());
		}

		return hashCode;
	}

	public static boolean genericEquals(final Object obj1, final Object obj2) {
		// Basic comparison
		if (obj1 == obj2) {
			return true;
		}
		if (obj2 == null) {
			return false;
		}

		Method[] methods = findMethodsToUseInEqualsAndHashCode(obj1.getClass());
		Object[] values1 = invokeMethods(obj1, methods);
		Object[] values2 = invokeMethods(obj2, methods);

		// Compare values in arrays using their equals method, return false if they differ
		for (int i = 0; i < values1.length; i++) {
			if (values1[i] == null) {
				if (values2[i] != null) {
					return false;
				}
			} else if (!values1[i].equals(values2[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Invokes all passed methods on an object and returns values in an array
	 */
	private static Object[] invokeMethods(final Object obj, final Method[] methods) {
		ArrayList<Object> results = new ArrayList<>();

		for (Method method : methods) {
			Object result;
			try {
				result = method.invoke(obj, (Object[]) null);
				results.add(result);
			} catch (IllegalAccessException | InvocationTargetException e) {
				log.error(e.getMessage(), e);
			}
		}

		return results.toArray();
	}

	/**
	 * Returns all declared getter methods with @Column annotation
	 */
	private static Method[] findMethodsToUseInEqualsAndHashCode(final Class<?> clazz) {
		ArrayList<Method> methods = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			Column column = method.getAnnotation(Column.class);
			if (column != null && method.getName().startsWith("get")) {
				methods.add(method);
			}
		}
		return methods.toArray(new Method[methods.size()]);
	}

}
