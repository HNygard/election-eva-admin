package no.valg.eva.admin.settlement.domain.model.factory;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.common.Randomizer;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.consumer.CandidateSeatConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CandidateSeatFactoryTest {
	@Test
	public void buildCandidateSeats_givenDividendEvent_producesCandidateSeat() throws Exception {
		CandidateSeatConsumer consumer = mock(CandidateSeatConsumer.class);
		CandidateSeatFactory candidateSeatFactory = candidateSeatFactory(consumer);
		int dividend = 1;
		CandidateSeatEvent event = candidateSeatEvent(dividend);

		candidateSeatFactory.candidateSeatDelta(event);
		candidateSeatFactory.buildCandidateSeats();

		ArgumentCaptor<CandidateSeat> argumentCaptor = ArgumentCaptor.forClass(CandidateSeat.class);
		verify(consumer).consume(argumentCaptor.capture());
		assertCandidateSeat(argumentCaptor.getValue(), event.getCandidate(), event.getAffiliation(), 1, null, dividend, null, true);
	}

	@Test
	public void buildCandidateSeats_givenDivisorEvent_producesCandidateSeat() throws Exception {
		CandidateSeatConsumer consumer = mock(CandidateSeatConsumer.class);
		CandidateSeatFactory candidateSeatFactory = candidateSeatFactory(consumer);
		BigDecimal divisor = ONE;
		CandidateSeatEvent event = candidateSeatEvent(divisor);

		candidateSeatFactory.candidateSeatDelta(event);
		candidateSeatFactory.buildCandidateSeats();

		ArgumentCaptor<CandidateSeat> argumentCaptor = ArgumentCaptor.forClass(CandidateSeat.class);
		verify(consumer).consume(argumentCaptor.capture());
		BigDecimal quotient = new BigDecimal("0.000000");
		assertCandidateSeat(argumentCaptor.getValue(), event.getCandidate(), event.getAffiliation(), 1, quotient, 0, divisor, true);
	}

	@Test
	public void buildCandidateSeats_givenDividendEventAndDivisorEvent_producesOneCandidateSeat() throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate = mock(Candidate.class);
		CandidateSeatConsumer consumer = mock(CandidateSeatConsumer.class);
		CandidateSeatFactory candidateSeatFactory = candidateSeatFactory(consumer);
		int dividend = 1;
		BigDecimal divisor = ONE;

		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate, dividend));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate, divisor));
		candidateSeatFactory.buildCandidateSeats();

		ArgumentCaptor<CandidateSeat> argumentCaptor = ArgumentCaptor.forClass(CandidateSeat.class);
		verify(consumer).consume(argumentCaptor.capture());
		BigDecimal quotient = new BigDecimal("1.000000");
		assertCandidateSeat(argumentCaptor.getValue(), candidate, affiliation, 1, quotient, dividend, divisor, true);
	}

	@Test
	public void buildCandidateSeats_givenTwoDividendEventsAndTwoDivisorEventForTwoCandidates_producesTwoCandidateSeatOrderedByDescendingQuotient()
			throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate1 = candidate(1);
		Candidate candidate2 = candidate(2);
		CandidateSeatConsumer consumer = mock(CandidateSeatConsumer.class);
		CandidateSeatFactory candidateSeatFactory = candidateSeatFactory(consumer);
		int dividend1 = 1;
		int dividend2 = 2;
		BigDecimal divisor = ONE;

		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate1, dividend1));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate1, divisor));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate2, dividend2));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate2, divisor));
		candidateSeatFactory.buildCandidateSeats();

		ArgumentCaptor<CandidateSeat> argumentCaptor = ArgumentCaptor.forClass(CandidateSeat.class);
		verify(consumer, times(2)).consume(argumentCaptor.capture());
		List<CandidateSeat> allValues = argumentCaptor.getAllValues();
		BigDecimal quotient1 = new BigDecimal("1.000000");
		BigDecimal quotient2 = new BigDecimal("2.000000");
		assertCandidateSeat(allValues.get(0), candidate2, affiliation, 1, quotient2, dividend2, divisor, true);
		assertCandidateSeat(allValues.get(1), candidate1, affiliation, 2, quotient1, dividend1, divisor, true);
	}

	@Test
	public void buildCandidateSeats_givenFourEventsForTwoCandidatesWithDifferentDividendsAndDivisorsButEqualQuotients_producesCandidateSeatOrderedByDescendingDividend()
			throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate1 = candidate(1);
		Candidate candidate2 = candidate(2);
		CandidateSeatConsumer consumer = mock(CandidateSeatConsumer.class);
		CandidateSeatFactory candidateSeatFactory = candidateSeatFactory(consumer);
		
		int dividend1 = 35;
		int dividend2 = 315;
		
		BigDecimal divisor1 = new BigDecimal("3.0");
		BigDecimal divisor2 = new BigDecimal("27.0");

		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate1, dividend1));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate1, divisor1));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate2, dividend2));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate2, divisor2));
		candidateSeatFactory.buildCandidateSeats();

		ArgumentCaptor<CandidateSeat> argumentCaptor = ArgumentCaptor.forClass(CandidateSeat.class);
		verify(consumer, times(2)).consume(argumentCaptor.capture());
		List<CandidateSeat> allValues = argumentCaptor.getAllValues();
		BigDecimal quotient = new BigDecimal("11.666667");
		assertCandidateSeat(allValues.get(0), candidate2, affiliation, 1, quotient, dividend2, divisor2, true);
		assertCandidateSeat(allValues.get(1), candidate1, affiliation, 2, quotient, dividend1, divisor1, true);
	}

	@Test(dataProvider = "randomizerTestData")
	public void buildCandidateSeats_givenFourEventsForTwoCandidatesWithEqualDividendsDivisorsAndQuotients_producesCandidateSeatRandomlyOrdered(
			int firstInt, int secondInt, int index0, int index1) throws Exception {
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate1 = candidate(1);
		Candidate candidate2 = candidate(2);
		CandidateSeatConsumer consumer = mock(CandidateSeatConsumer.class);
		CandidateSeatFactory candidateSeatFactory = candidateSeatFactory(consumer);
		Randomizer randomizer = mock(Randomizer.class);
		when(randomizer.nextInt()).thenReturn(firstInt, secondInt);
		setPrivateField(candidateSeatFactory, "randomizer", randomizer);
		
		int dividend = 35;
		
		BigDecimal divisor = new BigDecimal("3.0");

		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate1, dividend));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate1, divisor));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate2, dividend));
		candidateSeatFactory.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate2, divisor));
		candidateSeatFactory.buildCandidateSeats();

		ArgumentCaptor<CandidateSeat> argumentCaptor = ArgumentCaptor.forClass(CandidateSeat.class);
		verify(consumer, times(2)).consume(argumentCaptor.capture());
		List<CandidateSeat> allValues = argumentCaptor.getAllValues();
		BigDecimal quotient = new BigDecimal("11.666667");
		assertCandidateSeat(allValues.get(index0), candidate1, affiliation, index0 + 1, quotient, dividend, divisor, true);
		assertCandidateSeat(allValues.get(index1), candidate2, affiliation, index1 + 1, quotient, dividend, divisor, true);
	}

	@DataProvider
	public Object[][] randomizerTestData() {
		return new Object[][] {
				{ 1, 2, 1, 0 },
				{ 2, 1, 0, 1 }
		};
	}

	private CandidateSeatFactory candidateSeatFactory(CandidateSeatConsumer consumer) {
		
		int numberOfPositionsInContest = 5;
		
		CandidateSeatFactory candidateSeatFactory = new CandidateSeatFactory(numberOfPositionsInContest);
		candidateSeatFactory.addConsumer(consumer);
		return candidateSeatFactory;
	}

	private CandidateSeatEvent candidateSeatEvent(int dividend) {
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate = mock(Candidate.class);
		return new CandidateSeatEvent(affiliation, candidate, dividend);
	}

	private CandidateSeatEvent candidateSeatEvent(BigDecimal divisor) {
		Affiliation affiliation = mock(Affiliation.class);
		Candidate candidate = mock(Candidate.class);
		return new CandidateSeatEvent(affiliation, candidate, divisor);
	}

	private void setPrivateField(CandidateSeatFactory candidateSeatFactory, String fieldName, Randomizer randomizer)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = CandidateSeatFactory.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(candidateSeatFactory, randomizer);
	}

	private Candidate candidate(long pk) {
		Candidate candidate = new Candidate();
		candidate.setPk(pk);
		candidate.setFirstName(String.valueOf(pk));
		return candidate;
	}

	private void assertCandidateSeat(CandidateSeat candidateSeat, Candidate candidate, Affiliation affiliation, int seatNumber, BigDecimal quotient,
			int dividend, BigDecimal divisor, boolean elected) {
		assertThat(candidateSeat.getCandidate()).isSameAs(candidate);
		assertThat(candidateSeat.getAffiliation()).isSameAs(affiliation);
		assertThat(candidateSeat.getSeatNumber()).isEqualTo(seatNumber);
		assertThat(candidateSeat.getQuotient()).isEqualTo(quotient);
		assertThat(candidateSeat.getDividend()).isEqualTo(dividend);
		assertThat(candidateSeat.getDivisor()).isEqualTo(divisor);
		assertThat(candidateSeat.isElected()).isEqualTo(elected);
	}
}
