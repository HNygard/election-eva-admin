package no.valg.eva.admin.backend.common.auditlog;

import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Formats audit log messages. A new instance must be created for every audit event to format.
 */
public class AuditLogMessageFormatter {
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS Z");
	private final AuditEvent auditEvent;

	public AuditLogMessageFormatter(AuditEvent auditEvent) {
		this.auditEvent = auditEvent;
	}

	public String buildMessage() {
		StringBuilder sb = new StringBuilder();

		append("time", dateTimeFormatter.print(auditEvent.timestamp()), sb);
		append("client", auditEvent.clientIpAddress().getHostAddress(), sb);

		if (auditEvent.electionEventId() != null) {
			append("electionEvent", auditEvent.electionEventId(), sb);
		}

		append("process", auditEvent.process().name(), sb);

		if (auditEvent.objectType() != null) {
			append("objectType", auditEvent.objectType().getSimpleName(), sb);
		}

		append("eventType", auditEvent.eventType().name(), sb);
		append("outcome", auditEvent.outcome().name(), sb);
		append("uid", auditEvent.uid(), sb);

		if (auditEvent.roleId() != null) {
			append("role", auditEvent.roleId(), sb);
		}
		if (auditEvent.roleAreaPath() != null) {
			append("roleAreaPath", auditEvent.roleAreaPath().path(), sb);
		}
		if (auditEvent.roleElectionPath() != null) {
			append("roleElectionPath", auditEvent.roleElectionPath().path(), sb);
		}
		if (auditEvent.detail() != null) {
			append("detail", escapeQuotes(auditEvent.detail()), sb);
		}
		String auditObjectAsJson = auditEvent.toJson();
		if (auditObjectAsJson != null) {
			append("auditObject", auditObjectAsJson, sb);
		}

		return sb.toString();
	}

	private static void append(final String name, String value, StringBuilder sb) {
		boolean encloseValueInBrackets = value.contains(" ");
		boolean needsDelimiter = sb.length() > 0;

		if (needsDelimiter) {
			sb.append(", ");
		}
		sb.append(name).append("=");
		if (encloseValueInBrackets) {
			sb.append("\"");
		}
		sb.append(value);
		if (encloseValueInBrackets) {
			sb.append("\"");
		}
	}

	private String escapeQuotes(String detail) {
		return detail.replace("\"", "\\\"").replace("\n", "\\n");
	}
}
