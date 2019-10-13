package no.valg.eva.admin.settlement.domain.builder;

import static no.valg.eva.admin.settlement.domain.builder.SettlementBuilder.settlementBuilder;

import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.settlement.domain.event.factory.AffiliationVoteCountEventsFromCountingModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateRankEventsFromCandidateVoteCounts;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateRankEventsFromCandidates;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateRankEventsFromConfigurationModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateRankEventsFromCountingModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateSeatEventsFromAffiliationVoteCounts;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateSeatEventsFromCandidateRanks;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateVoteCountEventsFromConfigurationModel;
import no.valg.eva.admin.settlement.domain.event.factory.CandidateVoteCountEventsFromCountingModel;
import no.valg.eva.admin.settlement.domain.model.factory.AffiliationVoteCountFactory;
import no.valg.eva.admin.settlement.domain.model.factory.CandidateRankFactory;
import no.valg.eva.admin.settlement.domain.model.factory.CandidateSeatFactory;
import no.valg.eva.admin.settlement.domain.model.factory.CandidateVoteCountFactory;
import no.valg.eva.admin.settlement.domain.model.strategy.ProcessCandidateRanksForElectionWithPersonalVotes;
import no.valg.eva.admin.settlement.domain.model.strategy.ProcessCandidateRanksForElectionWithRenumbering;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public class SettlementBuilderFactory {
	public SettlementBuilderFactory() {

	}

	public SettlementBuilder settlementBuilderForRenumberingAndStrikeOuts(Contest contest, List<ContestReport> contestReports) {
		// event factories
		AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel = new AffiliationVoteCountEventsFromCountingModel(
				contest.affiliationBaselineVotesFactor());
		CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel = new CandidateVoteCountEventsFromCountingModel();
		CandidateRankEventsFromCandidates candidateRankEventsFromCandidates = new CandidateRankEventsFromCandidates();
		CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel = new CandidateRankEventsFromCountingModel(candidateRankEventsFromCandidates);
		CandidateSeatEventsFromAffiliationVoteCounts candidateSeatEventsFromAffiliationVoteCounts = new CandidateSeatEventsFromAffiliationVoteCounts();
		CandidateSeatEventsFromCandidateRanks candidateSeatEventsFromCandidateRanks = new CandidateSeatEventsFromCandidateRanks(
				contest.getSettlementFirstDivisor());

		// entity factories
		AffiliationVoteCountFactory affiliationVoteCountFactory = new AffiliationVoteCountFactory();
		CandidateVoteCountFactory candidateVoteCountFactory = new CandidateVoteCountFactory();
		CandidateRankFactory candidateRankFactory = new CandidateRankFactory(new ProcessCandidateRanksForElectionWithRenumbering());
		CandidateSeatFactory candidateSeatFactory = new CandidateSeatFactory(contest.getNumberOfPositions());

		// builder
		SettlementBuilder settlementBuilder = settlementBuilder(contest, contestReports)
				.with(affiliationVoteCountEventsFromCountingModel)
				.with(candidateVoteCountEventsFromCountingModel)
				.with(candidateRankEventsFromCountingModel)
				.with(affiliationVoteCountFactory)
				.with(candidateVoteCountFactory)
				.with(candidateRankFactory)
				.with(candidateSeatFactory)
				.finish();

		// bindings
		affiliationVoteCountEventsFromCountingModel.addEventListener(affiliationVoteCountFactory);
		candidateVoteCountEventsFromCountingModel.addEventListener(candidateVoteCountFactory);
		candidateRankEventsFromCandidates.addEventListener(candidateRankFactory);
		candidateRankEventsFromCountingModel.addEventListener(candidateRankFactory);
		affiliationVoteCountFactory
				.addConsumer(settlementBuilder)
				.addConsumer(candidateSeatEventsFromAffiliationVoteCounts);
		candidateVoteCountFactory.addConsumer(settlementBuilder);
		candidateRankFactory
				.addConsumer(settlementBuilder)
				.addConsumer(candidateSeatEventsFromCandidateRanks);
		candidateSeatEventsFromAffiliationVoteCounts.addEventListener(candidateSeatFactory);
		candidateSeatEventsFromCandidateRanks.addEventListener(candidateSeatFactory);
		candidateSeatFactory.addConsumer(settlementBuilder);

		return settlementBuilder;
	}

	public SettlementBuilder settlementBuilderForPersonalVotesAndWriteIns(
			Contest contest, List<ContestReport> contestReports, VoteCategory baselineVoteCategory) {
		// event factories
		AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel = new AffiliationVoteCountEventsFromCountingModel(
				contest.affiliationBaselineVotesFactor());
		CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel = candidateVoteCountEventsFromConfigurationModel(contest,
				baselineVoteCategory);
		CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel = new CandidateVoteCountEventsFromCountingModel(
				candidateVoteCountEventsFromConfigurationModel);
		CandidateRankEventsFromConfigurationModel candidateRankEventsFromConfigurationModel = new CandidateRankEventsFromConfigurationModel();
		CandidateRankEventsFromCandidateVoteCounts candidateRankEventsFromCandidateVoteCounts = new CandidateRankEventsFromCandidateVoteCounts();
		CandidateSeatEventsFromAffiliationVoteCounts candidateSeatEventsFromAffiliationVoteCounts = new CandidateSeatEventsFromAffiliationVoteCounts();
		CandidateSeatEventsFromCandidateRanks candidateSeatEventsFromCandidateRanks = new CandidateSeatEventsFromCandidateRanks(
				contest.getSettlementFirstDivisor());

		// entity factories
		AffiliationVoteCountFactory affiliationVoteCountFactory = new AffiliationVoteCountFactory();
		CandidateVoteCountFactory candidateVoteCountFactory = new CandidateVoteCountFactory();
		BigDecimal candidateRankVoteShareThreshold = contest.getCandidateRankVoteShareThreshold();
		ProcessCandidateRanksForElectionWithPersonalVotes processCandidateRanksForElectionWithPersonalVotes = new ProcessCandidateRanksForElectionWithPersonalVotes(
				candidateRankVoteShareThreshold);
		CandidateRankFactory candidateRankFactory = new CandidateRankFactory(processCandidateRanksForElectionWithPersonalVotes);
		CandidateSeatFactory candidateSeatFactory = new CandidateSeatFactory(contest.getNumberOfPositions());

		// builder
		SettlementBuilder settlementBuilder = settlementBuilder(contest, contestReports)
				.with(candidateRankEventsFromConfigurationModel)
				.with(affiliationVoteCountEventsFromCountingModel)
				.with(candidateVoteCountEventsFromCountingModel)
				.with(affiliationVoteCountFactory)
				.with(candidateVoteCountFactory)
				.with(candidateRankFactory)
				.with(candidateSeatFactory)
				.finish();

		// bindings
		affiliationVoteCountEventsFromCountingModel.addEventListener(affiliationVoteCountFactory);
		if (candidateVoteCountEventsFromConfigurationModel != null) {
			candidateVoteCountEventsFromConfigurationModel.addEventListener(candidateVoteCountFactory);
		}
		candidateVoteCountEventsFromCountingModel.addEventListener(candidateVoteCountFactory);
		candidateRankEventsFromConfigurationModel.addEventListener(candidateRankFactory);
		candidateRankEventsFromCandidateVoteCounts.addEventListener(candidateRankFactory);
		affiliationVoteCountFactory
				.addConsumer(settlementBuilder)
				.addConsumer(processCandidateRanksForElectionWithPersonalVotes)
				.addConsumer(candidateSeatEventsFromAffiliationVoteCounts);
		candidateVoteCountFactory
				.addConsumer(settlementBuilder)
				.addConsumer(candidateRankEventsFromCandidateVoteCounts);
		candidateRankFactory
				.addConsumer(settlementBuilder)
				.addConsumer(candidateSeatEventsFromCandidateRanks);
		candidateSeatEventsFromAffiliationVoteCounts.addEventListener(candidateSeatFactory);
		candidateSeatEventsFromCandidateRanks.addEventListener(candidateSeatFactory);
		candidateSeatFactory.addConsumer(settlementBuilder);

		return settlementBuilder;
	}

	private CandidateVoteCountEventsFromConfigurationModel candidateVoteCountEventsFromConfigurationModel(Contest contest, VoteCategory baselineVoteCategory) {
		BigDecimal baselineVoteFactor = contest.getBaselineVoteFactor();
		if (baselineVoteFactor != null) {
			return new CandidateVoteCountEventsFromConfigurationModel(baselineVoteCategory, baselineVoteFactor);
		}
		return null;
	}
}
