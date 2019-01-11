package no.valg.eva.admin.common.auditlog;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;

@InterceptorBinding
@Retention(RUNTIME)
@Target({ METHOD })
public @interface AuditLog {
	/**
	 * @return the class representing this audit event.
	 */
	Class<? extends AbstractAuditEvent> eventClass();

	/**
	 * @return the audit event type.
	 */
	AuditEventTypes eventType();

	/**
	 * @return where in the method signature the audited object can be found.
	 */
	AuditedObjectSource objectSource() default AuditedObjectSource.Parameters;
}
