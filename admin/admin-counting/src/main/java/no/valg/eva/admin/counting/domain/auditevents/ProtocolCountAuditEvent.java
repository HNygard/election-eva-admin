package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.ManualContestVotingsAuditDetails;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.joda.time.DateTime;

public class ProtocolCountAuditEvent extends AuditEvent {
	private VoteCountAuditDetails voteCountAuditDetails;
	private ManualContestVotingsAuditDetails manualContestVotingsAuditDetails;

	@SuppressWarnings("unused")
	public ProtocolCountAuditEvent(
			UserData userData, CountContext context, ProtocolCount protocolCount, AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.COUNTING, outcome, detail);
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == null) {
			throw new IllegalArgumentException("auditEventType must not be null");
		}
		switch ((AuditEventTypes) auditEventType) {
		case ApproveCount:
			return new Class[] { CountContext.class, ProtocolCount.class };
		case SaveCount:
			return new Class[] { CountContext.class, ProtocolCount.class };
		case RevokeCount:
			return new Class[] { CountContext.class, ProtocolCount.class };
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
		retrieveManualContestVotingsAuditDetails();
		if (voteCountAuditDetails != null && manualContestVotingsAuditDetails != null) {
			return new JsonBuilder()
					.add("voteCount", voteCountAuditDetails.toJsonObject())
					.add("manualContestVotings", manualContestVotingsAuditDetails.toJsonArray())
					.toJson();
		}
		if (voteCountAuditDetails != null) {
			return voteCountAuditDetails.toJson();
		}
		return new JsonBuilder().toJson();
	}

	private void retrieveVoteCountAuditDetails() {
		VoteCountAuditDetails auditDetails = ThreadLocalVoteCountAuditDetailsMap.INSTANCE.get(PROTOCOL);
		if (auditDetails != null) {
			this.voteCountAuditDetails = auditDetails;
		}
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.clear();
	}

	private void retrieveManualContestVotingsAuditDetails() {
		this.manualContestVotingsAuditDetails = ManualContestVotingsAuditDetails.THREAD_LOCAL.get();
		ManualContestVotingsAuditDetails.THREAD_LOCAL.remove();
	}
}
