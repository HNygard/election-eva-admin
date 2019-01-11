package no.valg.eva.admin.settlement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.testng.annotations.Test;


public class AffiliationVoteCountTest {

	@Test
	public void setBaselineVotes_givenBaselineVotes_calculatesNewVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setAddedVotes(1);
		affiliationVoteCount.setSubtractedVotes(2);
		affiliationVoteCount.setBaselineVotes(10);
		assertThat(affiliationVoteCount.getVotes()).isEqualTo(9);
	}

	@Test
	public void setVotes_givenVotes_calculatesNewBaselineVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setAddedVotes(1);
		affiliationVoteCount.setSubtractedVotes(2);
		affiliationVoteCount.setVotes(9);
		assertThat(affiliationVoteCount.getBaselineVotes()).isEqualTo(10);
	}

	@Test
	public void setAddedVotes_givenAddedVotes_calculatesNewVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setBaselineVotes(10);
		affiliationVoteCount.setSubtractedVotes(2);
		affiliationVoteCount.setAddedVotes(1);
		assertThat(affiliationVoteCount.getVotes()).isEqualTo(9);
	}

	@Test
	public void setSubtractedVotes_givenSubtractedVotes_calculatesNewVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setBaselineVotes(10);
		affiliationVoteCount.setAddedVotes(1);
		affiliationVoteCount.setSubtractedVotes(2);
		assertThat(affiliationVoteCount.getVotes()).isEqualTo(9);
	}

	@Test
	public void incrementBallots_givenBallots_incrementsBallots() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setBallots(1);
		affiliationVoteCount.incrementBallots(2);
		assertThat(affiliationVoteCount.getBallots()).isEqualTo(3);
	}

	@Test
	public void incrementModifiedBallots_givenModifiedBallots_incrementsModifiedBallots() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setModifiedBallots(1);
		affiliationVoteCount.incrementModifiedBallots(2);
		assertThat(affiliationVoteCount.getModifiedBallots()).isEqualTo(3);
	}

	@Test
	public void incrementBaselineVotes_givenBaselineVotes_incrementsBaselineVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setBaselineVotes(1);
		affiliationVoteCount.incrementBaselineVotes(2);
		assertThat(affiliationVoteCount.getBaselineVotes()).isEqualTo(3);
	}

	@Test
	public void incrementEarlyVotingBallots_givenEarlyVotingBallots_incrementsEarlyVotingBallots() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setEarlyVotingBallots(1);
		affiliationVoteCount.incrementEarlyVotingBallots(2);
		assertThat(affiliationVoteCount.getEarlyVotingBallots()).isEqualTo(3);
	}

	@Test
	public void incrementEarlyVotingModifiedBallots_givenEarlyVotingModifiedBallots_incrementsEarlyVotingModifiedBallots() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setEarlyVotingModifiedBallots(1);
		affiliationVoteCount.incrementEarlyVotingModifiedBallots(2);
		assertThat(affiliationVoteCount.getEarlyVotingModifiedBallots()).isEqualTo(3);
	}

	@Test
	public void incrementElectionDayBallots_givenElectionDayBallots_incrementsElectionDayBallots() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setElectionDayBallots(1);
		affiliationVoteCount.incrementElectionDayBallots(2);
		assertThat(affiliationVoteCount.getElectionDayBallots()).isEqualTo(3);
	}

	@Test
	public void incrementElectionDayModifiedBallots_givenElectionDayModifiedBallots_incrementsElectionDayModifiedBallots() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setElectionDayModifiedBallots(1);
		affiliationVoteCount.incrementElectionDayModifiedBallots(2);
		assertThat(affiliationVoteCount.getElectionDayModifiedBallots()).isEqualTo(3);
	}

	@Test
	public void incrementAddedVotes_givenAddedVotes_incrementsAddedVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setAddedVotes(1);
		affiliationVoteCount.incrementAddedVotes(2);
		assertThat(affiliationVoteCount.getAddedVotes()).isEqualTo(3);
	}

	@Test
	public void incrementSubtractedVotes_givenSubtractedVotes_incrementsSubtractedVotes() throws Exception {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setSubtractedVotes(1);
		affiliationVoteCount.incrementSubtractedVotes(2);
		assertThat(affiliationVoteCount.getSubtractedVotes()).isEqualTo(3);
	}

	@Test
	public void getAffiliationCandidates_givenAffiliationWithCandidates_returnsAffiliationCandidates() throws Exception {
		Set<Candidate> candidates = new HashSet<>();
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.getCandidates()).thenReturn(candidates);
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setAffiliation(affiliation);
		assertThat(affiliationVoteCount.getAffiliationCandidates()).isSameAs(candidates);
	}

	@Test
	public void accept_givenVisitor_callsVisitOnVisitor() throws Exception {
		SettlementVisitor visitor = mock(SettlementVisitor.class);
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setPk(new Random().nextLong());
		affiliationVoteCount.accept(visitor);
		verify(visitor).visit(affiliationVoteCount);
	}
}

