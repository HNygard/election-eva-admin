package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.joda.time.DateTime;

public class PreliminaryCountAuditEvent extends AuditEvent {
	private VoteCountAuditDetails voteCountAuditDetails;

	@SuppressWarnings("unused")
	public PreliminaryCountAuditEvent(UserData userData, CountContext context, PreliminaryCount preliminaryCount, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.COUNTING, outcome, detail);
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == null) {
			throw new IllegalArgumentException("auditEventType must not be null");
		}
		switch ((AuditEventTypes) auditEventType) {
		case ApproveCount:
			return new Class[] { CountContext.class, PreliminaryCount.class };
		case SaveCount:
			return new Class[] { CountContext.class, PreliminaryCount.class };
		case RevokeCount:
			return new Class[] { CountContext.class, PreliminaryCount.class };
		default:
			throw new UnsupportedOperationException();
		}
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
		VoteCountAuditDetails auditDetails = ThreadLocalVoteCountAuditDetailsMap.INSTANCE.get(PRELIMINARY);
		if (auditDetails != null) {
			this.voteCountAuditDetails = auditDetails;
		}
	}
}
