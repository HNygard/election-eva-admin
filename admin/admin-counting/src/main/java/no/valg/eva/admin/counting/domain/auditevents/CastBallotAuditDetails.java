package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.counting.domain.model.CastBallot;

public class CastBallotAuditDetails {
	private String id;
	private CastBallot.Type type;
	private boolean binaryDataIncluded;
	private CandidateVotesAuditDetails candidateVotesAuditDetails;

	public CastBallotAuditDetails(CastBallot castBallot) {
		this.id = castBallot.getId();
		this.type = castBallot.getType();
		this.binaryDataIncluded = castBallot.getBinaryData() != null;
		if (type == MODIFIED) {
			this.candidateVotesAuditDetails = new CandidateVotesAuditDetails(castBallot.getCandidateVotes());
		}
	}

	public JsonObject toJsonObject() {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("id", id)
				.add("type", type.name())
				.add("binaryDataIncluded", binaryDataIncluded);
		if (candidateVotesAuditDetails != null) {
			jsonBuilder.add("candidateVotes", candidateVotesAuditDetails.toJsonArray());
		}
		return jsonBuilder.asJsonObject();
	}
}
