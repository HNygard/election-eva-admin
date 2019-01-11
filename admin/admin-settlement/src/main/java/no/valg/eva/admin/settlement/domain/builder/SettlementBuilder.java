package no.valg.eva.admin.settlement.domain.builder;

import java.util.List;
import java.util.Optional;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;
import no.valg.eva.admin.settlement.domain.consumer.SettlementEntitiesConsumer;
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

public class SettlementBuilder implements SettlementEntitiesConsumer {
	private Contest contest;
	private Settlement settlement;
	private List<ContestReport> contestReports;

	// event factories
	private CandidateRankEventsFromConfigurationModel candidateRankEventsFromConfigurationModel;
	private AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel;
	private CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel;
	private CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel;

	// entity factories
	private AffiliationVoteCountFactory affiliationVoteCountFactory;
	private CandidateVoteCountFactory candidateVoteCountFactory;
	private CandidateRankFactory candidateRankFactory;
	private CandidateSeatFactory candidateSeatFactory;

	private SettlementBuilder(Contest contest, List<ContestReport> contestReports) {
		this.contest = contest;
		this.settlement = new Settlement(contest);
		this.contestReports = contestReports;
	}

	public static PartialSettlementBuilder settlementBuilder(Contest contest, List<ContestReport> contestReports) {
		return new PartialSettlementBuilder(contest, contestReports);
	}

	public Settlement build() {
		optional(candidateRankEventsFromConfigurationModel).ifPresent(contest::accept);
		contestReports.forEach(this::acceptVisitors);
		buildEntities();
		return settlement;
	}

	private void acceptVisitors(ContestReport contestReport) {
		acceptVisitor(contestReport, affiliationVoteCountEventsFromCountingModel);
		acceptVisitor(contestReport, candidateVoteCountEventsFromCountingModel);
		acceptVisitor(contestReport, candidateRankEventsFromCountingModel);
	}

	private void acceptVisitor(ContestReport contestReport, CountingVisitor eventListener) {
		optional(eventListener).ifPresent(contestReport::accept);
	}

	private void buildEntities() {
		affiliationVoteCountFactory.buildAffiliationVoteCounts();
		candidateVoteCountFactory.buildCandidateVoteCounts();
		candidateRankFactory.buildCandidateRanks();
		candidateSeatFactory.buildCandidateSeats();
	}

	private <T> Optional<T> optional(T t) {
		return Optional.ofNullable(t);
	}

	@Override
	public void consume(AffiliationVoteCount affiliationVoteCount) {
		settlement.addAffiliationVoteCount(affiliationVoteCount);
	}

	@Override
	public void consume(CandidateVoteCount candidateVoteCount) {
		settlement.addCandidateVoteCount(candidateVoteCount);
	}

	@Override
	public void consume(CandidateRank candidateRank) {
		settlement.addCandidateRank(candidateRank);
	}

	@Override
	public void consume(CandidateSeat candidateSeat) {
		settlement.addCandidateSeat(candidateSeat);
	}

	public static class PartialSettlementBuilder {
		private SettlementBuilder settlementBuilder;

		private PartialSettlementBuilder(Contest contest, List<ContestReport> contestReports) {
			this.settlementBuilder = new SettlementBuilder(contest, contestReports);
		}

		public PartialSettlementBuilder with(CandidateRankEventsFromConfigurationModel candidateRankEventsFromConfigurationModel) {
			this.settlementBuilder.candidateRankEventsFromConfigurationModel = candidateRankEventsFromConfigurationModel;
			return this;
		}

		public PartialSettlementBuilder with(AffiliationVoteCountEventsFromCountingModel affiliationVoteCountEventsFromCountingModel) {
			this.settlementBuilder.affiliationVoteCountEventsFromCountingModel = affiliationVoteCountEventsFromCountingModel;
			return this;
		}

		public PartialSettlementBuilder with(CandidateVoteCountEventsFromCountingModel candidateVoteCountEventsFromCountingModel) {
			this.settlementBuilder.candidateVoteCountEventsFromCountingModel = candidateVoteCountEventsFromCountingModel;
			return this;
		}

		public PartialSettlementBuilder with(CandidateRankEventsFromCountingModel candidateRankEventsFromCountingModel) {
			this.settlementBuilder.candidateRankEventsFromCountingModel = candidateRankEventsFromCountingModel;
			return this;
		}

		public PartialSettlementBuilder with(AffiliationVoteCountFactory affiliationVoteCountFactory) {
			this.settlementBuilder.affiliationVoteCountFactory = affiliationVoteCountFactory;
			return this;
		}

		public PartialSettlementBuilder with(CandidateVoteCountFactory candidateVoteCountFactory) {
			this.settlementBuilder.candidateVoteCountFactory = candidateVoteCountFactory;
			return this;
		}

		public PartialSettlementBuilder with(CandidateRankFactory candidateRankFactory) {
			this.settlementBuilder.candidateRankFactory = candidateRankFactory;
			return this;
		}

		public PartialSettlementBuilder with(CandidateSeatFactory candidateSeatFactory) {
			this.settlementBuilder.candidateSeatFactory = candidateSeatFactory;
			return this;
		}

		public SettlementBuilder finish() {
			return settlementBuilder;
		}
	}
}
