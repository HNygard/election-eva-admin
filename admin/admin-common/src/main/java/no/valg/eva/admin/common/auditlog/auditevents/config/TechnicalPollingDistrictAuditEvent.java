package no.valg.eva.admin.common.auditlog.auditevents.config;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;

public class TechnicalPollingDistrictAuditEvent extends PollingDistrictAuditEvent<TechnicalPollingDistrict> {

	public TechnicalPollingDistrictAuditEvent(UserData userData, TechnicalPollingDistrict district, AuditEventTypes crudType, Outcome outcome,
			String detail) {
		super(userData, district, crudType, outcome, detail);
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { TechnicalPollingDistrict.class };
	}
}
