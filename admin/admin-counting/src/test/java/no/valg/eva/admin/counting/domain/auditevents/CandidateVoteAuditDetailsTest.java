package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;

import javax.json.JsonObject;

import no.valg.eva.admin.counting.domain.model.CandidateVote;

import org.testng.annotations.Test;

public class CandidateVoteAuditDetailsTest {
	@Test
	public void toJsonObject_givenCandidateVoteWithoutRenumberPosition_returnsCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CandidateVoteConfig config = new CountingAuditEventTestObjectMother.CandidateVoteConfig();
		CandidateVote candidateVote = CountingAuditEventTestObjectMother.candidateVote(config);
		JsonObject jsonObject = new CandidateVoteAuditDetails(candidateVote).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.candidateVoteJsonObject(config));
	}

	@Test
	public void toJsonObject_givenCandidateVoteWithRenumberPosition_returnsCorrectJson() throws Exception {
		CountingAuditEventTestObjectMother.CandidateVoteConfig config = new CountingAuditEventTestObjectMother.CandidateVoteConfig(
				CountingAuditEventTestObjectMother.CandidateVoteConfig.RENUMBER_POSITION);
		CandidateVote candidateVote = CountingAuditEventTestObjectMother.candidateVote(config);
		JsonObject jsonObject = new CandidateVoteAuditDetails(candidateVote).toJsonObject();
		assertThat(jsonObject).isEqualTo(CountingAuditEventTestObjectMother.candidateVoteJsonObject(config));
	}
}
