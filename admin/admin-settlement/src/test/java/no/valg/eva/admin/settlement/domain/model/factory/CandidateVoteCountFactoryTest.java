package no.valg.eva.admin.settlement.domain.model.factory;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.settlement.domain.consumer.CandidateVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class CandidateVoteCountFactoryTest {
	@DataProvider
	public static Object[][] differentCandidateVoteCountEvents() {
		Affiliation affiliation1 = mock(Affiliation.class);
		Affiliation affiliation2 = mock(Affiliation.class);
		Candidate candidate1 = mock(Candidate.class);
		Candidate candidate2 = mock(Candidate.class);
		VoteCategory voteCategory1 = mock(VoteCategory.class);
		VoteCategory voteCategory2 = mock(VoteCategory.class);
		int rankNumber1 = 1;
		int rankNumber2 = 2;
		return new Object[][] {
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation1, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation1, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation1, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation1, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation1, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation1, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation1, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation1, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation1, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation1, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation1, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation1, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation1, candidate2, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation1, candidate2, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory1, rankNumber2 },
				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation2, candidate1, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate1, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber1 },
				{ affiliation2, candidate1, voteCategory1, rankNumber2, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation2, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation2, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation2, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation2, candidate1, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate1, voteCategory2, rankNumber1, affiliation2, candidate1, voteCategory2, rankNumber2 },
				{ affiliation2, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation2, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation2, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation2, candidate1, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber1 },
				{ affiliation2, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation2, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation2, candidate1, voteCategory2, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory1, rankNumber2 },
				{ affiliation2, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation2, candidate2, voteCategory1, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate2, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber1 },
				{ affiliation2, candidate2, voteCategory1, rankNumber2, affiliation2, candidate2, voteCategory2, rankNumber2 },

				{ affiliation2, candidate2, voteCategory2, rankNumber1, affiliation2, candidate2, voteCategory2, rankNumber2 },
		};
	}

	@Test
	public void buildCandidateVoteCounts_givenCandidateVoteCountEvent_sendsCandidateVoteCountToConsumer() {
		CandidateVoteCountConsumer consumer = mock(CandidateVoteCountConsumer.class);
		CandidateVoteCountFactory candidateVoteCountFactory = new CandidateVoteCountFactory();
		candidateVoteCountFactory.addConsumer(consumer);
		CandidateVoteCountEvent event = mock(CandidateVoteCountEvent.class);

		candidateVoteCountFactory.candidateVoteCountDelta(event);
		candidateVoteCountFactory.buildCandidateVoteCounts();

		verify(consumer).consume(any());
	}

	@Test
	public void buildCandidateVoteCounts_givenTwoEqualCandidateVoteCountEvents_sendsOneCombinedCandidateVoteCountToConsumer() {
		CandidateVoteCountConsumer consumer = mock(CandidateVoteCountConsumer.class);
		CandidateVoteCountFactory candidateVoteCountFactory = new CandidateVoteCountFactory();
		candidateVoteCountFactory.addConsumer(consumer);
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate = mock(Candidate.class);
		VoteCategory voteCategory = mock(VoteCategory.class);

		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation, candidate, voteCategory, 1, TEN, TEN, ZERO));
		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation, candidate, voteCategory, 1, TEN, ZERO, TEN));
		candidateVoteCountFactory.buildCandidateVoteCounts();

		ArgumentCaptor<CandidateVoteCount> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCount.class);
		verify(consumer, times(1)).consume(argumentCaptor.capture());
		CandidateVoteCount candidateVoteCount = argumentCaptor.getValue();
		assertThat(candidateVoteCount.getVotes()).isEqualTo(TEN.add(TEN));
		assertThat(candidateVoteCount.getEarlyVotingVotes()).isEqualTo(TEN);
		assertThat(candidateVoteCount.getElectionDayVotes()).isEqualTo(TEN);
	}

	@Test(dataProvider = "differentCandidateVoteCountEvents")
	public void buildCandidateVoteCounts_givenTwoDifferentCandidateVoteCountEvents_sendsTwoCandidateVoteCountsToConsumer(
			Affiliation affiliation1, Candidate candidate1, VoteCategory voteCategory1, int rankNumber1,
			Affiliation affiliation2, Candidate candidate2, VoteCategory voteCategory2, int rankNumber2) {
		CandidateVoteCountConsumer consumer = mock(CandidateVoteCountConsumer.class);
		CandidateVoteCountFactory candidateVoteCountFactory = new CandidateVoteCountFactory();
		candidateVoteCountFactory.addConsumer(consumer);
		BigDecimal two = BigDecimal.valueOf(2);
		BigDecimal three = BigDecimal.valueOf(3);
		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation1, candidate1, voteCategory1, rankNumber1, ONE, two, three));
		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation2, candidate2, voteCategory2, rankNumber2, ONE, two, three));
		candidateVoteCountFactory.buildCandidateVoteCounts();
		ArgumentCaptor<CandidateVoteCount> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCount.class);
		verify(consumer, times(2)).consume(argumentCaptor.capture());
		List<CandidateVoteCount> allValues = argumentCaptor.getAllValues();
		assertVoteCountCount(allValues.get(0), affiliation1, candidate1, voteCategory1, rankNumber1, ONE, two, three);
		assertVoteCountCount(allValues.get(1), affiliation2, candidate2, voteCategory2, rankNumber2, ONE, two, three);
	}

	private void assertVoteCountCount(CandidateVoteCount candidateVoteCount, Affiliation affiliation, Candidate candidate, VoteCategory voteCategory,
			Integer rankNumber, BigDecimal votes, BigDecimal earlyVotingVotes, BigDecimal electionDayVotes) {
		assertThat(candidateVoteCount.getAffiliation()).isSameAs(affiliation);
		assertThat(candidateVoteCount.getCandidate()).isSameAs(candidate);
		assertThat(candidateVoteCount.getVoteCategory()).isSameAs(voteCategory);
		assertThat(candidateVoteCount.getRankNumber()).isEqualTo(rankNumber);
		assertThat(candidateVoteCount.getVotes()).isEqualTo(votes);
		assertThat(candidateVoteCount.getEarlyVotingVotes()).isEqualTo(earlyVotingVotes);
		assertThat(candidateVoteCount.getElectionDayVotes()).isEqualTo(electionDayVotes);
	}

	@Test
	public void buildCandidateVoteCounts_givenThreeDeltasForDifferentAffiliationsAndSameCandidate_sendsOneCountForEachAffiliationToConsumers() {
		Affiliation affiliation1 = mock(Affiliation.class);
		Affiliation affiliation2 = mock(Affiliation.class);
		Affiliation affiliation3 = mock(Affiliation.class);
		Candidate candidate = mock(Candidate.class);
		VoteCategory voteCategory = mock(VoteCategory.class);
		CandidateVoteCountConsumer consumer = mock(CandidateVoteCountConsumer.class);
		CandidateVoteCountFactory candidateVoteCountFactory = new CandidateVoteCountFactory();
		candidateVoteCountFactory.addConsumer(consumer);
		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation1, candidate, voteCategory, null, ONE, ONE, ZERO));
		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation2, candidate, voteCategory, null, ONE, ONE, ZERO));
		candidateVoteCountFactory.candidateVoteCountDelta(new CandidateVoteCountEvent(affiliation3, candidate, voteCategory, null, ONE, ONE, ZERO));
		candidateVoteCountFactory.buildCandidateVoteCounts();
		ArgumentCaptor<CandidateVoteCount> argumentCaptor = ArgumentCaptor.forClass(CandidateVoteCount.class);
		verify(consumer, times(3)).consume(argumentCaptor.capture());
		List<CandidateVoteCount> allValues = argumentCaptor.getAllValues();
		assertVoteCountCount(allValues.get(0), affiliation1, candidate, voteCategory, null, ONE, ONE, ZERO);
		assertVoteCountCount(allValues.get(1), affiliation2, candidate, voteCategory, null, ONE, ONE, ZERO);
		assertVoteCountCount(allValues.get(2), affiliation3, candidate, voteCategory, null, ONE, ONE, ZERO);
	}
}

