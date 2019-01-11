package no.valg.eva.admin.backend.auditlog;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import no.evote.model.BaseEntity;
import no.evote.security.UserData;
import no.evote.service.security.ErrorCodeMapper;
import no.valg.eva.admin.backend.service.impl.AuditLogServiceEjb;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

import org.apache.log4j.Logger;

public class SecuredObjectsInterceptor {
	private static final String LOGGER_NAME = "SecuredObjectsLogger";
	private static final String OUTCOME_SUCCESS = "000";
	private static final String EMPTY = "";
	private static final String DELIMITER = ", ";
	private static final char PARAMETER_SEPARATOR = ',';

	private final Logger logger = Logger.getLogger(LOGGER_NAME);

	private ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

	@SuppressWarnings("unused")
	public SecuredObjectsInterceptor() {
	}

	SecuredObjectsInterceptor(ErrorCodeMapper errorCodeMapper) {
		this.errorCodeMapper = errorCodeMapper;
	}

	@AroundInvoke
	public Object logCall(final InvocationContext ctx) throws Exception {
		Class<?> calledClass = ctx.getTarget().getClass();
		Method calledMethod = ctx.getMethod();
		Object[] parameters = ctx.getParameters();

		if (skipLogging(calledClass, calledMethod)) {
			return ctx.proceed();
		}

		UserData userData = findUserDataAsFirstParameter(parameters);
		try {
			Object result = ctx.proceed();
			logInfo(userData, calledClass, calledMethod, parameters);
			return result;
		} catch (Exception e) {
			logError(userData, calledClass, calledMethod, parameters, e);
			throw e;
		}
	}

	private String createInfo(Object[] parameters) {
		if (parameters != null) {
			return generateParameterList(Arrays.asList(parameters));
		} else {
			return "";
		}
	}

	private boolean skipLogging(Class<?> calledClass, Method calledMethod) {
		return isLogService(calledClass) || (!isSecured(calledClass, calledMethod) || !Modifier.isPublic(calledMethod.getModifiers()));
	}

	private boolean isLogService(Class<?> calledClass) {
		return calledClass.equals(AuditLogServiceEjb.class);
	}

	private boolean isSecured(Class<?> clazz, Method method) {
		return findSecObjAnnotation(clazz, method) != null;
	}

	private Security findSecObjAnnotation(Class<?> clazz, Method method) {
		Security annotation = clazz.getAnnotation(Security.class);
		Security methodAnnotation = method.getAnnotation(Security.class);

		if (methodAnnotation != null) {
			annotation = methodAnnotation;
		}
		return annotation;
	}

	private UserData findUserDataAsFirstParameter(Object[] parameters) {
		if (parameters != null && parameters.length > 0 && parameters[0] instanceof UserData) {
			return (UserData) parameters[0];
		}
		return null;
	}

	private void logInfo(final UserData userData, final Class<?> calledClass, final Method calledMethod, final Object[] parameters) {
		String logMessage = buildLogMessage(userData, calledClass, calledMethod, parameters, OUTCOME_SUCCESS, null);
		logger.info(logMessage);
	}

	private void logError(final UserData userData, final Class<?> calledClass, final Method calledMethod, final Object[] parameters, Exception exc) {
		String outcome = errorCodeMapper.map(exc);
		String logMessage = buildLogMessage(userData, calledClass, calledMethod, parameters, outcome, exc);
		logger.error(logMessage);
	}

	private String buildLogMessage(UserData userData, Class<?> calledClass, Method calledMethod, Object[] parameters, String outcome, Exception exception) {
		String parameterList = createInfo(parameters);

		OperatorRole operatorRole = userData != null ? userData.getOperatorRole() : null;
		String electionEvent = operatorRole != null && operatorRole.getMvElection() != null ? operatorRole.getMvElection().getElectionEventId() : EMPTY;
		String roleId = operatorRole != null ? operatorRole.getRole().getId() : EMPTY;
		String roleAreaRights = operatorRole != null ? operatorRole.getMvArea().getActualAreaLevel().name() : EMPTY;
		String roleElectionRights = operatorRole != null ? operatorRole.getMvElection().getActualElectionLevel().name() : EMPTY;
		String uid = userData != null ? userData.getUid() : EMPTY;
		InetAddress clientAddress = getClientIPAddress(userData);
		String stackTraceElement = exception != null && exception.getStackTrace() != null && exception.getStackTrace().length > 0
				? exception.getStackTrace()[0].toString() : EMPTY;

		StringBuilder sb = new StringBuilder();
		sb.append("class=").append(calledClass.getSimpleName()).append(DELIMITER);
		sb.append("method=").append(calledMethod.getName()).append(DELIMITER);
		sb.append("parameters=").append(parameterList).append(DELIMITER);
		sb.append("outcome=").append(outcome).append(DELIMITER);
		if (!EMPTY.equals(stackTraceElement)) {
			sb.append("errorLocation=").append(stackTraceElement).append(DELIMITER);
		}
		sb.append("client=").append(clientAddress.getHostAddress()).append(DELIMITER);
		sb.append("electionEvent=").append(electionEvent).append(DELIMITER);
		sb.append("uid=").append(uid).append(DELIMITER);
		sb.append("role=").append(roleId).append(DELIMITER);
		sb.append("roleAreaLevel=").append(roleAreaRights).append(DELIMITER);
		sb.append("roleElectionLevel=").append(roleElectionRights);

		return sb.toString();
	}

	private InetAddress getClientIPAddress(UserData userData) {
		if (userData != null && userData.getClientAddress() != null) {
			return userData.getClientAddress();
		} else {
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
	}

	static String generateParameterList(final Iterable<?> args) {
		Iterator<?> iter = args.iterator();
		StringBuilder builder = new StringBuilder();

		boolean isFirst = true;
		while (iter.hasNext()) {
			String elementValue = valueOf(iter.next());
			if (isFirst) {
				builder.append(elementValue);
				isFirst = false;
			} else {
				builder.append(PARAMETER_SEPARATOR).append(elementValue);
			}
		}

		return "<" + builder.toString() + ">";
	}

	private static String valueOf(final Object o) {
		String idToBeLogged;
		if (o instanceof BaseEntity) {
			idToBeLogged = String.valueOf(((BaseEntity) o).getPk());
		} else if (o instanceof UserData) {
			idToBeLogged = UserData.class.getSimpleName();
		} else {
			idToBeLogged = String.valueOf(o);
		}
		return idToBeLogged;
	}
}
