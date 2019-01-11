package no.valg.eva.admin.settlement.domain.event.factory;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.ADDED_VOTES;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.BALLOTS;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.BASELINE_VOTES;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.EARLY_VOTING_BALLOTS;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.EARLY_VOTING_MODIFIED_BALLOTS;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.ELECTION_DAY_BALLOTS;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.ELECTION_DAY_MODIFIED_BALLOTS;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.MODIFIED_BALLOTS;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.SUBTRACTED_VOTES;
import static no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModelTest.Value.VOTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.settlement.domain.event.AffiliationVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.AffiliationVoteCountEventListener;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class AffiliationVoteCountEventsFromCountingModelTest {
	public void assertEvent(AffiliationVoteCountEvent event, Affiliation affiliation, EnumMap<Value, Integer> valueMap) {
		assertThat(event.getAffiliation()).isSameAs(affiliation);
		assertThat(event.getBallots()).isEqualTo(valueMap.get(BALLOTS));
		assertThat(event.getModifiedBallots()).isEqualTo(valueMap.get(MODIFIED_BALLOTS));
		assertThat(event.getEarlyVotingBallots()).isEqualTo(valueMap.get(EARLY_VOTING_BALLOTS));
		assertThat(event.getEarlyVotingModifiedBallots()).isEqualTo(valueMap.get(EARLY_VOTING_MODIFIED_BALLOTS));
		assertThat(event.getElectionDayBallots()).isEqualTo(valueMap.get(ELECTION_DAY_BALLOTS));
		assertThat(event.getElectionDayModifiedBallots()).isEqualTo(valueMap.get(ELECTION_DAY_MODIFIED_BALLOTS));
		assertThat(event.getBaselineVotes()).isEqualTo(valueMap.get(BASELINE_VOTES));
		assertThat(event.getAddedVotes()).isEqualTo(valueMap.get(ADDED_VOTES));
		assertThat(event.getSubtractedVotes()).isEqualTo(valueMap.get(SUBTRACTED_VOTES));
	}

	private EnumMap<Value, Integer> affiliationVoteCountValues(
			int ballots, int modifiedBallots, int earlyVotingBallots, int earlyVotingModifiedBallots, int electionDayBallots, int electionDayModifiedBallots,
			int baselineVotes, int addedVotes, int subtractedVotes, int votes) {
		EnumMap<Value, Integer> valueMap = new EnumMap<>(Value.class);
		valueMap.put(BALLOTS, ballots);
		valueMap.put(MODIFIED_BALLOTS, modifiedBallots);
		valueMap.put(EARLY_VOTING_BALLOTS, earlyVotingBallots);
		valueMap.put(EARLY_VOTING_MODIFIED_BALLOTS, earlyVotingModifiedBallots);
		valueMap.put(ELECTION_DAY_BALLOTS, electionDayBallots);
		valueMap.put(ELECTION_DAY_MODIFIED_BALLOTS, electionDayModifiedBallots);
		valueMap.put(BASELINE_VOTES, baselineVotes);
		valueMap.put(ADDED_VOTES, addedVotes);
		valueMap.put(SUBTRACTED_VOTES, subtractedVotes);
		valueMap.put(VOTES, votes);
		return valueMap;
	}

	@Test
	public void visit_givenEarlyVotingBallotCount_firesAffiliationVoteCountEvent() throws Exception {
		BallotCount ballotCount = ballotCount(true, 50, 50);
		AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel = new AffiliationVoteCountEventsFromCountingModel(20);
		AffiliationVoteCountEventListener listener = mock(AffiliationVoteCountEventListener.class);
		affiliationVoteCountEventsFromCountingModel.addEventListener(listener);

		affiliationVoteCountEventsFromCountingModel.visit(ballotCount);

		ArgumentCaptor<AffiliationVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(AffiliationVoteCountEvent.class);
		verify(listener).affiliationVoteCountDelta(argumentCaptor.capture());
		EnumMap<Value, Integer> affiliationVoteCountValues = affiliationVoteCountValues(100, 50, 100, 50, 0, 0, 2000, 0, 0, 2000);
		assertEvent(argumentCaptor.getValue(), ballotCount.getBallotAffiliation(), affiliationVoteCountValues);
	}

	@Test
	public void visit_givenElectionDayBallotCount_firesAffiliationVoteCountEvent() throws Exception {
		BallotCount ballotCount = ballotCount(false, 50, 50);
		AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel = new AffiliationVoteCountEventsFromCountingModel(20);
		AffiliationVoteCountEventListener listener = mock(AffiliationVoteCountEventListener.class);
		affiliationVoteCountEventsFromCountingModel.addEventListener(listener);

		affiliationVoteCountEventsFromCountingModel.visit(ballotCount);

		ArgumentCaptor<AffiliationVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(AffiliationVoteCountEvent.class);
		verify(listener).affiliationVoteCountDelta(argumentCaptor.capture());
		EnumMap<Value, Integer> affiliationVoteCountValues = affiliationVoteCountValues(100, 50, 0, 0, 100, 50, 2000, 0, 0, 2000);
		assertEvent(argumentCaptor.getValue(), ballotCount.getBallotAffiliation(), affiliationVoteCountValues);
	}

	@Test
	public void visit_givenCandidateVote_firesAffiliationVoteCountEvent() throws Exception {
		AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel = new AffiliationVoteCountEventsFromCountingModel(1);
		AffiliationVoteCountEventListener listener = mock(AffiliationVoteCountEventListener.class);
		affiliationVoteCountEventsFromCountingModel.addEventListener(listener);
		CandidateVote candidateVote = mock(CandidateVote.class, RETURNS_DEEP_STUBS);
		Affiliation candidateAffiliation = affiliation("C");
		when(candidateVote.getCandidateAffiliation()).thenReturn(candidateAffiliation);
		Affiliation ballotAffiliation = affiliation("B");
		when(candidateVote.getBallotAffiliation()).thenReturn(ballotAffiliation);

		affiliationVoteCountEventsFromCountingModel.visit(candidateVote);

		ArgumentCaptor<AffiliationVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(AffiliationVoteCountEvent.class);
		verify(listener, times(2)).affiliationVoteCountDelta(argumentCaptor.capture());
		List<AffiliationVoteCountEvent> values = argumentCaptor.getAllValues();
		assertEvent(values.get(0), candidateAffiliation, affiliationVoteCountValues(0, 0, 0, 0, 0, 0, 0, 1, 0, 1));
		assertEvent(values.get(1), ballotAffiliation, affiliationVoteCountValues(0, 0, 0, 0, 0, 0, 0, 0, 1, -1));
	}

	private Affiliation affiliation(String partyId) {
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(new Random().nextLong());
		Party party = new Party();
		party.setId(partyId);
		affiliation.setParty(party);
		return affiliation;
	}

	private BallotCount ballotCount(boolean earlyVoting, int unmodifiedBallots, int modifiedBallots) {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.isEarlyVoting()).thenReturn(earlyVoting);
		when(ballotCount.getBallots()).thenReturn(unmodifiedBallots + modifiedBallots);
		when(ballotCount.getModifiedBallots()).thenReturn(modifiedBallots);
		return ballotCount;
	}

	@Test
	public void include_givenContestReport_returnsTrue() throws Exception {
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(new ContestReport())).isTrue();
	}

	@Test
	public void include_givenFinalVoteCountToSettlement_returnsTrue() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.isFinalCount()).thenReturn(true);
		when(voteCount.isToSettlement()).thenReturn(true);
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(voteCount)).isTrue();
	}

	@Test
	public void include_givenNotFinalVoteCount_returnsFalse() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.getCountQualifierId()).thenReturn(PRELIMINARY.getId());
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(voteCount)).isFalse();
	}

	@Test
	public void include_givenFinalVoteCountNotToSettlement_returnsFalse() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.getCountQualifierId()).thenReturn(FINAL.getId());
		when(voteCount.getCountStatus()).thenReturn(APPROVED);
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(voteCount)).isFalse();
	}

	@Test
	public void include_givenBallotCountWithBlankBallot_returnsTrue() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallotId()).thenReturn(EvoteConstants.BALLOT_BLANK);
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(ballotCount)).isTrue();
	}

	@Test
	public void include_givenBallotCountWithOrdinaryBallot_returnsTrue() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallotId()).thenReturn("ORDINARY");
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(ballotCount)).isTrue();
	}

	@Test
	public void include_givenBallotCountWithBallotRejection_returnsFalse() throws Exception {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallot()).thenReturn(null);
		when(ballotCount.getBallotRejection()).thenReturn(new BallotRejection());
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(ballotCount)).isFalse();
	}

	@Test
	public void include_givenCastBallot_returnsTrue() throws Exception {
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(new CastBallot())).isTrue();
	}

	@Test
	public void include_givenWriteInCandidateVote_returnsTrue() throws Exception {
		CandidateVote candidateVote = mock(CandidateVote.class);
		when(candidateVote.isWriteIn()).thenReturn(true);
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(candidateVote)).isTrue();
	}

	@Test
	public void include_givenNotWriteInCandidateVote_returnsFalse() throws Exception {
		CandidateVote candidateVote = mock(CandidateVote.class);
		when(candidateVote.isWriteIn()).thenReturn(false);
		assertThat(new AffiliationVoteCountEventsFromCountingModel(1).include(candidateVote)).isFalse();
	}

	public enum Value {
		BALLOTS, MODIFIED_BALLOTS, EARLY_VOTING_BALLOTS, EARLY_VOTING_MODIFIED_BALLOTS, ELECTION_DAY_BALLOTS, ELECTION_DAY_MODIFIED_BALLOTS,
		BASELINE_VOTES, ADDED_VOTES, SUBTRACTED_VOTES, VOTES
	}
}

