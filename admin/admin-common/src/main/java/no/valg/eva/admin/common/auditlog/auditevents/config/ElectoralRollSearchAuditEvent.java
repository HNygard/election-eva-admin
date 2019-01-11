package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.ElectoralRollSearch;

import org.joda.time.DateTime;

public class ElectoralRollSearchAuditEvent extends AuditEvent {

	private final AreaPath areaPath;
	private final ElectoralRollSearch electoralRollSearch;

	public ElectoralRollSearchAuditEvent(UserData userData, AreaPath areaPath, ElectoralRollSearch electoralRollSearch,
			AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, LOCAL_CONFIGURATION, outcome, detail);
		this.areaPath = areaPath;
		this.electoralRollSearch = electoralRollSearch;
	}

	@Override
	public Class objectType() {
		return ElectoralRollSearch.class;
	}

	@Override
	public String toJson() {
		JsonBuilder json = new JsonBuilder();
		json.add("areaPath", areaPath.path());
		if (electoralRollSearch.hasValidSsn()) {
			json.add("ssn", electoralRollSearch.getSsn());
		} else {
			json.add("birthDate", electoralRollSearch.getBirthDate() != null ? electoralRollSearch.getBirthDate().toString("dd.MM.yyyy") : "");
			json.add("name", electoralRollSearch.getName());
		}
		return json.toJson();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { AreaPath.class, ElectoralRollSearch.class };
	}
}
