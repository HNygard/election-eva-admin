package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.joda.time.DateTime;

public class FinalCountAuditEvent extends AuditEvent {
	private VoteCountAuditDetails voteCountAuditDetails;

	public FinalCountAuditEvent(UserData userData, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.COUNTING, outcome, detail);
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[0];
	}

	@Override
	public Class objectType() {
		return VoteCount.class;
	}

	@Override
	public String toJson() {
		retrieveVoteCountAuditDetails();
		if (voteCountAuditDetails != null) {
			return voteCountAuditDetails.toJson();
		}
		return new JsonBuilder().toJson();
	}

	private void retrieveVoteCountAuditDetails() {
		VoteCountAuditDetails auditDetails = ThreadLocalVoteCountAuditDetailsMap.INSTANCE.get(FINAL);
		if (auditDetails != null) {
			this.voteCountAuditDetails = auditDetails;
		}
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.clear();
	}
}
