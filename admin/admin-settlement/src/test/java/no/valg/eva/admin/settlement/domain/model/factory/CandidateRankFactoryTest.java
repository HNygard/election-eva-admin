package no.valg.eva.admin.settlement.domain.model.factory;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.consumer.CandidateRankConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class CandidateRankFactoryTest {
	@Test
	public void buildCandidateRanks_givenCandidateRankEvent_sendsCandidateRankToConsumer() throws Exception {
		CandidateRankConsumer consumer = mock(CandidateRankConsumer.class);
		CandidateRankFactory candidateRankFactory = new CandidateRankFactory(this::processCandidateRanks);
		candidateRankFactory.addConsumer(consumer);
		Candidate candidate = mock(Candidate.class);
		Affiliation affiliation = mock(Affiliation.class);
		CandidateRankEvent event = new CandidateRankEvent(candidate, affiliation, TEN, null);

		candidateRankFactory.candidateRankDelta(event);
		candidateRankFactory.buildCandidateRanks();

		ArgumentCaptor<CandidateRank> argumentCaptor = ArgumentCaptor.forClass(CandidateRank.class);
		verify(consumer).consume(argumentCaptor.capture());
		CandidateRank candidateRank = argumentCaptor.getValue();
		assertThat(candidateRank.getCandidate()).isEqualTo(candidate);
		assertThat(candidateRank.getAffiliation()).isEqualTo(affiliation);
		assertThat(candidateRank.getVotes()).isEqualTo(TEN);
	}

	@Test
	public void buildCandidateRanks_givenTwoEqualCandidateRankEvents_sendsOneCombinedCandidateRankToConsumer() throws Exception {
		CandidateRankConsumer consumer = mock(CandidateRankConsumer.class);
		CandidateRankFactory candidateRankFactory = new CandidateRankFactory(this::processCandidateRanks);
		candidateRankFactory.addConsumer(consumer);
		Candidate candidate = mock(Candidate.class);
		Affiliation affiliation = mock(Affiliation.class);
		CandidateRankEvent event1 = new CandidateRankEvent(candidate, affiliation, TEN, null);
		CandidateRankEvent event2 = new CandidateRankEvent(candidate, affiliation, TEN, null);

		candidateRankFactory.candidateRankDelta(event1);
		candidateRankFactory.candidateRankDelta(event2);
		candidateRankFactory.buildCandidateRanks();

		ArgumentCaptor<CandidateRank> argumentCaptor = ArgumentCaptor.forClass(CandidateRank.class);
		verify(consumer).consume(argumentCaptor.capture());
		CandidateRank candidateRank = argumentCaptor.getValue();
		assertThat(candidateRank.getVotes()).isEqualTo(TEN.add(TEN));
	}

	private List<CandidateRank> processCandidateRanks(Collection<CandidateRank> candidateRanks) {
		return new ArrayList<>(candidateRanks);
	}
}

