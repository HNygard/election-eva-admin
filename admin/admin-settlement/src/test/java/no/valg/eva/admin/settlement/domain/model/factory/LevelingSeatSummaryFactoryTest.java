package no.valg.eva.admin.settlement.domain.model.factory;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatSummaryConsumer;
import no.valg.eva.admin.settlement.domain.model.ElectionSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LevelingSeatSummaryFactoryTest extends MockUtilsTestCase {
	@Test
    public void buildLevelingSeatSummaries_givenElectionVoteCountsAndElectionSeats_buildsLevelingSeatSummaries() {
		LevelingSeatSummaryConsumer levelingSeatSummaryConsumer = createMock(LevelingSeatSummaryConsumer.class);
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = new LevelingSeatSummaryFactory();
		levelingSeatSummaryFactory.addConsumer(levelingSeatSummaryConsumer);
		Party party1 = createMock(Party.class);
		Party party2 = createMock(Party.class);

		levelingSeatSummaryFactory.consume(electionVoteCount(party1));
		levelingSeatSummaryFactory.consume(electionVoteCount(party2));
		levelingSeatSummaryFactory.consume(electionSeat(party1));
		levelingSeatSummaryFactory.consume(electionSeat(party2));
		levelingSeatSummaryFactory.buildLevelingSeatSummaries();

		ArgumentCaptor<LevelingSeatSummary> argumentCaptor = ArgumentCaptor.forClass(LevelingSeatSummary.class);
		verify(levelingSeatSummaryConsumer, times(2)).consume(argumentCaptor.capture());
		List<LevelingSeatSummary> levelingSeatSummaries = argumentCaptor.getAllValues();
		assertThat(levelingSeatSummaries.get(0)).isEqualToComparingFieldByField(levelingSeatSummary(party1));
		assertThat(levelingSeatSummaries.get(1)).isEqualToComparingFieldByField(levelingSeatSummary(party2));
	}

	@Test
    public void buildLevelingSeatSummaries_givenElectionVoteCountAndElectionSeat_sendsEntitiesToConsumersOnlyOnce() {
		LevelingSeatSummaryConsumer levelingSeatSummaryConsumer = createMock(LevelingSeatSummaryConsumer.class);
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = new LevelingSeatSummaryFactory();
		levelingSeatSummaryFactory.addConsumer(levelingSeatSummaryConsumer);
		Party party = createMock(Party.class);

		levelingSeatSummaryFactory.consume(electionVoteCount(party));
		levelingSeatSummaryFactory.consume(electionSeat(party));
		levelingSeatSummaryFactory.buildLevelingSeatSummaries();
		levelingSeatSummaryFactory.buildLevelingSeatSummaries();

		verify(levelingSeatSummaryConsumer, times(1)).consume(any(LevelingSeatSummary.class));
	}

	private ElectionVoteCount electionVoteCount(Party party) {
		ElectionVoteCount electionVoteCount = createMock(ElectionVoteCount.class);
		when(electionVoteCount.getParty()).thenReturn(party);
		when(electionVoteCount.getContestSeats()).thenReturn(2);
		when(electionVoteCount.isEligibleForLevelingSeats()).thenReturn(true);
		return electionVoteCount;
	}

	private ElectionSeat electionSeat(Party party) {
		ElectionSeat electionSeat = createMock(ElectionSeat.class);
		when(electionSeat.getParty()).thenReturn(party);
		when(electionSeat.isElected()).thenReturn(true);
		return electionSeat;
	}

	private LevelingSeatSummary levelingSeatSummary(Party party) {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setParty(party);
		levelingSeatSummary.setElectionSeats(1);
		levelingSeatSummary.setContestSeats(2);
		return levelingSeatSummary;
	}
}
