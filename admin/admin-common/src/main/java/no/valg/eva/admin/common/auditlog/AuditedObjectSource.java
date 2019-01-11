package no.valg.eva.admin.common.auditlog;

/**
 * Where will the audited object be found.
 */
public enum AuditedObjectSource {
	/**
	 * Parameters on the method annotated with {@link AuditLog}. The audit event class must implement
	 * {@code public static Class[] objectClasses(AuditEventType auditEventType)} which must return the types of the parameters to insert into the event type's
	 * constructor.
	 */
	Parameters,
	/**
	 * Collect data underway
	 */
	Collected,
	/**
	 * Return value from the method annotated with {@link AuditLog}.
	 */
	ReturnValue,
	/**
	 * The parameters passed to the audit event's constructor is both from the arguments list, with the return value last.
	 * This is a mix of {@link #Parameters} and {@link #ReturnValue}.
	 */
	ParametersAndReturnValue
}
