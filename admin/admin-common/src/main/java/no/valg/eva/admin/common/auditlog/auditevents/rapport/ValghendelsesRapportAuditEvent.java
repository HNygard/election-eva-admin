package no.valg.eva.admin.common.auditlog.auditevents.rapport;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;

import org.joda.time.DateTime;

public class ValghendelsesRapportAuditEvent extends AuditEvent {

	private ElectionPath electionEventPath;
	private List<ValghendelsesRapport> rapporter;

	public ValghendelsesRapportAuditEvent(UserData userData, ElectionPath electionEventPath, List<ValghendelsesRapport> rapporter,
			AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.CENTRAL_CONFIGURATION, outcome, detail);
		this.electionEventPath = electionEventPath;
		this.rapporter = rapporter;
	}

	@Override
	public String toJson() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("electionEventPath", electionEventPath.path());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (ValghendelsesRapport rapport : rapporter) {
			JsonObjectBuilder divisionObjectBuilder = Json.createObjectBuilder();
			divisionObjectBuilder.add("reportId", rapport.getRapportId());
			divisionObjectBuilder.add("visible", String.valueOf(rapport.isSynlig()));
			arrayBuilder.add(divisionObjectBuilder.build());
		}
		objectBuilder.add("reports", arrayBuilder.build());
		return objectBuilder.toJson();
	}

	@Override
	public Class objectType() {
		return ValghendelsesRapport.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ElectionPath.class, List.class };
	}
}
