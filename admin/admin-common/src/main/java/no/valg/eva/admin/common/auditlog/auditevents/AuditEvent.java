package no.valg.eva.admin.common.auditlog.auditevents;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;

import org.joda.time.DateTime;

/**
 * Common superclass for all single audit events.
 *
 * Remember to implement {@code public static Class[] objectClasses(AuditEventType)}, if the class is instantiated by
 * {@link no.valg.eva.admin.common.auditlog.AuditEventFactory}.
 */
public abstract class AuditEvent extends AbstractAuditEvent implements Serializable {
	public static final String OBJECT_CLASSES_METHOD_NAME = "objectClasses";

	private final DateTime timestamp;
	private final String detail;
	private final AuditEventType eventType;
	private final Process process;
	private final Outcome outcome;

	protected AuditEvent(String uid, String electionEventId, String roleId, AreaPath roleAreaPath, ElectionPath roleElectionPath,
			InetAddress clientIpAddress, DateTime timestamp, AuditEventType eventType, Process process, Outcome outcome, String detail) {
		super(uid, electionEventId, roleId, roleAreaPath, roleElectionPath, clientIpAddress);
		this.timestamp = requireNonNull(timestamp, "Timestamp is required");
		this.eventType = requireNonNull(eventType, "Audit event type is required");
		this.process = requireNonNull(process, "Process is required");
		this.outcome = requireNonNull(outcome, "Outcome is required");
		this.detail = detail;
	}

	protected AuditEvent(UserData userData, DateTime dateTime, AuditEventType crudType, Process process, Outcome outcome, String detail) {
		this(userData.getUid(), userData.getElectionEventId(), userData.getRoleId(),
				userData.getOperatorAreaPath(), userData.getOperatorElectionPath(),
				userData.getClientAddress(), dateTime, crudType, process, outcome, detail);
	}

	public DateTime timestamp() {
		return timestamp;
	}

	<T> void addStringElementToJson(JsonBuilder jsonBuilder, String name, T t, Predicate<T> predicate, Function<T, String> mapper) {
		if (predicate.test(t)) {
			jsonBuilder.add(name, mapper.apply(t));
		} else {
			jsonBuilder.addNull(name);
		}
	}

	<T> void addIntegerElementToJson(JsonBuilder jsonBuilder, String name, T t, Predicate<T> predicate, Function<T, Integer> mapper) {
		if (predicate.test(t)) {
			jsonBuilder.add(name, mapper.apply(t));
		} else {
			jsonBuilder.addNull(name);
		}
	}

	<T> void addLongElementToJson(JsonBuilder jsonBuilder, String name, T t, Predicate<T> predicate, Function<T, Long> mapper) {
		if (predicate.test(t)) {
			jsonBuilder.add(name, mapper.apply(t));
		} else {
			jsonBuilder.addNull(name);
		}
	}

	<T> void addDateTimeElementToJson(JsonBuilder jsonBuilder, String name, T t, Predicate<T> predicate, Function<T, DateTime> mapper) {
		if (predicate.test(t)) {
			jsonBuilder.addDateTime(name, mapper.apply(t));
		} else {
			jsonBuilder.addNull(name);
		}
	}

	/**
	 * @return type of the object being logged, or {@code null} if no object is logged.
	 */
	public abstract Class objectType();

	public AuditEventType eventType() {
		return eventType;
	}

	public Process process() {
		return process;
	}

	public Outcome outcome() {
		return outcome;
	}

	public String detail() {
		return detail;
	}

	/**
	 * Override this method to provide a JSON representation of the object.
	 * <p/>
	 *
	 * @return selected fields of the object, represented as JSON, or 'null' if no such representation is available.
	 */
	public abstract String toJson();

	/**
	 * This class indicates that no object is supported or relevant.
	 *
	 * @see #toJson()
	 */
	public static class None {
	}

	public List<AuditEvent> getAndClearAllEvents() {
		return newArrayList(this);
	}

}
