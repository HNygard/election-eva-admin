package no.valg.eva.admin.common.auditlog.auditevents;

import static com.google.common.base.Optional.fromNullable;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.google.common.base.Optional;

/**
 * This class represents all audit events that does not have custom implementations.
 */
public class SimpleAuditEvent extends AuditEvent {

	private Class objectType;
	private Map<String, Optional<String>> jsonProperties;

	public SimpleAuditEvent(String uid, String electionEventId, String roleId, AreaPath roleAreaPath,
			ElectionPath roleElectionPath,
			InetAddress clientIpAddress, DateTime timestamp, AuditEventType eventType, Process process, Outcome outcome, String detail, Class objectType) {
		super(uid, electionEventId, roleId, roleAreaPath, roleElectionPath, clientIpAddress, timestamp, eventType,
				eventType instanceof SimpleAuditEventType ? ((SimpleAuditEventType) eventType).process() : process, outcome, detail);
		this.objectType = objectType;
	}

	public SimpleAuditEvent(String uid, String electionEventId, String roleId, AreaPath roleAreaPath,
			ElectionPath roleElectionPath,
			InetAddress clientIpAddress, DateTime timestamp, AuditEventType eventType, Process process, Outcome outcome, String detail, Class objectType,
			Map<String, Optional<String>> jsonProperties1) {
		super(uid, electionEventId, roleId, roleAreaPath, roleElectionPath, clientIpAddress, timestamp, eventType, process, outcome, detail);
		this.objectType = objectType;
		jsonProperties = jsonProperties1;
	}

	public static SimpleAuditEvent.Builder from(UserData userData) {
		return new SimpleAuditEvent.Builder(userData);
	}

	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this, SHORT_PREFIX_STYLE);
		addMandatoryFields(toStringBuilder);
		addDetail(toStringBuilder);
		return toStringBuilder.toString();
	}

	private void addMandatoryFields(ToStringBuilder toStringBuilder) {
		toStringBuilder.append("uid", uid())
				.append("roleId", roleId())
				.append("roleAreaPath", roleAreaPath())
				.append("eventType", eventType())
				.append("outcome", outcome());
	}

	private void addDetail(ToStringBuilder toStringBuilder) {
		if (detail() != null) {
			toStringBuilder
					.append("detail", detail());
		}
	}

	@Override
	public Class objectType() {
		return objectType;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] {};
	}

	@Override
	public String toJson() {
		if (jsonProperties != null) {
			JsonBuilder jsonBuilder = new JsonBuilder();
			for (Map.Entry<String, Optional<String>> jsonProperty : jsonProperties.entrySet()) {
				jsonBuilder.add(jsonProperty.getKey(), jsonProperty.getValue().orNull());
			}
			return jsonBuilder.toJson();
		} else {
			return null;
		}
	}

	public static class Builder {
		private UserData userData;
		private AuditEventType eventType;
		private Outcome outcome;
		private Process process;
		private String detail;
		private Class objectType;
		private Map<String, Optional<String>> jsonFields = new LinkedHashMap<>();

		Builder(UserData userData) {
			this.userData = userData;
		}

		public Builder ofType(AuditEventType eventType) {
			this.eventType = eventType;
			if (eventType instanceof SimpleAuditEventType) {
				SimpleAuditEventType simpleAuditEventType = (SimpleAuditEventType) eventType;
				this.process = simpleAuditEventType.process();
			}
			return this;
		}

		public Builder withOutcome(Outcome outcome) {
			this.outcome = outcome;
			return this;
		}

		public Builder withProcess(Process process) {
			this.process = process;
			return this;
		}

		public SimpleAuditEvent build() {
			requireNonNull(userData, "UserData is required");
			requireNonNull(eventType, "AuditEventType is required");

			if (outcome == null) {
				outcome = resolveOutcomeIfPossible();
			}

			return new SimpleAuditEvent(userData.getUid(), userData.getElectionEventId(), userData.getRoleId(),
					userData.getOperatorAreaPath(), userData.getOperatorElectionPath(),
					userData.getClientAddress(), new DateTime(), eventType, process, outcome, detail, objectType, jsonFields);
		}

		private Outcome resolveOutcomeIfPossible() {
			if (eventType instanceof SimpleAuditEventType && ((SimpleAuditEventType) eventType).hasSingleOutcome()) {
				return ((SimpleAuditEventType) eventType).getSingleOutcome();
			} else {
				throw new NullPointerException("Outcome is required");
			}
		}

		public Builder withDetail(String detail) {
			this.detail = detail;
			return this;
		}

		public Builder withObjectType(Class objectType) {
			this.objectType = objectType;
			return this;
		}

		public Builder withAuditObjectProperty(String name, String value) {
			jsonFields.put(name, fromNullable(value));
			return this;
		}
	}

	@Override
	public List<AuditEvent> getAndClearAllEvents() {
		return super.getAndClearAllEvents();
	}
}
