package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import org.joda.time.DateTime;

public class ElectionDayAuditEvent extends AuditEvent {
	private final ElectionDay electionDay;

	public ElectionDayAuditEvent(UserData userData, ElectionDay electionDay, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.CENTRAL_CONFIGURATION, outcome, detail);
		this.electionDay = electionDay;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.addDate("date", electionDay.getDate());
		builder.addTime("startTime", electionDay.getStartTime());
		builder.addTime("endTime", electionDay.getEndTime());

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return ElectionDay.class;
	}

	public static Class[] objectClasses(@SuppressWarnings("unused") AuditEventType auditEventType) {
		return new Class[] { ElectionDay.class };
	}
}
