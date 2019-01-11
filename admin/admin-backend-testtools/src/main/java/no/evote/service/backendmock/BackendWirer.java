package no.evote.service.backendmock;

import no.evote.exception.EvoteException;
import org.mockito.Mockito;
import org.mockito.mock.MockCreationSettings;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;

/**
 * Dependency injection tool for EVA Admin Backend.
 * <p/>
 * {@link #initServices()} must be invoked to initialize an instance.
 */
public class BackendWirer {
    private final EntityManager entityManager;
    private final Object wiringTarget;

    private Map<Class, Object> fieldClassToInstanceMap;
    private Map<Class, Object> implClassToInstanceMap;
    private Map<Class, Object> preinitializedImplClassToInstanceMap = new HashMap<>();

    public BackendWirer(final Object wiringTarget, final EntityManager entityManager) {
        this.wiringTarget = wiringTarget;
        this.entityManager = entityManager;
    }

    public BackendWirer(Object wiringTarget, EntityManager entityManager, Object... preInitializedFields) throws ClassNotFoundException {
        this.wiringTarget = wiringTarget;
        this.entityManager = entityManager;
        for (Object preInitializedField : preInitializedFields) {
            String className = nameForMockedClass(preInitializedField);
            preinitializedImplClassToInstanceMap.put(Class.forName(className), preInitializedField);
        }
    }

    private String nameForMockedClass(Object preInitializedField) {
        MockCreationSettings<?> mockCreationSettings = Mockito.mockingDetails(preInitializedField).getMockCreationSettings();
        return mockCreationSettings.getTypeToMock().getName();
    }

    public void initServices() {
        fieldClassToInstanceMap = new HashMap<>();
        implClassToInstanceMap = new HashMap<>();
        createDefaultProducedInstances();
        createProducersAndWireProducedInstances();
        wireNonProducedInstances();
        invokePostConstruct();
    }

    private void createDefaultProducedInstances() {
        Field[] wiredFields = ClassUtil.getAnnotatedDeclaredFields(wiringTarget.getClass(), Wired.class, false);
        Iterable<Field> nonProducerFields = Arrays.stream(wiredFields).filter(field -> !isProducerNeeded(field)).collect(Collectors.toList());

        for (Field field : nonProducerFields) {
            createObjectAndSetFieldValue(field);
        }
    }

    private void createObjectAndSetFieldValue(final Field field) {
        Class fieldClass = field.getType();
        Class implClass = getImplClass(field);

        Object instance = preinitializedImplClassToInstanceMap.get(implClass);
        if (instance == null) {
            instance = ClassUtil.createInstance(implClass);
        }

        setInstanceOnWiringTarget(field, instance);

        fieldClassToInstanceMap.put(fieldClass, instance);
        implClassToInstanceMap.put(implClass, instance);
    }

    private Class getImplClass(Field field) {
        Class implClass = field.getAnnotation(Wired.class).impl();
        if (Wired.Unassigned.class.equals(implClass)) {
            return field.getType();
        }
        return implClass;
    }

    private void setInstanceOnWiringTarget(final Field field, final Object instance) {
        field.setAccessible(true);

        try {
            field.set(wiringTarget, instance);
        } catch (IllegalAccessException e) {
            throw new EvoteException("Could not set field " + field + " to object of class " + instance.getClass(), e);
        }
    }

    private void createProducersAndWireProducedInstances() {
        Field[] wiredFields = ClassUtil.getAnnotatedDeclaredFields(wiringTarget.getClass(), Wired.class, false);
        Iterable<Field> producerFields = Arrays.stream(wiredFields).filter(this::isProducerNeeded).collect(Collectors.toList());

        for (Field field : producerFields) {
            Class producerClass = field.getAnnotation(Wired.class).producer();
            Object producer = ClassUtil.createInstance(producerClass);

            wireInstance(producer); // setter felter på producer

            Object producedInstance = createProducedInstance(field, producer);
            setInstanceOnWiringTarget(field, producedInstance);

            fieldClassToInstanceMap.put(field.getType(), producedInstance);
            implClassToInstanceMap.put(producedInstance.getClass(), producedInstance);
        }
    }

    private Object createProducedInstance(final Field field, final Object producer) {
        Method producerMethod = getProducerMethod(producer, field.getType());
        try {
            if (producerMethod.getParameterTypes().length == 0) {
                return producerMethod.invoke(producer);
            }
            return producerMethod.invoke(producer, (Object[]) new InjectionPoint[1]);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new EvoteException("Could not create produced instance: " + e);
        }
    }

    private Method getProducerMethod(final Object producer, final Class producedType) {
        Method[] producerMethods = ClassUtil.getAnnotatedMethods(producer.getClass(), Produces.class, true);
        for (Method producerMethod : producerMethods) {
            // noinspection unchecked
            if (producedType.isAssignableFrom(producerMethod.getReturnType())) {
                return producerMethod;
            }
        }
        throw new IllegalArgumentException("No producer method found");
    }

    private boolean isProducerNeeded(final Field field) {
        Wired wiredAnnotation = field.getAnnotation(Wired.class);
        return !wiredAnnotation.producer().equals(Wired.Unassigned.class);
    }

    private void wireNonProducedInstances() {
        for (Object instance : fieldClassToInstanceMap.values()) {
            wireInstance(instance);
        }
    }

    private void wireInstance(final Object instance) {
        Field[] ejbFields = ClassUtil.getAnnotatedDeclaredFields(instance.getClass(), EJB.class, true);
        for (Field field : ejbFields) {
            setField(instance, field);
        }

        Field[] injectFields = ClassUtil.getAnnotatedDeclaredFields(instance.getClass(), Inject.class, true);
        for (Field field : injectFields) {
            setField(instance, field);
        }

        Field[] persistenceContextFields = ClassUtil.getAnnotatedDeclaredFields(instance.getClass(), PersistenceContext.class, true);
        for (Field field : persistenceContextFields) {
            setField(instance, field, entityManager);
        }

        Field[] resourceFields = ClassUtil.getAnnotatedDeclaredFields(instance.getClass(), Resource.class, true);
        for (Field field : resourceFields) {
            setField(instance, field);
        }
    }

    private void invokePostConstruct() {
        for (Object instance : fieldClassToInstanceMap.values()) {
            Method[] postConstructMethods = ClassUtil.getAnnotatedMethods(instance.getClass(), PostConstruct.class, true);
            for (Method postConstructMethod : postConstructMethods) {
                try {
                    postConstructMethod.setAccessible(true);
                    postConstructMethod.invoke(instance);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new EvoteException("Exception in invokePostConstruct: " + e);
                }
            }
        }
    }

    private void setField(final Object instance, final Field field) {
        Class<?> classToInject = field.getType();
        Object objectToInject = findObjectToInject(classToInject);

        if (objectToInject == null) {
            return;
        }

        setField(instance, field, objectToInject);
    }

    private void setField(final Object instance, final Field field, final Object objectToInject) {
        field.setAccessible(true);
        try {
            field.set(instance, objectToInject);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new EvoteException("Exception setting field: " + e);
        }
    }

    private Object findObjectToInject(final Class<?> classToInject) {
        Object preinitializedObject = preinitializedImplClassToInstanceMap.get(classToInject);
        if (preinitializedObject != null) {
            return preinitializedObject;
        }

        Object objectFoundByInterface = fieldClassToInstanceMap.get(classToInject);
        if (objectFoundByInterface != null) {
            return objectFoundByInterface;
        }

        Object objectFoundByImplClass = implClassToInstanceMap.get(classToInject);
        if (objectFoundByImplClass != null) {
            return objectFoundByImplClass;
        }

        return null;
    }
}
