package no.valg.eva.admin.settlement.domain.event.factory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateSeatEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class CandidateSeatEventsFromCandidateRanksTest {
	private static final BigDecimal FIRST_MODIFIED_SAINT_LAGUE_DIVISOR = new BigDecimal("1.4");

	@DataProvider
	public static Object[][] rankNumbers() {
		BigDecimal divisor1 = FIRST_MODIFIED_SAINT_LAGUE_DIVISOR;
		BigDecimal divisor2 = BigDecimal.valueOf(3);
		BigDecimal divisor3 = BigDecimal.valueOf(5);
		BigDecimal divisor4 = BigDecimal.valueOf(7);
		BigDecimal divisor5 = BigDecimal.valueOf(9);
		BigDecimal divisor6 = BigDecimal.valueOf(11);
		return new Object[][] {
				{ 1, divisor1 },
				{ 2, divisor2 },
				{ 3, divisor3 },
				{ 4, divisor4 },
				{ 5, divisor5 },
				{ 6, divisor6 },
		};
	}

	@Test(dataProvider = "rankNumbers")
	public void consume_givenCandidateRank_fireCandidateSeatEvent(int rankNumber, BigDecimal divisor) throws Exception {
		Affiliation affiliation = affiliation();
		Candidate candidate = candidate();
		CandidateRank candidateRank = mock(CandidateRank.class);
		when(candidateRank.getAffiliation()).thenReturn(affiliation);
		when(candidateRank.getCandidate()).thenReturn(candidate);
		when(candidateRank.getRankNumber()).thenReturn(rankNumber);
		CandidateSeatEventListener eventListener = mock(CandidateSeatEventListener.class);
		CandidateSeatEventsFromCandidateRanks candidateSeatEventsFromCandidateRanks = new CandidateSeatEventsFromCandidateRanks(
				FIRST_MODIFIED_SAINT_LAGUE_DIVISOR);
		candidateSeatEventsFromCandidateRanks.addEventListener(eventListener);
		candidateSeatEventsFromCandidateRanks.consume(candidateRank);
		verify(eventListener).candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate, divisor));
	}

	private Affiliation affiliation() {
		Affiliation affiliation = new Affiliation();
		Party party = mock(Party.class);
		when(party.getId()).thenReturn("P");
		affiliation.setParty(party);
		return affiliation;
	}

	private Candidate candidate() {
		Candidate candidate = new Candidate();
		candidate.setPk(1L);
		return candidate;
	}
}

