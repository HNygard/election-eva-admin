package no.evote.util;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.PersistenceContext;

/**
 * Test mock utils class.
 */
@SuppressWarnings("unchecked")
public final class MockUtils {

	private MockUtils() {
	}

	public static class MockContent<T> {
		private T instance;
		private List<Object> mocks = new ArrayList<>();

		public MockContent(T t) {
			this.instance = t;
		}

		public T getInstance() {
			return instance;
		}

		public List<Object> getMocks() {
			return mocks;
		}
	}

	/**
	 * Create mocks for inject constructor and all injected fields in cls.
	 * 
	 * @param cls
	 *            Class containing injected fields.
	 * 
	 * @return List with mocks.
	 */
	public static <T> MockContent<T> mockInjects(Class<T> cls) throws IllegalAccessException, InstantiationException, NoSuchFieldException,
			InvocationTargetException {

		T t = instanceOfMockedType(cls);

		MockContent<T> result = new MockContent<>(t);
		result.getMocks().addAll(mockInjects(result.getInstance()));
		return result;
	}

	private static <T> T instanceOfMockedType(Class<T> cls) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		// Default constructor? Use it.
		for (Constructor<?> constructor : cls.getConstructors()) {
			if (constructor.getParameterCount() == 0) {
				return cls.newInstance();
			}
		}
		// No default constructor, try finding a constructor annotated with @Inject
		for (Constructor<?> constructor : cls.getConstructors()) {
			if (isInjectable(constructor) && constructor.getParameterCount() > 0) {
				Object[] mockParameterArray = new Object[constructor.getParameterCount()];
				Class<?>[] parameterTypes = constructor.getParameterTypes();
				for (int i = 0; i < constructor.getParameterCount(); i++) {
					if (parameterTypes[i].isPrimitive()) {
						// Legg evt. inn manglende primitivtyper her
						if (parameterTypes[i].equals(Boolean.TYPE)) {
							mockParameterArray[i] = false;
						}
 					} else {
						mockParameterArray[i] = mock(parameterTypes[i]);
					}
				}
				return (T) constructor.newInstance(mockParameterArray);
			}
		}
		throw new IllegalArgumentException("Could not find suitable constructor in " + cls.getName());
	}

	private static boolean isInjectable(Constructor<?> constructor) {
		Annotation[] annotations = constructor.getAnnotations();
		for (Annotation annotation : annotations) {
			if (isInjectable(annotation)) {
				return true;
			}
		}
		return false;
	}

	private static <T> Constructor<T> getInjectConstructor(Class<T> cls) {
		Constructor<?>[] constructors = cls.getConstructors();
		for (Constructor<?> constructor : constructors) {
			Annotation annotation = constructor.getAnnotation(Inject.class);
			if (annotation != null && constructor.getParameterTypes().length > 0) {
				return (Constructor<T>) constructor;
			}
		}
		return null;
	}

	/**
	 * Create mocks for all injected fields in target.
	 *
	 * @param target
	 *            Object containing injected fields.
	 *
	 * @return List with mocks.
	 */
	public static List<Object> mockInjects(Object target) throws NoSuchFieldException, IllegalAccessException {
		Set<Field> fields = findInjectedFields(target.getClass());
		List<Object> result = new ArrayList<>();
		for (Field field : fields) {
			Object mock = mock(field.getType(), RETURNS_DEEP_STUBS);
			set(field, target, mock);
			result.add(mock);
		}
		return result;
	}

	/**
	 * Get mock of type mockClass from mocks list.
	 * @param mocks
	 *            Mocks from <code>mockInjects</code>
	 * @param mockClass
	 *            The class to be mocked
	 * @param matchStrictly
	 *            classes strictly, ie assignable both ways
	 */
	public static <T> T getMock(List<Object> mocks, Class<T> mockClass, boolean matchStrictly) {
		for (Object mock : mocks) {
			if (mockClass.isAssignableFrom(mock.getClass()) && (!matchStrictly || mock.getClass().getSuperclass().isAssignableFrom(mockClass))) {
				return (T) mock;
			}
		}
		throw new RuntimeException("Could not find mock of type " + mockClass + ". Mocks: " + mocks);
	}

	/**
	 * Does mock of type mockClass exist in mocks list.
	 * 
	 * @param mocks
	 *            Mocks from <code>mockInjects</code>
	 * @param mockClass
	 *            Mock class.
	 */
	public static boolean hasMock(List<Object> mocks, Class mockClass) {
		for (Object mock : mocks) {
			if (mockClass.isAssignableFrom(mock.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set private field on an object.
	 * 
	 * @param obj
	 *            Object to be set private field on.
	 * @param fieldName
	 *            Name of field to be set.
	 * @param value
	 *            Value to be placed in field.
	 */
	public static void setPrivateField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		setPrivateField(obj, fieldName, value, false);
	}

	public static void setPrivateField(Object obj, String fieldName, Object value, boolean force) throws NoSuchFieldException,
			IllegalAccessException {
		Field field = findPrivateField(obj.getClass(), fieldName);
		if (isValidInjectable(field) && !force) {
			throw new IllegalAccessException("Not possible to set field (" + field.getName() + "), it is injectable! Use MockUtils.mockInjects");
		}
		set(field, obj, value);
	}

	public static Object getPrivateField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = findPrivateField(obj.getClass(), fieldName);
		field.setAccessible(true);
		return field.get(obj);
	}

	private static void set(Field field, Object obj, Object value) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(obj, value);
	}

	private static Field findPrivateField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			}
			return findPrivateField(superClass, fieldName);
		}
	}

	private static Set<Field> findInjectedFields(Class<?> clazz) throws NoSuchFieldException {
		return findInjectedFields(clazz, new HashSet<>());
	}

	private static Set<Field> findInjectedFields(Class<?> clazz, Set<Field> result) throws NoSuchFieldException {
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			findInjectedFields(superClass, result);
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (isValidInjectable(field)) {
				result.add(field);
			}
		}
		return result;
	}

	private static boolean isValidInjectable(Field field) {
		if (hasGenerics(field) || field.getType().isPrimitive() || field.getType() == String.class) {
			return false;
		}
		Annotation[] annotations = field.getAnnotations();
		for (Annotation annotation : annotations) {
			if (isInjectable(annotation)) {
				return true;
			}
		}
		return isConstructorInjected(field);
	}

	private static boolean isConstructorInjected(Field field) {
		Constructor constructor = getInjectConstructor(field.getDeclaringClass());
		if (constructor != null) {
			for (Class cls : constructor.getParameterTypes()) {
				if (cls == field.getType()) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasGenerics(Field field) {
		return !field.getGenericType().equals(field.getType());
	}

	private static boolean isInjectable(Annotation annotation) {
		boolean beanInject = annotation instanceof Inject || annotation instanceof EJB;
		boolean resourceInject = annotation instanceof Resource || annotation instanceof PersistenceContext;
		return beanInject || resourceInject;
	}
}
