package no.valg.eva.admin.settlement.domain.model.factory;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import no.valg.eva.admin.settlement.domain.consumer.ElectionSettlementConsumer;
import no.valg.eva.admin.settlement.domain.model.ElectionSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionSettlement;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class ElectionSettlementFactoryTest extends MockUtilsTestCase {
	@Test
	public void buildElectionSettlement_givenElectionSeats_addsElectionSeatsToElectionSettlement() throws Exception {
		ElectionSettlementConsumer consumer = createMock(ElectionSettlementConsumer.class);
		ElectionSettlementFactory electionSettlementFactory = new ElectionSettlementFactory();
		electionSettlementFactory.addConsumer(consumer);
		ElectionSeat electionSeat1 = electionSeat();
		ElectionSeat electionSeat2 = electionSeat();

		buildElectionSettlement(electionSettlementFactory, electionSeat1, electionSeat2);

		ArgumentCaptor<ElectionSettlement> argumentCaptor = ArgumentCaptor.forClass(ElectionSettlement.class);
		verify(consumer, times(1)).consume(argumentCaptor.capture());
		ElectionSettlement electionSettlement = argumentCaptor.getValue();
		assertThat(electionSettlement).isEqualToComparingFieldByField(electionSettlement(asList(electionSeat1, electionSeat2), 1));
		verify(electionSeat1).setElectionSettlement(electionSettlement);
		verify(electionSeat2).setElectionSettlement(electionSettlement);
	}

	@Test
	public void buildElectionSettlement_givenLevelingSeatSummeries_addsLevelingSeatSummariesToElectionSettlement() throws Exception {
		ElectionSettlementConsumer consumer = createMock(ElectionSettlementConsumer.class);
		ElectionSettlementFactory electionSettlementFactory = new ElectionSettlementFactory();
		electionSettlementFactory.addConsumer(consumer);
		LevelingSeatSummary levelingSeatSummary1 = levelingSeatSummary();
		LevelingSeatSummary levelingSeatSummary2 = levelingSeatSummary();

		buildElectionSettlement(electionSettlementFactory, levelingSeatSummary1, levelingSeatSummary2);

		ArgumentCaptor<ElectionSettlement> argumentCaptor = ArgumentCaptor.forClass(ElectionSettlement.class);
		verify(consumer, times(1)).consume(argumentCaptor.capture());
		ElectionSettlement electionSettlement = argumentCaptor.getValue();
		assertThat(electionSettlement).isEqualToComparingFieldByField(electionSettlement(emptyList(), asList(levelingSeatSummary1, levelingSeatSummary2), 1));
		verify(levelingSeatSummary1).setElectionSettlement(electionSettlement);
		verify(levelingSeatSummary1).setElectionSettlement(electionSettlement);
	}

	@Test
	public void buildElectionSettlement_givenDataTwoTimes_buildsCorrectElectionSettlements() throws Exception {
		ElectionSettlementConsumer consumer = createMock(ElectionSettlementConsumer.class);
		ElectionSettlementFactory electionSettlementFactory = new ElectionSettlementFactory();
		electionSettlementFactory.addConsumer(consumer);
		ElectionSeat electionSeat = electionSeat();
		LevelingSeatSummary levelingSeatSummary = levelingSeatSummary();

		// run 1
		buildElectionSettlement(electionSettlementFactory, electionSeat, levelingSeatSummary);

		// run 2
		buildElectionSettlement(electionSettlementFactory, electionSeat, levelingSeatSummary);

		ArgumentCaptor<ElectionSettlement> argumentCaptor = ArgumentCaptor.forClass(ElectionSettlement.class);
		verify(consumer, times(2)).consume(argumentCaptor.capture());
		List<ElectionSettlement> electionSettlements = argumentCaptor.getAllValues();
		assertThat(electionSettlements.get(0)).isEqualToComparingFieldByField(electionSettlement(electionSeat, levelingSeatSummary, 1));
		assertThat(electionSettlements.get(1)).isEqualToComparingFieldByField(electionSettlement(electionSeat, levelingSeatSummary, 2));
	}

	private void buildElectionSettlement(ElectionSettlementFactory electionSettlementFactory, ElectionSeat electionSeat,
			LevelingSeatSummary levelingSeatSummary) {
		electionSettlementFactory.consume(electionSeat);
		electionSettlementFactory.consume(levelingSeatSummary);
		electionSettlementFactory.buildElectionSettlement();
	}

	private void buildElectionSettlement(ElectionSettlementFactory electionSettlementFactory, ElectionSeat electionSeat1, ElectionSeat electionSeat2) {
		electionSettlementFactory.consume(electionSeat1);
		electionSettlementFactory.consume(electionSeat2);
		electionSettlementFactory.buildElectionSettlement();
	}

	private void buildElectionSettlement(
			ElectionSettlementFactory electionSettlementFactory, LevelingSeatSummary levelingSeatSummary1, LevelingSeatSummary levelingSeatSummary2) {
		electionSettlementFactory.consume(levelingSeatSummary1);
		electionSettlementFactory.consume(levelingSeatSummary2);
		electionSettlementFactory.buildElectionSettlement();
	}

	private ElectionSettlement electionSettlement(ElectionSeat electionSeat, LevelingSeatSummary levelingSeatSummary, int settlementNumber) {
		return electionSettlement(singletonList(electionSeat), singletonList(levelingSeatSummary), settlementNumber);
	}

	private ElectionSettlement electionSettlement(List<ElectionSeat> electionSeats, int settlementNumber) {
		return electionSettlement(electionSeats, emptyList(), settlementNumber);
	}

	private ElectionSettlement electionSettlement(List<ElectionSeat> electionSeats, List<LevelingSeatSummary> levelingSeatSummaries, int settlementNumber) {
		ElectionSettlement electionSettlement = new ElectionSettlement();
		electionSettlement.setElectionSeats(electionSeats);
		electionSettlement.setLevelingSeatSummaries(levelingSeatSummaries);
		electionSettlement.setSettlementNumber(settlementNumber);
		return electionSettlement;
	}

	private ElectionSeat electionSeat() {
		return createMock(ElectionSeat.class);
	}

	private LevelingSeatSummary levelingSeatSummary() {
		return createMock(LevelingSeatSummary.class);
	}
}
