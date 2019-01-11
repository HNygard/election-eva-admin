package no.valg.eva.admin.settlement.domain.builder;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.event.factory.ElectionVoteCountEventsFromSettlementModel;
import no.valg.eva.admin.settlement.domain.event.factory.LevelingSeatQuotientEventsFromSettlementModel;
import no.valg.eva.admin.settlement.domain.model.ElectionSettlement;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;
import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSettlement;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.domain.model.factory.ElectionSeatFactory;
import no.valg.eva.admin.settlement.domain.model.factory.ElectionSettlementFactory;
import no.valg.eva.admin.settlement.domain.model.factory.ElectionVoteCountFactory;
import no.valg.eva.admin.settlement.domain.model.factory.LevelingSeatFactory;
import no.valg.eva.admin.settlement.domain.model.factory.LevelingSeatQuotientFactory;
import no.valg.eva.admin.settlement.domain.model.factory.LevelingSeatSummaryFactory;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class LevelingSeatSettlementBuilderTest extends MockUtilsTestCase {
	@Test
	public void build_givenElection_buildsLevelingSeatSettlement() throws Exception {
		Election election = election();
		LevelingSeatSettlement levelingSeatSettlement = levelingSeatSettlementBuilder(election).build();
		assertThat(levelingSeatSettlement).isNotNull();
		assertThat(levelingSeatSettlement.getElection()).isSameAs(election);
	}

	@Test
	public void build_givenElectionVoteCountEventsFromSettlementModel_settlementsAcceptVisitor() throws Exception {
		Settlement settlement = mock(Settlement.class);
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = createMock(ElectionVoteCountEventsFromSettlementModel.class);
		levelingSeatSettlementBuilder(settlement, electionVoteCountEventsFromSettlementModel)
				.build();
		verify(settlement).accept(electionVoteCountEventsFromSettlementModel);
	}

	@Test
	public void build_givenLevelingSeatQuotientEventsFromSettlementModel_settlementsAcceptVisitor() throws Exception {
		Settlement settlement = mock(Settlement.class);
		LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel = createMock(
				LevelingSeatQuotientEventsFromSettlementModel.class);
		levelingSeatSettlementBuilder(settlement, levelingSeatQuotientEventsFromSettlementModel)
				.build();
		verify(settlement).accept(levelingSeatQuotientEventsFromSettlementModel);
	}

	@Test
	public void consume_givenElectionSettlement_addsElectionSettlementToLevelingSeatSettlement() throws Exception {
		LevelingSeatSettlementBuilder builder = levelingSeatSettlementBuilder();
		ElectionSettlement electionSettlement = mock(ElectionSettlement.class);
		builder.consume(electionSettlement);
		LevelingSeatSettlement levelingSeatSettlement = builder.build();
		assertThat(levelingSeatSettlement.getElectionSettlements()).containsExactly(electionSettlement);
	}

	@Test
	public void consume_givenElectionVoteCount_addsElectionVoteCountToLevelingSeatSettlement() throws Exception {
		LevelingSeatSettlementBuilder builder = levelingSeatSettlementBuilder();
		ElectionVoteCount electionVoteCount = mock(ElectionVoteCount.class);
		builder.consume(electionVoteCount);
		LevelingSeatSettlement levelingSeatSettlement = builder.build();
		assertThat(levelingSeatSettlement.getElectionVoteCounts()).containsExactly(electionVoteCount);
	}

	@Test
	public void consume_givenLevelingSeat_addsLevelingSeatToLevelingSeatSettlement() throws Exception {
		LevelingSeatSettlementBuilder builder = levelingSeatSettlementBuilder();
		LevelingSeat levelingSeat = mock(LevelingSeat.class);
		builder.consume(levelingSeat);
		LevelingSeatSettlement levelingSeatSettlement = builder.build();
		assertThat(levelingSeatSettlement.getLevelingSeats()).containsExactly(levelingSeat);
	}

	@Test
	public void consume_givenLevelingSeatQuotient_addsLevelingSeatQuotientToLevelingSeatSettlement() throws Exception {
		LevelingSeatSettlementBuilder builder = levelingSeatSettlementBuilder();
		LevelingSeatQuotient levelingSeatQuotient = mock(LevelingSeatQuotient.class);
		builder.consume(levelingSeatQuotient);
		LevelingSeatSettlement levelingSeatSettlement = builder.build();
		assertThat(levelingSeatSettlement.getLevelingSeatQuotients()).containsExactly(levelingSeatQuotient);
	}

	private LevelingSeatSettlementBuilder levelingSeatSettlementBuilder(Election election) {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = createMock(ElectionVoteCountEventsFromSettlementModel.class);
		LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel = createMock(
				LevelingSeatQuotientEventsFromSettlementModel.class);
		ElectionVoteCountFactory electionVoteCountFactory = createMock(ElectionVoteCountFactory.class);
		ElectionSeatFactory electionSeatFactory = electionSeatFactory();
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = createMock(LevelingSeatSummaryFactory.class);
		ElectionSettlementFactory electionSettlementFactory = createMock(ElectionSettlementFactory.class);
		LevelingSeatQuotientFactory levelingSeatQuotientFactory = createMock(LevelingSeatQuotientFactory.class);
		LevelingSeatFactory levelingSeatFactory = createMock(LevelingSeatFactory.class);
		return LevelingSeatSettlementBuilder.levelingSeatSettlementBuilder(election, createListMock())
				.with(electionVoteCountEventsFromSettlementModel)
				.with(levelingSeatQuotientEventsFromSettlementModel)
				.with(electionVoteCountFactory)
				.with(electionSeatFactory)
				.with(levelingSeatSummaryFactory)
				.with(electionSettlementFactory)
				.with(levelingSeatQuotientFactory)
				.with(levelingSeatFactory)
				.finish();
	}

	private LevelingSeatSettlementBuilder levelingSeatSettlementBuilder() {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = createMock(ElectionVoteCountEventsFromSettlementModel.class);
		LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel = createMock(
				LevelingSeatQuotientEventsFromSettlementModel.class);
		ElectionVoteCountFactory electionVoteCountFactory = createMock(ElectionVoteCountFactory.class);
		ElectionSeatFactory electionSeatFactory = electionSeatFactory();
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = createMock(LevelingSeatSummaryFactory.class);
		ElectionSettlementFactory electionSettlementFactory = createMock(ElectionSettlementFactory.class);
		LevelingSeatQuotientFactory levelingSeatQuotientFactory = createMock(LevelingSeatQuotientFactory.class);
		LevelingSeatFactory levelingSeatFactory = createMock(LevelingSeatFactory.class);
		return LevelingSeatSettlementBuilder.levelingSeatSettlementBuilder(election(), createListMock())
				.with(electionVoteCountEventsFromSettlementModel)
				.with(levelingSeatQuotientEventsFromSettlementModel)
				.with(electionVoteCountFactory)
				.with(electionSeatFactory)
				.with(levelingSeatSummaryFactory)
				.with(electionSettlementFactory)
				.with(levelingSeatQuotientFactory)
				.with(levelingSeatFactory)
				.finish();
	}

	private LevelingSeatSettlementBuilder levelingSeatSettlementBuilder(Settlement settlement,
			ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel) {
		LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel = createMock(
				LevelingSeatQuotientEventsFromSettlementModel.class);
		List<Settlement> settlements = singletonList(settlement);
		ElectionVoteCountFactory electionVoteCountFactory = createMock(ElectionVoteCountFactory.class);
		ElectionSeatFactory electionSeatFactory = electionSeatFactory();
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = createMock(LevelingSeatSummaryFactory.class);
		ElectionSettlementFactory electionSettlementFactory = createMock(ElectionSettlementFactory.class);
		LevelingSeatQuotientFactory levelingSeatQuotientFactory = createMock(LevelingSeatQuotientFactory.class);
		LevelingSeatFactory levelingSeatFactory = createMock(LevelingSeatFactory.class);
		return LevelingSeatSettlementBuilder.levelingSeatSettlementBuilder(election(), settlements)
				.with(electionVoteCountEventsFromSettlementModel)
				.with(levelingSeatQuotientEventsFromSettlementModel)
				.with(electionVoteCountFactory)
				.with(electionSeatFactory)
				.with(levelingSeatSummaryFactory)
				.with(electionSettlementFactory)
				.with(levelingSeatQuotientFactory)
				.with(levelingSeatFactory)
				.finish();
	}

	private LevelingSeatSettlementBuilder levelingSeatSettlementBuilder(Settlement settlement,
			LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel) {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = createMock(ElectionVoteCountEventsFromSettlementModel.class);
		List<Settlement> settlements = singletonList(settlement);
		ElectionVoteCountFactory electionVoteCountFactory = createMock(ElectionVoteCountFactory.class);
		ElectionSeatFactory electionSeatFactory = electionSeatFactory();
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = createMock(LevelingSeatSummaryFactory.class);
		ElectionSettlementFactory electionSettlementFactory = createMock(ElectionSettlementFactory.class);
		LevelingSeatQuotientFactory levelingSeatQuotientFactory = createMock(LevelingSeatQuotientFactory.class);
		LevelingSeatFactory levelingSeatFactory = createMock(LevelingSeatFactory.class);
		return LevelingSeatSettlementBuilder.levelingSeatSettlementBuilder(election(), settlements)
				.with(electionVoteCountEventsFromSettlementModel)
				.with(levelingSeatQuotientEventsFromSettlementModel)
				.with(electionVoteCountFactory)
				.with(electionSeatFactory)
				.with(levelingSeatSummaryFactory)
				.with(electionSettlementFactory)
				.with(levelingSeatQuotientFactory)
				.with(levelingSeatFactory)
				.finish();
	}

	private ElectionSeatFactory electionSeatFactory() {
		ElectionSeatFactory electionSeatFactory = createMock(ElectionSeatFactory.class);
		when(electionSeatFactory.isMoreElectionSeatsNeeded()).thenReturn(false);
		return electionSeatFactory;
	}

	private Election election() {
		return createMock(Election.class);
	}
}
