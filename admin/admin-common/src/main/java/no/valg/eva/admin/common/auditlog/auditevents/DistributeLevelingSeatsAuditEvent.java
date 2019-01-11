package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlement;

import org.joda.time.DateTime;

public class DistributeLevelingSeatsAuditEvent extends AuditEvent {
	public DistributeLevelingSeatsAuditEvent(UserData userData, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.SETTLEMENT, outcome, detail);
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Create.equals(auditEventType)) {
			return new Class[0];
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}

	@Override
	public String toJson() {
		return new JsonBuilder().toJson();
	}

	@Override
	public Class objectType() {
		return LevelingSeatSettlement.class;
	}
}
