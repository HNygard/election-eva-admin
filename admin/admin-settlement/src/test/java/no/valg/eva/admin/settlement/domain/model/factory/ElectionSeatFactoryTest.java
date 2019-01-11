package no.valg.eva.admin.settlement.domain.model.factory;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_UP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.ElectionSeatConsumer;
import no.valg.eva.admin.settlement.domain.model.ElectionSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class ElectionSeatFactoryTest extends MockUtilsTestCase {
	@Test
	public void consume_givenLevelingSeatSummaryWithMoreContestSeatsThanElectionSeats_isMoreElectionSeatsTrue() throws Exception {
		Party party = createMock(Party.class);
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(ONE, 1);
		electionSeatFactory.consume(electionVoteCount(party));
		assertThat(electionSeatFactory.isMoreElectionSeatsNeeded()).isFalse();
		electionSeatFactory.consume(levelingSeatSummary(party, true));
		assertThat(electionSeatFactory.isMoreElectionSeatsNeeded()).isTrue();
	}

	@Test
	public void consume_givenLevelingSeatSummaryWithNotMoreContestSeatsThanElectionSeats_isMoreElectionSeatsFalse() throws Exception {
		Party party = createMock(Party.class);
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(ONE, 1);
		electionSeatFactory.consume(electionVoteCount(party));
		assertThat(electionSeatFactory.isMoreElectionSeatsNeeded()).isFalse();
		electionSeatFactory.consume(levelingSeatSummary(party, false));
		assertThat(electionSeatFactory.isMoreElectionSeatsNeeded()).isFalse();
	}

	@Test
	public void isMoreElectionSeatsNeeded_whenTrue_isFalseAfterBuildElectionSeats() throws Exception {
		Party party = createMock(Party.class);
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(ONE, 1);
		electionSeatFactory.consume(electionVoteCount(party));
		electionSeatFactory.consume(levelingSeatSummary(party, true));
		assertThat(electionSeatFactory.isMoreElectionSeatsNeeded()).isTrue();
		electionSeatFactory.buildElectionSeats();
		assertThat(electionSeatFactory.isMoreElectionSeatsNeeded()).isFalse();
	}

	@Test
	public void generateSeats_givenElectionVoteCounts_createCorrectElectionSeats() throws Exception {
		ElectionSeatConsumer consumer = createMock(ElectionSeatConsumer.class);
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(ONE, 1);
		electionSeatFactory.addConsumer(consumer);
		Party party1 = createMock(Party.class);
		Party party2 = createMock(Party.class);
		int votes1 = 181;
		int votes2 = 211;
		BigDecimal divisor1 = ONE;
		BigDecimal divisor2 = BigDecimal.valueOf(3);
		BigDecimal divisor3 = BigDecimal.valueOf(5);
		boolean elected = true;
		boolean notElected = !elected;
		boolean notSameQuotientAsNext = false;
		boolean notSameVotesAsNext = false;

		when(party1.toString()).thenReturn("party1");
		when(party2.toString()).thenReturn("party2");

		electionSeatFactory.consume(electionVoteCount(party1, votes1));
		electionSeatFactory.consume(electionVoteCount());
		electionSeatFactory.consume(electionVoteCount(party2, votes2));
		electionSeatFactory.buildElectionSeats();

		ArgumentCaptor<ElectionSeat> argumentCaptor = ArgumentCaptor.forClass(ElectionSeat.class);
		verify(consumer, times(6)).consume(argumentCaptor.capture());
		List<ElectionSeat> electionSeats = argumentCaptor.getAllValues();
		assertElectionSeat(electionSeats.get(0), 1, party2, votes2, divisor1, elected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 211
		assertElectionSeat(electionSeats.get(1), 2, party1, votes1, divisor1, elected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 181
		assertElectionSeat(electionSeats.get(2), 3, party2, votes2, divisor2, elected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 70.33...
		assertElectionSeat(electionSeats.get(3), 4, party1, votes1, divisor2, notElected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 60.433...
		assertElectionSeat(electionSeats.get(4), 5, party2, votes2, divisor3, notElected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 42.2
		assertElectionSeat(electionSeats.get(5), 6, party1, votes1, divisor3, notElected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 36.2
	}

	@Test
	public void generateSeats_givenElectionVoteCountsWhichGivesEqualQuotients_createCorrectElectionSeats() throws Exception {
		ElectionSeatConsumer consumer = createMock(ElectionSeatConsumer.class);
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(ONE, 1);
		electionSeatFactory.addConsumer(consumer);
		Party party1 = createMock(Party.class);
		Party party2 = createMock(Party.class);
		int votes1 = 5;
		int votes2 = 15;
		BigDecimal divisor1 = ONE;
		BigDecimal divisor2 = BigDecimal.valueOf(3);
		BigDecimal divisor3 = BigDecimal.valueOf(5);
		boolean elected = true;
		boolean notElected = !elected;
		boolean sameQuotientAsNext = true;
		boolean notSameQuotientAsNext = !sameQuotientAsNext;
		boolean sameVotesAsNext = true;
		boolean notSameVotesAsNext = !sameVotesAsNext;

		when(party1.toString()).thenReturn("party1");
		when(party2.toString()).thenReturn("party2");

		electionSeatFactory.consume(electionVoteCount(party1, votes1));
		electionSeatFactory.consume(electionVoteCount());
		electionSeatFactory.consume(electionVoteCount(party2, votes2));
		electionSeatFactory.buildElectionSeats();

		ArgumentCaptor<ElectionSeat> argumentCaptor = ArgumentCaptor.forClass(ElectionSeat.class);
		verify(consumer, times(6)).consume(argumentCaptor.capture());
		List<ElectionSeat> electionSeats = argumentCaptor.getAllValues();
		assertElectionSeat(electionSeats.get(0), 1, party2, votes2, divisor1, elected, notSameQuotientAsNext, sameVotesAsNext); // quotient: 15
		assertElectionSeat(electionSeats.get(1), 2, party2, votes2, divisor2, elected, sameQuotientAsNext, notSameVotesAsNext); // quotient: 5; votes: 15
		assertElectionSeat(electionSeats.get(2), 3, party1, votes1, divisor1, elected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 5; votes: 5
		assertElectionSeat(electionSeats.get(3), 4, party2, votes2, divisor3, notElected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 3
		assertElectionSeat(electionSeats.get(4), 5, party1, votes1, divisor2, notElected, notSameQuotientAsNext, sameVotesAsNext); // quotient: 1.66...
		assertElectionSeat(electionSeats.get(5), 6, party1, votes1, divisor3, notElected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 1
	}

	@Test
	public void generateSeats_givenElectionVoteCountsWithEqualVotes_createCorrectElectionSeats() throws Exception {
		ElectionSeatConsumer consumer = createMock(ElectionSeatConsumer.class);
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(ONE, 1);
		electionSeatFactory.addConsumer(consumer);
		Party party1 = createMock(Party.class);
		Party party2 = createMock(Party.class);
		int votes1 = 5;
		int votes2 = 5;
		BigDecimal divisor1 = ONE;
		BigDecimal divisor2 = BigDecimal.valueOf(3);
		BigDecimal divisor3 = BigDecimal.valueOf(5);
		boolean elected = true;
		boolean notElected = !elected;
		boolean sameQuotientAsNext = true;
		boolean notSameQuotientAsNext = !sameQuotientAsNext;
		boolean sameVotesAsNext = true;
		boolean notSameVotesAsNext = !sameVotesAsNext;

		when(party1.toString()).thenReturn("party1");
		when(party2.toString()).thenReturn("party2");

		electionSeatFactory.consume(electionVoteCount(party1, votes1));
		electionSeatFactory.consume(electionVoteCount());
		electionSeatFactory.consume(electionVoteCount(party2, votes2));
		electionSeatFactory.buildElectionSeats();

		ArgumentCaptor<ElectionSeat> argumentCaptor = ArgumentCaptor.forClass(ElectionSeat.class);
		verify(consumer, times(6)).consume(argumentCaptor.capture());
		List<ElectionSeat> electionSeats = argumentCaptor.getAllValues();
		assertElectionSeat(electionSeats.get(0), 1, party1, votes1, divisor1, elected, sameQuotientAsNext, sameVotesAsNext); // quotient: 5
		assertElectionSeat(electionSeats.get(1), 2, party2, votes2, divisor1, elected, notSameQuotientAsNext, sameVotesAsNext); // quotient: 5
		assertElectionSeat(electionSeats.get(2), 3, party1, votes1, divisor2, elected, sameQuotientAsNext, sameVotesAsNext); // quotient: 1.66...
		assertElectionSeat(electionSeats.get(3), 4, party2, votes2, divisor2, notElected, notSameQuotientAsNext, sameVotesAsNext); // quotient: 1.66...
		assertElectionSeat(electionSeats.get(4), 5, party1, votes1, divisor3, notElected, sameQuotientAsNext, sameVotesAsNext); // quotient: 1
		assertElectionSeat(electionSeats.get(5), 6, party2, votes2, divisor3, notElected, notSameQuotientAsNext, notSameVotesAsNext); // quotient: 1
	}

	private void assertElectionSeat(ElectionSeat electionSeat, int seatNumber, Party party, int dividend, BigDecimal divisor, boolean elected,
			boolean sameQuotientAsNext, boolean sameVotesAsNext) {
		BigDecimal quotient = quotient(dividend, divisor);
		assertThat(electionSeat.getSeatNumber()).isEqualTo(seatNumber);
		assertThat(electionSeat.getParty()).isEqualTo(party);
		assertThat(electionSeat.getQuotient()).isEqualTo(quotient);
		assertThat(electionSeat.getDividend()).isEqualTo(dividend);
		assertThat(electionSeat.getDivisor()).isEqualTo(divisor);
		assertThat(electionSeat.isElected()).isEqualTo(elected);
		assertThat(electionSeat.isSameQuotientAsNext()).isEqualTo(sameQuotientAsNext);
		assertThat(electionSeat.isSameVotesAsNext()).isEqualTo(sameVotesAsNext);
	}

	private BigDecimal quotient(int votes, BigDecimal divisor) {
		return BigDecimal.valueOf(votes).divide(divisor, 14, HALF_UP);
	}

	private ElectionVoteCount electionVoteCount() {
		return electionVoteCount(false);
	}

	private ElectionVoteCount electionVoteCount(Party party) {
		ElectionVoteCount electionVoteCount = electionVoteCount(true);
		when(electionVoteCount.getParty()).thenReturn(party);
		return electionVoteCount;
	}

	private ElectionVoteCount electionVoteCount(Party party, int votes) {
		ElectionVoteCount electionVoteCount = electionVoteCount(true);
		when(electionVoteCount.getParty()).thenReturn(party);
		when(electionVoteCount.getVotes()).thenReturn(votes);
		return electionVoteCount;
	}

	private ElectionVoteCount electionVoteCount(boolean eligibleForLevelingSeats) {
		ElectionVoteCount electionVoteCount = createMock(ElectionVoteCount.class);
		when(electionVoteCount.getContestSeats()).thenReturn(1);
		when(electionVoteCount.isEligibleForLevelingSeats()).thenReturn(eligibleForLevelingSeats);
		return electionVoteCount;
	}

	private LevelingSeatSummary levelingSeatSummary(Party party, boolean hasMoreContestSeatsThanElectionSeats) {
		LevelingSeatSummary levelingSeatSummary = createMock(LevelingSeatSummary.class);
		when(levelingSeatSummary.getParty()).thenReturn(party);
		when(levelingSeatSummary.hasMoreContestSeatsThanElectionSeats()).thenReturn(hasMoreContestSeatsThanElectionSeats);
		return levelingSeatSummary;
	}
}

