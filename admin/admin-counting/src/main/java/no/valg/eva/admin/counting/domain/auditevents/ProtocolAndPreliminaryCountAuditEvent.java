package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.ProtocolAndPreliminaryCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.joda.time.DateTime;

public class ProtocolAndPreliminaryCountAuditEvent extends AuditEvent {
	private VoteCountAuditDetails protocolVoteCountAuditDetails;
	private VoteCountAuditDetails preliminaryVoteCountAuditDetails;

	@SuppressWarnings("unused")
	public ProtocolAndPreliminaryCountAuditEvent(UserData userData, CountContext context, ProtocolAndPreliminaryCount protocolAndPreliminaryCount,
			AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.COUNTING, outcome, detail);
	}

	@SuppressWarnings("unused")
	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == null) {
			throw new IllegalArgumentException("auditEventType must not be null");
		}
		switch ((AuditEventTypes) auditEventType) {
		case SaveCount:
			return new Class[] { CountContext.class, ProtocolAndPreliminaryCount.class };
		case ApproveCount:
			return new Class[] { CountContext.class, ProtocolAndPreliminaryCount.class };
		case RevokeCount:
			return new Class[] { CountContext.class, ProtocolAndPreliminaryCount.class };
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
		if (protocolVoteCountAuditDetails != null && preliminaryVoteCountAuditDetails != null) {
			return new JsonBuilder()
					.add("protocolVoteCount", protocolVoteCountAuditDetails.toJsonObject())
					.add("preliminaryVoteCount", preliminaryVoteCountAuditDetails.toJsonObject())
					.toJson();
		}
		return new JsonBuilder().toJson();
	}

	private void retrieveVoteCountAuditDetails() {
		VoteCountAuditDetails aProtocolVoteCountAuditDetails = ThreadLocalVoteCountAuditDetailsMap.INSTANCE.get(PROTOCOL);
		if (aProtocolVoteCountAuditDetails != null) {
			this.protocolVoteCountAuditDetails = aProtocolVoteCountAuditDetails;
		}
		VoteCountAuditDetails aPreliminaryVoteCountAuditDetails = ThreadLocalVoteCountAuditDetailsMap.INSTANCE.get(PRELIMINARY);
		if (aPreliminaryVoteCountAuditDetails != null) {
			this.preliminaryVoteCountAuditDetails = aPreliminaryVoteCountAuditDetails;
		}
	}
}
