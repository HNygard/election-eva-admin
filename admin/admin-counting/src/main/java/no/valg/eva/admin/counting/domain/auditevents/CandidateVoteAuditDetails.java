package no.valg.eva.admin.counting.domain.auditevents;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.counting.domain.model.CandidateVote;

public class CandidateVoteAuditDetails {
	private String candidateId;
	private String voteCategoryId;
	private Integer renumberPosition;

	public CandidateVoteAuditDetails(CandidateVote candidateVote) {
		this.candidateId = candidateVote.getCandidate().getId();
		this.voteCategoryId = candidateVote.getVoteCategory().getId();
		this.renumberPosition = candidateVote.getRenumberPosition();
	}

	public JsonObject toJsonObject() {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("candidateId", candidateId)
				.add("voteCategoryId", voteCategoryId);
		if (renumberPosition != null) {
			jsonBuilder.add("renumberPosition", renumberPosition);
		}
		return jsonBuilder.asJsonObject();
	}
}
