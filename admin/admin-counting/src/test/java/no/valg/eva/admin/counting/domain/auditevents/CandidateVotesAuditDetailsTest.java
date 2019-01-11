package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import javax.json.JsonArray;

import no.valg.eva.admin.counting.domain.model.CandidateVote;

import org.testng.annotations.Test;

public class CandidateVotesAuditDetailsTest {
	@Test
	public void toJsonArray_givenCandidateVotes_returnCorrectJsonArray() throws Exception {
		CountingAuditEventTestObjectMother.CandidateVoteConfig[] configs = CountingAuditEventTestObjectMother.candidateVoteConfigs();
		Collection<CandidateVote> candidateVotes = CountingAuditEventTestObjectMother.candidateVotes(configs);
		JsonArray jsonArray = new CandidateVotesAuditDetails(candidateVotes).toJsonArray();
		assertThat(jsonArray).isEqualTo(CountingAuditEventTestObjectMother.candidateVoteJsonArray(configs));
	}
}
