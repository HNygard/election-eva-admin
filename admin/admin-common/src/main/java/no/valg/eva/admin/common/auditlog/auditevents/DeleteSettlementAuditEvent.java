package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.settlement.model.Settlement;

import org.joda.time.DateTime;

public class DeleteSettlementAuditEvent extends AuditEvent {
	private final ElectionPath electionPath;
	private final AreaPath areaPath;

	public DeleteSettlementAuditEvent(UserData userData, ElectionPath electionPath, AreaPath areaPath, AuditEventTypes auditEventType, Outcome outcome,
			String detail) {
		super(userData, new DateTime(), auditEventType, Process.SETTLEMENT, outcome, detail);
		this.electionPath = electionPath;
		this.areaPath = areaPath;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.DeletedAllInArea.equals(auditEventType)) {
			return new Class[] { ElectionPath.class, AreaPath.class };
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("areaPath", areaPath.path());
		builder.add("electionPath", electionPath.path());
		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Settlement.class;
	}
}
