package no.valg.eva.admin.counting.domain.auditevents;

import java.util.Collection;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import no.valg.eva.admin.counting.domain.model.CandidateVote;

public class CandidateVotesAuditDetails {
	private CandidateVoteAuditDetails[] candidateVoteAuditDetailses;

	public CandidateVotesAuditDetails(Collection<CandidateVote> candidateVotes) {
		this.candidateVoteAuditDetailses = new CandidateVoteAuditDetails[candidateVotes.size()];
		Iterator<CandidateVote> candidateVoteIterator = candidateVotes.iterator();
		for (int i = 0; candidateVoteIterator.hasNext(); i++) {
			candidateVoteAuditDetailses[i] = new CandidateVoteAuditDetails(candidateVoteIterator.next());
		}
	}

	public JsonArray toJsonArray() {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (CandidateVoteAuditDetails candidateVoteAuditDetails : candidateVoteAuditDetailses) {
			arrayBuilder.add(candidateVoteAuditDetails.toJsonObject());
		}
		return arrayBuilder.build();
	}
}
