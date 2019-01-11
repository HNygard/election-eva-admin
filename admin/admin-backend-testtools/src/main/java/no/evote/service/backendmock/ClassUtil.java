package no.evote.service.backendmock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility for reflection.
 * <p/>
 * Code is based on <a
 * href="http://www.java2s.com/Code/Java/Reflection/GetAnnotatedDeclaredFields.htm">http://www.java2s.com/Code/Java/Reflection/GetAnnotatedDeclaredFields
 * .htm</a>
 */
final class ClassUtil {

	private ClassUtil() {
	}

	/**
	 * Create new instance of specified class and type
	 * 
	 * @param clazz
	 *            of instance
	 * @param <T>
	 *            type of object
	 * @return new Class instance
	 */
	static <T> T createInstance(final Class<T> clazz) {
		try {
			Constructor<T> zeroArgumentConstructor = clazz.getDeclaredConstructor();
			zeroArgumentConstructor.setAccessible(true);
			return zeroArgumentConstructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Could not instantiate " + clazz.getSimpleName(), e);
		}
	}

	/**
	 * Retrieving fields list of specified class and which are annotated by incoming annotation class If recursively is true, retrieving fields from all class
	 * hierarchy
	 * 
	 * @param clazz
	 *            - where fields are searching
	 * @param annotationClass
	 *            - specified annotation class
	 * @param recursively
	 *            param
	 * @return list of annotated fields
	 */
	static Field[] getAnnotatedDeclaredFields(final Class clazz, final Class<? extends Annotation> annotationClass, final boolean recursively) {
		Field[] allFields = getDeclaredFields(clazz, recursively);
		List<Field> annotatedFields = new LinkedList<>();

		for (Field field : allFields) {
			if (field.isAnnotationPresent(annotationClass)) {
				annotatedFields.add(field);
			}
		}

		return annotatedFields.toArray(new Field[annotatedFields.size()]);
	}

	private static Field[] getDeclaredFields(final Class clazz, final boolean recursively) {
		List<Field> fields = new LinkedList<>();
		Field[] declaredFields = clazz.getDeclaredFields();
		Collections.addAll(fields, declaredFields);

		Class superClass = clazz.getSuperclass();

		if (superClass != null && recursively) {
			Field[] declaredFieldsOfSuper = getDeclaredFields(superClass, recursively);
			if (declaredFieldsOfSuper.length > 0) {
				Collections.addAll(fields, declaredFieldsOfSuper);
			}
		}

		return fields.toArray(new Field[fields.size()]);
	}

	/**
	 * Find methods with a specific annotation.
	 * 
	 * @param clazz
	 *            the class to examine
	 * @param annotationClass
	 *            the annotation to find
	 * @param recursively
	 *            also searches superclasses
	 * @return methods with the specific annotation
	 */
	public static Method[] getAnnotatedMethods(final Class clazz, final Class<? extends Annotation> annotationClass, final boolean recursively) {
		Method[] allMethods = getDeclaredMethods(clazz, recursively);
		List<Method> annotatedMethods = new LinkedList<>();

		for (Method method : allMethods) {
			if (method.isAnnotationPresent(annotationClass)) {
				annotatedMethods.add(method);
			}
		}

		return annotatedMethods.toArray(new Method[annotatedMethods.size()]);
	}

	private static Method[] getDeclaredMethods(final Class clazz, final boolean recursively) {
		List<Method> methods = new LinkedList<>();
		Method[] declaredMethods = clazz.getDeclaredMethods();
		Collections.addAll(methods, declaredMethods);

		Class superClass = clazz.getSuperclass();

		if (superClass != null && recursively) {
			Method[] declaredMethodsOfSuper = getDeclaredMethods(superClass, recursively);
			if (declaredMethodsOfSuper.length > 0) {
				Collections.addAll(methods, declaredMethodsOfSuper);
			}
		}

		return methods.toArray(new Method[methods.size()]);
	}
}
