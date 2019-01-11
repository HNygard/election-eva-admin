package no.valg.eva.admin.counting.domain.auditevents;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.counting.domain.model.BallotCount;

public class BallotCountAuditDetails {
	private final String ballotId;
	private final String ballotRejectionId;
	private final int unmodifiedBallots;
	private final Integer modifiedBallots;
	private final CastBallotsAuditDetails castBallotAuditsDetails;

	public BallotCountAuditDetails(BallotCount ballotCount, boolean splitCount, boolean includeCastBallots) {
		this.ballotId = ballotCount.getBallotId();
		this.ballotRejectionId = ballotCount.getBallotRejectionId();
		this.unmodifiedBallots = ballotCount.getUnmodifiedBallots();
		if (splitCount && ballotRejectionId == null && !ballotCount.isBlank()) {
			this.modifiedBallots = ballotCount.getModifiedBallots();
		} else {
			this.modifiedBallots = null;
		}
		if (includeCastBallots && !ballotCount.isBlank()) {
			this.castBallotAuditsDetails = new CastBallotsAuditDetails(ballotCount.getCastBallots());
		} else {
			this.castBallotAuditsDetails = null;
		}
	}

	public JsonObject toJsonObject() {
		JsonBuilder jsonBuilder = new JsonBuilder();
		if (ballotId != null) {
			jsonBuilder.add("ballotId", ballotId);
		}
		if (ballotRejectionId != null) {
			jsonBuilder.add("ballotRejectionId", ballotRejectionId);
		}
		jsonBuilder.add("unmodifiedBallots", unmodifiedBallots);
		if (modifiedBallots != null) {
			jsonBuilder.add("modifiedBallots", modifiedBallots);
		}
		if (castBallotAuditsDetails != null) {
			jsonBuilder.add("castBallots", castBallotAuditsDetails.toJsonArray());
		}
		return jsonBuilder.asJsonObject();
	}
}
