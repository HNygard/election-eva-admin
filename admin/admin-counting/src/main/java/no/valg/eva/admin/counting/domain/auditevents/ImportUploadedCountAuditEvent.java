package no.valg.eva.admin.counting.domain.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.joda.time.DateTime;

public class ImportUploadedCountAuditEvent extends AuditEvent {
	private static ThreadLocal<VoteCountAuditDetails> threadLocalVoteCountAuditDetails = new ThreadLocal<>();

	private final int batchId;
	private final Long electionEventPk;
	private final String accessPath;
	private VoteCountAuditDetails voteCountAuditDetails;

	public ImportUploadedCountAuditEvent(
		UserData userData, int batchId, Long electionEventPk, Jobbkategori category, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.COUNTING, outcome, detail);
		this.batchId = batchId;
		this.electionEventPk = electionEventPk;
		this.accessPath = category.toAccessPath();
	}

	public static void saveVoteCountAuditDetails(VoteCount voteCount, boolean splitCount, boolean includeCastBallots) {
		threadLocalVoteCountAuditDetails.set(new VoteCountAuditDetails(voteCount, splitCount, includeCastBallots));
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[]{int.class, Long.class, Jobbkategori.class};
	}

	private void retrieveVoteCountAuditDetails() {
		VoteCountAuditDetails auditDetails = threadLocalVoteCountAuditDetails.get();
		if (auditDetails != null) {
			threadLocalVoteCountAuditDetails.remove();
			this.voteCountAuditDetails = auditDetails;
		}
	}

	@Override
	public Class objectType() {
		return VoteCount.class;
	}

	@Override
	public String toJson() {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("batchId", batchId)
				.add("electionEventPk", electionEventPk)
				.add("accessPath", accessPath);
		retrieveVoteCountAuditDetails();
		if (voteCountAuditDetails != null) {
			jsonBuilder.add("voteCount", voteCountAuditDetails.toJsonObject());
		}
		return jsonBuilder.toJson();
	}
}
