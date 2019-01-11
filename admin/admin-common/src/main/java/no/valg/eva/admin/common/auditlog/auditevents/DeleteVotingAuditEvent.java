package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;

public class DeleteVotingAuditEvent extends AuditEvent {
	private final Long votingPk;

	public DeleteVotingAuditEvent(UserData userData, Long votingPk, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, DateTime.now(), auditEventType, Process.VOTING, outcome, detail);
		this.votingPk = votingPk;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		builder.add("votingPk", votingPk);

		return builder.toJson();
	}

	@Override
	public Class objectType() {
		return Voting.class;
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Delete.equals(auditEventType)) {
			return new Class[]{Long.class};
		} else {
			throw new IllegalArgumentException("Unsupported audit event type: " + auditEventType.name());
		}
	}
}
