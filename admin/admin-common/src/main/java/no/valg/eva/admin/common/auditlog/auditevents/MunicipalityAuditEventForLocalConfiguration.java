package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.Municipality;

public class MunicipalityAuditEventForLocalConfiguration extends MunicipalityAuditEvent {
	public MunicipalityAuditEventForLocalConfiguration(UserData userData, Municipality municipality, AuditEventTypes auditEventType, Outcome outcome,
			String detail) {
		super(userData, municipality, auditEventType, LOCAL_CONFIGURATION, outcome, detail);
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Municipality.class };
	}

}
