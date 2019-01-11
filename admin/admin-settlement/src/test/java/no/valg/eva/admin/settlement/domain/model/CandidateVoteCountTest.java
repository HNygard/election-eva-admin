package no.valg.eva.admin.settlement.domain.model;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;

import org.testng.annotations.Test;

public class CandidateVoteCountTest {

	@Test
	public void incrementVotes_givenVotes_incrementsVotes() throws Exception {
		CandidateVoteCount candidateVoteCount = new CandidateVoteCount();
		candidateVoteCount.setVotes(TEN);
		candidateVoteCount.incrementVotes(TEN);
		assertThat(candidateVoteCount.getVotes()).isEqualTo(TEN.add(TEN));
	}

	@Test
	public void testIncrementEarlyVotingVotes() throws Exception {
		CandidateVoteCount candidateVoteCount = new CandidateVoteCount();
		candidateVoteCount.setEarlyVotingVotes(TEN);
		candidateVoteCount.incrementEarlyVotingVotes(TEN);
		assertThat(candidateVoteCount.getEarlyVotingVotes()).isEqualTo(TEN.add(TEN));
	}

	@Test
	public void testIncrementElectionDayVotes() throws Exception {
		CandidateVoteCount candidateVoteCount = new CandidateVoteCount();
		candidateVoteCount.setElectionDayVotes(TEN);
		candidateVoteCount.incrementElectionDayVotes(TEN);
		assertThat(candidateVoteCount.getElectionDayVotes()).isEqualTo(TEN.add(TEN));
	}

	@Test
	public void getCandidateAffiliation_givenCandidateVoteCount_returnsCandidateAffiliation() throws Exception {
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		CandidateVoteCount candidateVoteCount = new CandidateVoteCount();
		candidateVoteCount.setCandidate(candidate);
		assertThat(candidateVoteCount.getCandidateAffiliation()).isSameAs(candidate.getAffiliation());
	}

	@Test
	public void equals_givenEqualSettlements_returnsTrue() throws Exception {
		Settlement settlement = mock(Settlement.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setSettlement(settlement);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setSettlement(settlement);
		assertThat(candidateVoteCount1).isEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenDifferentSettlements_returnsFalse() throws Exception {
		Settlement settlement1 = mock(Settlement.class);
		Settlement settlement2 = mock(Settlement.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setSettlement(settlement1);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setSettlement(settlement2);
		assertThat(candidateVoteCount1).isNotEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenEqualAffiliations_returnsTrue() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setAffiliation(affiliation);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setAffiliation(affiliation);
		assertThat(candidateVoteCount1).isEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenDifferentAffiliations_returnsFalse() throws Exception {
		Affiliation affiliation1 = mock(Affiliation.class);
		Affiliation affiliation2 = mock(Affiliation.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setAffiliation(affiliation1);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setAffiliation(affiliation2);
		assertThat(candidateVoteCount1).isNotEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenEqualCandidates_returnsTrue() throws Exception {
		Candidate candidate = mock(Candidate.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setCandidate(candidate);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setCandidate(candidate);
		assertThat(candidateVoteCount1).isEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenDifferentCandidates_returnsFalse() throws Exception {
		Candidate candidate1 = mock(Candidate.class);
		Candidate candidate2 = mock(Candidate.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setCandidate(candidate1);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setCandidate(candidate2);
		assertThat(candidateVoteCount1).isNotEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenEqualVoteCategories_returnsTrue() throws Exception {
		VoteCategory voteCategory = mock(VoteCategory.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setVoteCategory(voteCategory);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setVoteCategory(voteCategory);
		assertThat(candidateVoteCount1).isEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenDifferentVoteCategories_returnsFalse() throws Exception {
		VoteCategory voteCategory1 = mock(VoteCategory.class);
		VoteCategory voteCategory2 = mock(VoteCategory.class);
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setVoteCategory(voteCategory1);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setVoteCategory(voteCategory2);
		assertThat(candidateVoteCount1).isNotEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenEqualRankNumbers_returnsTrue() throws Exception {
		Integer rankNumber = 1;
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setRankNumber(rankNumber);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setRankNumber(rankNumber);
		assertThat(candidateVoteCount1).isEqualTo(candidateVoteCount2);
	}

	@Test
	public void equals_givenDifferentRankNumbers_returnsFalse() throws Exception {
		Integer rankNumber1 = 1;
		Integer rankNumber2 = 2;
		CandidateVoteCount candidateVoteCount1 = new CandidateVoteCount();
		candidateVoteCount1.setRankNumber(rankNumber1);
		CandidateVoteCount candidateVoteCount2 = new CandidateVoteCount();
		candidateVoteCount2.setRankNumber(rankNumber2);
		assertThat(candidateVoteCount1).isNotEqualTo(candidateVoteCount2);
	}
}
