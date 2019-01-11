package no.valg.eva.admin.common.auditlog;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.interceptor.InvocationContext;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent;

import org.apache.commons.lang3.ClassUtils;

/**
 * This class resolves the correct audit event from a method invocation, and constructs the event.
 * <p/>
 * Audit event classes must implement {@code public static Class[] objectClasses(AuditEventType)}. It must return the classes of the objects being audit-logged,
 * or {@link no.valg.eva.admin.common.auditlog.auditevents.AuditEvent.None} if no object class is supported or relevant.
 * <p/>
 * Audit event classes must provide one constructor for every variation of {@code objectClasses(AuditEventType)}. The constructor signature must match {code
 * (UserData, [any classes from objectClasses], AuditEventType, Outcome, String)}, where the last argument is 'detail'.
 */
public class AuditEventFactory {
	private final Method method;
	private final Object[] parameters;
	private final AuditLog auditLogAnnotation;
	private final UserData userData;
	private final AuditedObjectSource auditedObjectSource;
	private final Class[] auditObjectClasses;
	private final Object[] auditedParameters;

	public AuditEventFactory(InvocationContext context) {
		this(context.getMethod(), context.getParameters());
	}

	public AuditEventFactory(Method method, Object[] parameters) {
		this.method = requireNonNull(method);
		this.parameters = requireNonNull(parameters);
		this.auditLogAnnotation = findAuditLogAnnotation(method);

		if (auditLogAnnotation != null) { 
			userData = getUserData();
			auditedObjectSource = getAuditedObjectSource();
			if (auditedObjectSource != AuditedObjectSource.Collected) {
				auditObjectClasses = getAuditObjectClasses();
				if (auditedObjectSource.equals(AuditedObjectSource.Parameters)) {
					auditedParameters = getAuditedParameters(auditObjectClasses, method);
				} else if (auditedObjectSource.equals(AuditedObjectSource.ReturnValue)) {
					if (auditObjectClasses.length != 1) {
						throw new IllegalArgumentException("Only one object can be audited when retrieved from " + AuditedObjectSource.ReturnValue.name());
					}
					auditedParameters = null;
				} else {
					// auditedObjectSource is AuditedObjectSource.ParametersAndReturnValue
					auditedParameters = getAuditedParameters(Arrays.copyOfRange(auditObjectClasses, 0, auditObjectClasses.length - 1), method);
				}
			} else {
				auditedParameters = new Object[0];
				auditObjectClasses = new Class[0];
			}
		} else {
			userData = null;
			auditedObjectSource = null;
			auditObjectClasses = null;
			auditedParameters = null;
		}
	}

	private AuditLog findAuditLogAnnotation(Method method) {
		for (Annotation annotation : method.getDeclaredAnnotations()) {
			if (annotation instanceof AuditLog) {
				return (AuditLog) annotation;
			}
		}
		return null;
	}

	public boolean isAuditedInvocation() {
		return auditLogAnnotation != null;
	}

	public AbstractAuditEvent buildSuccessfulAuditEvent(Object returnValue) {
		verifyMethodIsAudited();

		try {
			Constructor<? extends AbstractAuditEvent> constructor = getAuditEventConstructor();
			List<Object> constructorParameters = getAuditEventConstructorParameters(Outcome.Success, null, returnValue);

			return constructor.newInstance(constructorParameters.toArray());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
			throw new RuntimeException(errrorMessage(), e);
		}
	}

	public AbstractAuditEvent buildErrorAuditEvent(String detail) {
		verifyMethodIsAudited();

		try {
			Constructor<? extends AbstractAuditEvent> constructor = getAuditEventConstructor();
			List<Object> constructorParameters = getAuditEventConstructorParameters(Outcome.GenericError, detail, null);

			return constructor.newInstance(constructorParameters.toArray());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(errrorMessage(), e);
		}
	}

	private String errrorMessage() {
		if (auditLogAnnotation == null) {
			return "Unable to create audit event for event class";
		}
		return format("Unable to create audit event for event class: %s", auditLogAnnotation.eventClass());
	}

	private Constructor<? extends AbstractAuditEvent> getAuditEventConstructor() throws NoSuchMethodException {
		return getAuditEventConstructor(auditLogAnnotation.eventClass(), auditObjectClasses, auditLogAnnotation.objectSource());
	}

	public static Constructor<? extends AbstractAuditEvent> getAuditEventConstructor(Class<? extends AbstractAuditEvent> auditEventClass,
			Class[] auditObjectClasses,
			AuditedObjectSource auditedObjectSource)
			throws NoSuchMethodException {
		List<Class> constructorParameterTypes = new ArrayList<>();
		constructorParameterTypes.add(UserData.class);
		// If this is a collected or multi event type, the information below are gathered when adding sub events
		if (auditedObjectSource != AuditedObjectSource.Collected) {
			constructorParameterTypes.addAll(Arrays.asList(auditObjectClasses));
			constructorParameterTypes.add(AuditEventTypes.class); // burde egentlig vært AuditEventType
			constructorParameterTypes.add(Outcome.class);
			constructorParameterTypes.add(String.class);
		}
		return auditEventClass.getConstructor(constructorParameterTypes.toArray(new Class<?>[constructorParameterTypes.size()]));
	}

	private List<Object> getAuditEventConstructorParameters(Outcome outcome, String detail, Object returnValue) {
		List<Object> constructorParameters = new ArrayList<>();
		constructorParameters.add(userData);
		if (auditedObjectSource.equals(AuditedObjectSource.Parameters) || auditedObjectSource.equals(AuditedObjectSource.ParametersAndReturnValue)) {
			constructorParameters.addAll(Arrays.asList(auditedParameters));
		}
		if (auditedObjectSource.equals(AuditedObjectSource.ReturnValue) || auditedObjectSource.equals(AuditedObjectSource.ParametersAndReturnValue)) {
			constructorParameters.add(returnValue);
		}
		if (auditedObjectSource != AuditedObjectSource.Collected) {
			constructorParameters.add(auditLogAnnotation.eventType());
			constructorParameters.add(outcome);
			constructorParameters.add(detail);
		}
		return constructorParameters;
	}

	private void verifyMethodIsAudited() {
		if (!isAuditedInvocation()) {
			throw new IllegalStateException("Method is not audited: " + method.getName());
		}
	}

	private UserData getUserData() {
		if (parameters == null || parameters.length == 0 || !(parameters[0] instanceof UserData)) {
			throw new IllegalArgumentException("UserData must be the first parameter");
		}
		return (UserData) parameters[0];
	}

	private AuditedObjectSource getAuditedObjectSource() {
		return auditLogAnnotation.objectSource();
	}

	private Class[] getAuditObjectClasses() {
		Class<? extends AbstractAuditEvent> auditEventClass = auditLogAnnotation.eventClass();
		try {
			Method objectClassMethod = auditEventClass.getDeclaredMethod(AuditEvent.OBJECT_CLASSES_METHOD_NAME, AuditEventType.class);
			return (Class[]) objectClassMethod.invoke(null, auditLogAnnotation.eventType());
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException(auditEventClass.getSimpleName() + " does not implement \"public static Class[] "
					+ AuditEvent.OBJECT_CLASSES_METHOD_NAME
					+ "(AuditEventType auditEventType)\"", e);
		}
	}

	/** allows null valued parameters */
	private Object[] getAuditedParameters(Class[] objectClasses, Method method) {
		List<Object> objects = new ArrayList<>();
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			Class parameterClass = method.getParameterTypes()[i];
			Object parameter = parameters[i];
			for (Class objectClass : objectClasses) {
				// noinspection unchecked
				if (ClassUtils.isAssignable(parameterClass, objectClass, true)) {
					objects.add(parameter);
					break;
				}
			}

		}
		return objects.toArray();
	}

	public void initialize() {
		if (auditLogAnnotation.eventClass() == CompositeAuditEvent.class) {
			CompositeAuditEvent.initializeForThread();
		}
	}
}
