package no.valg.eva.admin.settlement.domain.builder;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.settlement.domain.builder.SettlementBuilder.PartialSettlementBuilder;
import no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateRankEventsFromConfigurationModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateRankEventsFromCountingModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateVoteCountEventsFromCountingModel;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.domain.model.factory.AffiliationVoteCountFactory;
import no.valg.eva.admin.settlement.domain.model.factory.CandidateRankFactory;
import no.valg.eva.admin.settlement.domain.model.factory.CandidateSeatFactory;
import no.valg.eva.admin.settlement.domain.model.factory.CandidateVoteCountFactory;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class SettlementBuilderTest extends MockUtilsTestCase {
	@Test
	public void build_givenContest_buildsSettlement() throws Exception {
		Contest contest = contest();
		Settlement settlement = settlementBuilder(contest).build();
		assertThat(settlement).isNotNull();
		assertThat(settlement.getContest()).isSameAs(contest);
	}

	@Test
	public void build_givenCandidateRankEventsFromConfigurationModel_contestAcceptVisitor() throws Exception {
		Contest contest = contest();
		CandidateRankEventsFromConfigurationModel candidateRankEventsFromConfigurationModel = createMock(CandidateRankEventsFromConfigurationModel.class);
		settlementBuilder(contest, candidateRankEventsFromConfigurationModel).build();
		verify(contest).accept(candidateRankEventsFromConfigurationModel);
	}

	@Test
	public void build_givenNullCandidateRankEventsFromConfigurationModel_doesNotThrowException() throws Exception {
		settlementBuilder().build();
	}

	@Test
	public void build_givenAffiliationVoteCountEventsFromCountingModel_contestReportsAcceptVisitor() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel = createMock(AffiliationVoteCountEventsFromCountingModel.class);
		settlementBuilder(contestReport, affiliationVoteCountEventsFromCountingModel).build();
		verify(contestReport).accept(affiliationVoteCountEventsFromCountingModel);
	}

	@Test
	public void build_givenCandidateVoteCountEventsFromCountingModel_contestReportsAcceptVisitor() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel = createMock(CandidateVoteCountEventsFromCountingModel.class);
		settlementBuilder(contestReport, candidateVoteCountEventsFromCountingModel).build();
		verify(contestReport).accept(candidateVoteCountEventsFromCountingModel);
	}

	@Test
	public void build_givenCandidateRankEventsFromCountingModel_contestReportsAcceptVisitor() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel = createMock(CandidateRankEventsFromCountingModel.class);
		settlementBuilder(contestReport, candidateRankEventsFromCountingModel).build();
		verify(contestReport).accept(candidateRankEventsFromCountingModel);
	}

	@Test
	public void build_givenAffiliationVoteCountFactory_buildAffiliationVoteCounts() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		AffiliationVoteCountFactory affiliationVoteCountFactory = createMock(AffiliationVoteCountFactory.class);
		settlementBuilder(contestReport, affiliationVoteCountFactory).build();
		verify(affiliationVoteCountFactory).buildAffiliationVoteCounts();
	}

	@Test
	public void build_givenCandidateVoteCountFactory_buildCandidateVoteCounts() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		CandidateVoteCountFactory candidateVoteCountFactory = createMock(CandidateVoteCountFactory.class);
		settlementBuilder(contestReport, candidateVoteCountFactory).build();
		verify(candidateVoteCountFactory).buildCandidateVoteCounts();
	}

	@Test
	public void build_givenCandidateRankFactory_buildCandidateRanks() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		CandidateRankFactory candidateRankFactory = createMock(CandidateRankFactory.class);
		settlementBuilder(contestReport, candidateRankFactory).build();
		verify(candidateRankFactory).buildCandidateRanks();
	}

	@Test
	public void build_givenCandidateSeatFactory_buildCandidateSeats() throws Exception {
		ContestReport contestReport = mock(ContestReport.class);
		CandidateSeatFactory candidateSeatFactory = createMock(CandidateSeatFactory.class);
		settlementBuilder(contestReport, candidateSeatFactory).build();
		verify(candidateSeatFactory).buildCandidateSeats();
	}

	@Test
	public void consume_givenAffiliationVoteCount_addAffiliationVoteCountToSettlement() throws Exception {
		SettlementBuilder settlementBuilder = settlementBuilder();
		AffiliationVoteCount affiliationVoteCount = mock(AffiliationVoteCount.class);
		settlementBuilder.consume(affiliationVoteCount);
		Settlement settlement = settlementBuilder.build();
		assertThat(settlement.getAffiliationVoteCounts()).containsExactly(affiliationVoteCount);
	}

	@Test
	public void consume_givenCandidateVoteCount_addCandidateVoteCountToSettlement() throws Exception {
		SettlementBuilder settlementBuilder = settlementBuilder();
		CandidateVoteCount candidateVoteCount = mock(CandidateVoteCount.class);
		settlementBuilder.consume(candidateVoteCount);
		Settlement settlement = settlementBuilder.build();
		assertThat(settlement.getCandidateVoteCounts()).containsExactly(candidateVoteCount);
	}

	@Test
	public void consume_givenCandidateRank_addCandidateRankToSettlement() throws Exception {
		SettlementBuilder settlementBuilder = settlementBuilder();
		CandidateRank candidateRank = mock(CandidateRank.class);
		settlementBuilder.consume(candidateRank);
		Settlement settlement = settlementBuilder.build();
		assertThat(settlement.getCandidateRanks()).containsExactly(candidateRank);
	}

	@Test
	public void consume_givenCandidateSeat_addCandidateSeatToSettlement() throws Exception {
		SettlementBuilder settlementBuilder = settlementBuilder();
		CandidateSeat candidateSeat = mock(CandidateSeat.class);
		settlementBuilder.consume(candidateSeat);
		Settlement settlement = settlementBuilder.build();
		assertThat(settlement.getCandidateSeats()).containsExactly(candidateSeat);
	}

	private SettlementBuilder settlementBuilder() {
		return partialSettlementBuilder(SettlementBuilder.settlementBuilder(contest(), emptyContestReports())).finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport,
			AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel) {
		PartialSettlementBuilder partialSettlementBuilder = SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport));
		return partialSettlementBuilder(partialSettlementBuilder)
				.with(affiliationVoteCountEventsFromCountingModel)
				.finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport,
			CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel) {
		PartialSettlementBuilder partialSettlementBuilder = SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport));
		return partialSettlementBuilder(partialSettlementBuilder)
				.with(candidateVoteCountEventsFromCountingModel)
				.finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport, CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel) {
		PartialSettlementBuilder partialSettlementBuilder = SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport));
		return partialSettlementBuilder(partialSettlementBuilder)
				.with(candidateRankEventsFromCountingModel)
				.finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport, AffiliationVoteCountFactory affiliationVoteCountFactory) {
		return SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport))
				.with(affiliationVoteCountFactory)
				.with(createMock(CandidateVoteCountFactory.class))
				.with(createMock(CandidateRankFactory.class))
				.with(createMock(CandidateSeatFactory.class))
				.finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport, CandidateVoteCountFactory candidateVoteCountFactory) {
		return SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport))
				.with(createMock(AffiliationVoteCountFactory.class))
				.with(candidateVoteCountFactory)
				.with(createMock(CandidateRankFactory.class))
				.with(createMock(CandidateSeatFactory.class))
				.finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport, CandidateRankFactory candidateRankFactory) {
		return SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport))
				.with(createMock(AffiliationVoteCountFactory.class))
				.with(createMock(CandidateVoteCountFactory.class))
				.with(candidateRankFactory)
				.with(createMock(CandidateSeatFactory.class))
				.finish();
	}

	private SettlementBuilder settlementBuilder(ContestReport contestReport, CandidateSeatFactory candidateSeatFactory) {
		return SettlementBuilder.settlementBuilder(contest(), singletonList(contestReport))
				.with(createMock(AffiliationVoteCountFactory.class))
				.with(createMock(CandidateVoteCountFactory.class))
				.with(createMock(CandidateRankFactory.class))
				.with(candidateSeatFactory)
				.finish();
	}

	private SettlementBuilder settlementBuilder(Contest contest) {
		return partialSettlementBuilder(SettlementBuilder.settlementBuilder(contest, emptyContestReports())).finish();
	}

	private SettlementBuilder settlementBuilder(Contest contest, CandidateRankEventsFromConfigurationModel candidateRankEventsFromConfigurationModel) {
		return partialSettlementBuilder(SettlementBuilder.settlementBuilder(contest, emptyContestReports())
				.with(candidateRankEventsFromConfigurationModel))
						.finish();
	}

	private PartialSettlementBuilder partialSettlementBuilder(PartialSettlementBuilder partialSettlementBuilder) {
		return partialSettlementBuilder
				.with(createMock(AffiliationVoteCountFactory.class))
				.with(createMock(CandidateVoteCountFactory.class))
				.with(createMock(CandidateRankFactory.class))
				.with(createMock(CandidateSeatFactory.class));
	}

	private Contest contest() {
		return mock(Contest.class, RETURNS_DEEP_STUBS);
	}

	private List<ContestReport> emptyContestReports() {
		return new ArrayList<>();
	}
}

