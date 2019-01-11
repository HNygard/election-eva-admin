package no.valg.eva.admin.settlement.domain.model.factory;

import static com.codepoetics.protonpack.StreamUtils.zip;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatConsumer;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionSettlement;
import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class LevelingSeatFactoryTest extends MockUtilsTestCase {
	private static final Contest CONTEST_1 = mock(Contest.class);
	private static final Contest CONTEST_2 = mock(Contest.class);
	private static final Party PARTY_1 = mock(Party.class);
	private static final Party PARTY_2 = mock(Party.class);
	private static final boolean SAME_QUOTIENT_AS_NEXT = true;
	private static final boolean NOT_SAME_QUOTIENT_AS_NEXT = !SAME_QUOTIENT_AS_NEXT;
	private static final boolean SAME_VOTES_AS_NEXT = true;
	private static final boolean NOT_SAME_VOTES_AS_NEXT = !SAME_VOTES_AS_NEXT;
	private static final boolean CONTEST_LEVELED = true;
	private static final boolean NOT_CONTEST_LEVELED = !CONTEST_LEVELED;
	private static final boolean PARTY_LEVELED = true;
	private static final boolean NOT_PARTY_LEVELED = !PARTY_LEVELED;

	@Test(dataProvider = "buildLevelingSeatsTestData")
	public void buildLevelingSeats_givenTestData_buildsLevelingSeats(
			String testName, List<CandidateSeat> candidateSeats, List<LevelingSeatQuotient> levelingSeatQuotients,
			List<ElectionSettlement> electionSettlements, List<LevelingSeat> expectedLevelingSeats) throws Exception {
		LevelingSeatConsumer consumer = createMock(LevelingSeatConsumer.class);
		LevelingSeatFactory levelingSeatFactory = levelingSeatFactory(consumer);

		candidateSeats.forEach(levelingSeatFactory::visit);
		electionSettlements.forEach(levelingSeatFactory::consume);
		levelingSeatQuotients.forEach(levelingSeatFactory::consume);

		levelingSeatFactory.buildLevelingSeats();

		ArgumentCaptor<LevelingSeat> argumentCaptor = ArgumentCaptor.forClass(LevelingSeat.class);
		verify(consumer, times(expectedLevelingSeats.size())).consume(argumentCaptor.capture());
		assertLevelingSeats(argumentCaptor.getAllValues(), expectedLevelingSeats);
	}

	private void assertLevelingSeats(List<LevelingSeat> levelingSeats, List<LevelingSeat> expectedLevelingSeats) {
		zip(levelingSeats.stream(), expectedLevelingSeats.stream(), AssertLevelingSeat::new).forEach(AssertLevelingSeat::doAssert);
	}

	private LevelingSeatFactory levelingSeatFactory(LevelingSeatConsumer consumer) {
		LevelingSeatFactory levelingSeatFactory = new LevelingSeatFactory();
		levelingSeatFactory.addConsumer(consumer);
		return levelingSeatFactory;
	}

	@DataProvider
	public Object[][] buildLevelingSeatsTestData() {
		return new Object[][] {
				baseTest(), equalParties(), equalContests(), equalQuotients(), equalVotes(), electedCandidate(),
				partyWithZeroLevelingSeatsOrderedFirst(), partyWithZeroLevelingSeatsOrderedLast()
		};
	}

	private Object[] baseTest() {
		CandidateSeat candidateSeat1 = candidateSeat(CONTEST_1, PARTY_1, false);
		CandidateSeat candidateSeat2 = candidateSeat(CONTEST_2, PARTY_2, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_1, PARTY_2, new BigDecimal("25"), 250);
		LevelingSeatQuotient levelingSeatQuotient3 = levelingSeatQuotient(CONTEST_2, PARTY_1, new BigDecimal("20"), 200);
		LevelingSeatQuotient levelingSeatQuotient4 = levelingSeatQuotient(CONTEST_2, PARTY_2, TEN, 100);
		return new Object[] {
				"baseTest",
				asList(candidateSeat1, candidateSeat2),
				asList(levelingSeatQuotient1, levelingSeatQuotient2, levelingSeatQuotient3, levelingSeatQuotient4),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 1), levelingSeatSummary(PARTY_2, 1))),
				asList(levelingSeat(levelingSeatQuotient1, 1, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
						candidateSeat1),
						levelingSeat(levelingSeatQuotient2, 2, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, CONTEST_LEVELED, NOT_PARTY_LEVELED, null,
								null),
						levelingSeat(levelingSeatQuotient3, 3, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, PARTY_LEVELED, null,
								null),
						levelingSeat(levelingSeatQuotient4, 4, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 2,
								candidateSeat2))
		};
	}

	private Object[] equalContests() {
		CandidateSeat candidateSeat1 = candidateSeat(CONTEST_1, PARTY_1, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_1, PARTY_2, new BigDecimal("20"), 200);
		return new Object[] {
				"equalContests",
				singletonList(candidateSeat1),
				asList(levelingSeatQuotient1, levelingSeatQuotient2),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 1), levelingSeatSummary(PARTY_2, 1))),
				asList(levelingSeat(levelingSeatQuotient1, 1, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
						candidateSeat1),
						levelingSeat(levelingSeatQuotient2, 2, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, CONTEST_LEVELED, NOT_PARTY_LEVELED, null,
								null))
		};
	}

	private Object[] equalParties() {
		CandidateSeat candidateSeat1 = candidateSeat(CONTEST_1, PARTY_1, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_2, PARTY_1, new BigDecimal("20"), 200);
		return new Object[] {
				"equalParties",
				singletonList(candidateSeat1),
				asList(levelingSeatQuotient1, levelingSeatQuotient2),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 1))),
				singletonList(
						levelingSeat(levelingSeatQuotient1, 1, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
						candidateSeat1))
		};
	}

	private Object[] equalQuotients() {
		CandidateSeat candidateSeat1 = candidateSeat(CONTEST_1, PARTY_1, false);
		CandidateSeat candidateSeat2 = candidateSeat(CONTEST_2, PARTY_2, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_2, PARTY_2, new BigDecimal("30"), 100);
		return new Object[] {
				"equalQuotients",
				asList(candidateSeat1, candidateSeat2),
				asList(levelingSeatQuotient2, levelingSeatQuotient1),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 1), levelingSeatSummary(PARTY_2, 1))),
				asList(levelingSeat(levelingSeatQuotient1, 1, SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
						candidateSeat1),
						levelingSeat(levelingSeatQuotient2, 2, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 2,
								candidateSeat2))
		};
	}

	private Object[] equalVotes() {
		CandidateSeat candidateSeat1 = candidateSeat(CONTEST_1, PARTY_1, false);
		CandidateSeat candidateSeat2 = candidateSeat(CONTEST_2, PARTY_2, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_2, PARTY_2, TEN, 300);
		return new Object[] {
				"equalVotes",
				asList(candidateSeat2, candidateSeat1),
				asList(levelingSeatQuotient1, levelingSeatQuotient2),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 1), levelingSeatSummary(PARTY_2, 1))),
				asList(levelingSeat(levelingSeatQuotient1, 1, NOT_SAME_QUOTIENT_AS_NEXT, SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
						candidateSeat1),
						levelingSeat(levelingSeatQuotient2, 2, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 2,
								candidateSeat2))
		};
	}

	private Object[] electedCandidate() {
		CandidateSeat candidateSeat1 = candidateSeat(CONTEST_1, PARTY_1, true);
		CandidateSeat candidateSeat2 = candidateSeat(CONTEST_1, PARTY_1, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		return new Object[] {
				"electedCandidate",
				asList(candidateSeat2, candidateSeat1),
				singletonList(levelingSeatQuotient1),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 1))),
				singletonList(
						levelingSeat(levelingSeatQuotient1, 1, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
						candidateSeat2))
		};
	}

	private Object[] partyWithZeroLevelingSeatsOrderedFirst() {
		CandidateSeat candidateSeat = candidateSeat(CONTEST_1, PARTY_2, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("30"), 300);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_1, PARTY_2, new BigDecimal("25"), 250);
		return new Object[] {
				"partyWithZeroLevelingSeatsOrderedFirst",
				singletonList(candidateSeat),
				asList(levelingSeatQuotient1, levelingSeatQuotient2),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 0), levelingSeatSummary(PARTY_2, 1))),
				asList(levelingSeat(levelingSeatQuotient1, 1, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, PARTY_LEVELED, null,
						null),
						levelingSeat(levelingSeatQuotient2, 2, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
								candidateSeat))
		};
	}

	private Object[] partyWithZeroLevelingSeatsOrderedLast() {
		CandidateSeat candidateSeat = candidateSeat(CONTEST_1, PARTY_2, false);
		LevelingSeatQuotient levelingSeatQuotient1 = levelingSeatQuotient(CONTEST_1, PARTY_1, new BigDecimal("25"), 250);
		LevelingSeatQuotient levelingSeatQuotient2 = levelingSeatQuotient(CONTEST_1, PARTY_2, new BigDecimal("30"), 300);
		return new Object[] {
				"partyWithZeroLevelingSeatsOrderedLast",
				singletonList(candidateSeat),
				asList(levelingSeatQuotient1, levelingSeatQuotient2),
				singletonList(electionSettlement(levelingSeatSummary(PARTY_1, 0), levelingSeatSummary(PARTY_2, 1))),
				singletonList(
						levelingSeat(levelingSeatQuotient2, 1, NOT_SAME_QUOTIENT_AS_NEXT, NOT_SAME_VOTES_AS_NEXT, NOT_CONTEST_LEVELED, NOT_PARTY_LEVELED, 1,
								candidateSeat))
		};
	}

	private CandidateSeat candidateSeat(Contest contest, Party party, boolean elected) {
		CandidateSeat candidateSeat = createMock(CandidateSeat.class);
		when(candidateSeat.getContest()).thenReturn(contest);
		when(candidateSeat.getParty()).thenReturn(party);
		when(candidateSeat.isElected()).thenReturn(elected);
		return candidateSeat;
	}

	private LevelingSeatSummary levelingSeatSummary(Party party, int levelingSeats) {
		LevelingSeatSummary levelingSeatSummary = createMock(LevelingSeatSummary.class);
		when(levelingSeatSummary.getParty()).thenReturn(party);
		when(levelingSeatSummary.getLevelingSeats()).thenReturn(levelingSeats);
		return levelingSeatSummary;
	}

	private LevelingSeatQuotient levelingSeatQuotient(Contest contest, Party party, BigDecimal quotient, int partyVotes) {
		LevelingSeatQuotient levelingSeatQuotient = createMock(LevelingSeatQuotient.class);
		when(levelingSeatQuotient.getContest()).thenReturn(contest);
		when(levelingSeatQuotient.getParty()).thenReturn(party);
		when(levelingSeatQuotient.getQuotient()).thenReturn(quotient);
		when(levelingSeatQuotient.getPartyVotes()).thenReturn(partyVotes);
		return levelingSeatQuotient;
	}

	private ElectionSettlement electionSettlement(LevelingSeatSummary... levelingSeatSummaries) {
		ElectionSettlement electionSettlement = createMock(ElectionSettlement.class);
		when(electionSettlement.getLevelingSeatSummaries()).thenReturn(asList(levelingSeatSummaries));
		return electionSettlement;
	}

	private LevelingSeat levelingSeat(LevelingSeatQuotient levelingSeatQuotient, int rankNumber, boolean sameQuotientAsNext, boolean sameVotesAsNext,
			boolean contestLeveled, boolean partyLeveled, Integer seatNumber, CandidateSeat candidateSeat) {
		LevelingSeat levelingSeat = new LevelingSeat();
		levelingSeat.setLevelingSeatQuotient(levelingSeatQuotient);
		levelingSeat.setRankNumber(rankNumber);
		levelingSeat.setSameQuotientAsNext(sameQuotientAsNext);
		levelingSeat.setSameVotesAsNext(sameVotesAsNext);
		levelingSeat.setContestLeveled(contestLeveled);
		levelingSeat.setPartyLeveled(partyLeveled);
		levelingSeat.setSeatNumber(seatNumber);
		levelingSeat.setCandidateSeat(candidateSeat);
		return levelingSeat;
	}

	private static class AssertLevelingSeat {
		private final LevelingSeat levelingSeat;
		private final LevelingSeat expectedLevelingSeat;

		AssertLevelingSeat(LevelingSeat levelingSeat, LevelingSeat expectedLevelingSeat) {
			this.levelingSeat = levelingSeat;
			this.expectedLevelingSeat = expectedLevelingSeat;
		}

		void doAssert() {
			assertThat(levelingSeat).isEqualToComparingFieldByField(expectedLevelingSeat);
		}
	}
}

