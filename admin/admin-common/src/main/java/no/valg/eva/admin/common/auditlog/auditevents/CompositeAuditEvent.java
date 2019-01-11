package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.List;

import no.evote.security.UserData;

import com.google.common.collect.Lists;

/**
 * Collects one or more sub events generated during service method execution and stores in a ThreadLocal List.
 */
public class CompositeAuditEvent extends AbstractAuditEvent {
	public CompositeAuditEvent(UserData userData) {
		super(userData);
	}

	private static final ThreadLocal<List<AuditEvent>> COLLECTED_EVENTS = new ThreadLocal<>();

	public static void addAuditEvent(AuditEvent event) {
		List<AuditEvent> auditEvents = getAuditEvents();
		auditEvents.add(event);
	}

	public static void initializeForThread() {
		COLLECTED_EVENTS.set(Lists.<AuditEvent> newArrayList());
	}

	public static List<AuditEvent> getAuditEvents() {
		List<AuditEvent> auditEvents = COLLECTED_EVENTS.get();
		if (auditEvents == null) {
			throw new IllegalStateException(
					"CompositeAuditEvent must be initialized for thread before use. Is the service method leading up to this error properly @AuditLog-annotated?");
		}
		return auditEvents;
	}

	public static void clearCollectedEvents() {
		COLLECTED_EVENTS.remove();
	}

	@Override
	public List<AuditEvent> getAndClearAllEvents() {
		List<AuditEvent> auditEvents = getAuditEvents();
		clearCollectedEvents();
		return auditEvents;
	}
}
