package no.valg.eva.admin.common.auditlog.auditevents;

import static java.util.Optional.ofNullable;
import static javax.json.Json.createObjectBuilder;

import java.util.Map;

import javax.json.JsonObjectBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.reporting.model.ReportExecution;

import org.joda.time.DateTime;

public class ReportAuditEvent extends AuditEvent {
	public static final String INTET_RAPPORTRESULTAT = "intet rapportresultat";
	public static final byte[] EMPTY = new byte[0];

	private ReportExecution reportResult;

	public ReportAuditEvent(UserData userData, ReportExecution reportResult, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.LOCAL_CONFIGURATION, outcome, detail);
		this.reportResult = reportResult;
	}

	@Override
	public Class objectType() {
		return ReportExecution.class;
	}

	@Override
	public String toJson() {
		final JsonBuilder objectBuilder = new JsonBuilder();
		if (reportResult != null) {
			objectBuilder.add("report", reportResult.getReportName());
			objectBuilder.add("fileName", reportResult.getFileName());
			objectBuilder.add("size", ofNullable(reportResult.getContent()).orElse(EMPTY).length);
			objectBuilder.add("format", reportResult.getFormat());
			addArguments(objectBuilder, reportResult);
		} else {
			objectBuilder.add("report", INTET_RAPPORTRESULTAT);
		}
		return objectBuilder.toJson();
	}

	private void addArguments(JsonBuilder objectBuilder, ReportExecution reportResult) {
		JsonObjectBuilder argumentsJsonObjectBuilder = createObjectBuilder();
		for (Map.Entry<String, String> entry : reportResult.getArguments().entrySet()) {
			String parameterId = entry.getKey();
			String parameterLabel = reportResult.getParameterLabels().get(parameterId);
			if (entry.getValue() != null) {
				argumentsJsonObjectBuilder.add(parameterLabel != null ? parameterLabel : parameterId, entry.getValue());
			} else {
				argumentsJsonObjectBuilder.addNull(parameterLabel != null ? parameterLabel : parameterId);
			}
		}
		objectBuilder.add("arguments", argumentsJsonObjectBuilder);
	}

	@Override
	public boolean muteEvent() {
		return !reportResult.isReady();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ReportExecution.class };
	}
}
