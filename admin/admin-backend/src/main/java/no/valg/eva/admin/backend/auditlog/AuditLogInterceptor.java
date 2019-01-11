package no.valg.eva.admin.backend.auditlog;

import java.io.Serializable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;

import org.apache.log4j.Logger;

/**
 * Intercepts method invocations to methods annotated with {@link no.valg.eva.admin.common.auditlog.AuditLog}.
 * <p/>
 * The implementation is robust, so that if audit logging fails, the business operation will still proceed as normal.
 */
public class AuditLogInterceptor implements Serializable {
	private final Logger logger = Logger.getLogger(AuditLogInterceptor.class);

	@Inject
	private AuditLogServiceBean auditLogService;

	@SuppressWarnings("unused")
	public AuditLogInterceptor() {
	}

	AuditLogInterceptor(AuditLogServiceBean auditLogService) {
		this.auditLogService = auditLogService;
	}

	@AroundInvoke
	public Object intercept(InvocationContext context) throws Exception {
		AuditEventFactory auditEventFactory = null;
		try {
			auditEventFactory = createAuditEventFactory(context);
		} catch (Exception e) {
			logger.error("Unable to audit log method invocation - failed to create audit event.", e);
			return context.proceed();
		}

		if (!auditEventFactory.isAuditedInvocation()) {
			return context.proceed();
		}

		auditEventFactory.initialize();

		try {
			Object returnValue = context.proceed();
			try {
				AbstractAuditEvent auditEvent = auditEventFactory.buildSuccessfulAuditEvent(returnValue);
				if (!auditEvent.muteEvent()) {
					auditLogService.addToAuditTrail(auditEvent);
				}
			} catch (Exception exceptionWhenAuditLogging) {
				logger.error("Unable to audit log successful method invocation", exceptionWhenAuditLogging);
			}
			return returnValue;
		} catch (Exception e) {
			try {
				AbstractAuditEvent auditEvent = auditEventFactory.buildErrorAuditEvent(e.getMessage());
				auditLogService.addToAuditTrail(auditEvent);
			} catch (Exception exceptionWhenAuditLogging) {
				logger.error("Unable to audit log failed method invocation", exceptionWhenAuditLogging);
			}
			throw e;
		}
	}

	/**
	 * Creates an {@link AuditEventFactory}. May be overridden in tests, to provide a non-default implementation.
	 */
	AuditEventFactory createAuditEventFactory(InvocationContext context) {
		return new AuditEventFactory(context);
	}
}
