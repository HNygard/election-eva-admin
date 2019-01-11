package no.valg.eva.admin.common.auditlog.auditevents.config;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;

import org.joda.time.DateTime;

public class ResponsibleOfficerAuditEvent extends AuditEvent {

	private final ResponsibleOfficer responsibleOfficer;

	public ResponsibleOfficerAuditEvent(UserData userData, ResponsibleOfficer officer, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType == Save && officer.getPk() == null ? Create : Update, LOCAL_CONFIGURATION, outcome, detail);
		this.responsibleOfficer = officer;
	}

	@Override
	public Class objectType() {
		return ResponsibleOfficer.class;
	}

	@Override
	public String toJson() {
		JsonBuilder json = new JsonBuilder();
		if (responsibleOfficer.getAreaPath() != null) {
			json.add("areaPath", responsibleOfficer.getAreaPath().path());
		}
		json.add("pk", responsibleOfficer.getPk());
		json.add("firstName", responsibleOfficer.getFirstName());
		json.add("middleName", responsibleOfficer.getMiddleName());
		json.add("lastName", responsibleOfficer.getLastName());
		json.add("responsibility", responsibleOfficer.getResponsibilityId().getId());
		if (!AuditEventTypes.Delete.equals(eventType())) {
			json.add("displayOrder", responsibleOfficer.getDisplayOrder());
			json.add("address", responsibleOfficer.getAddress());
			json.add("postalCode", responsibleOfficer.getPostalCode());
			json.add("postalTown", responsibleOfficer.getPostalTown());
			json.add("email", responsibleOfficer.getEmail());
			json.add("tlf", responsibleOfficer.getTlf());
		}
		return json.toJson();
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { ResponsibleOfficer.class };
	}
}
