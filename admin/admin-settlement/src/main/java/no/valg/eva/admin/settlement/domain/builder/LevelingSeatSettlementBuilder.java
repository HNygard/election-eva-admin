package no.valg.eva.admin.settlement.domain.builder;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatSettlementEntitiesConsumer;
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

public class LevelingSeatSettlementBuilder implements LevelingSeatSettlementEntitiesConsumer {
	private final LevelingSeatSettlement levelingSeatSettlement;
	private final List<Settlement> settlements;
	private ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel;
	private LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel;
	private ElectionVoteCountFactory electionVoteCountFactory;
	private ElectionSeatFactory electionSeatFactory;
	private LevelingSeatSummaryFactory levelingSeatSummaryFactory;
	private ElectionSettlementFactory electionSettlementFactory;
	private LevelingSeatQuotientFactory levelingSeatQuotientFactory;
	private LevelingSeatFactory levelingSeatFactory;

	private LevelingSeatSettlementBuilder(Election election, List<Settlement> settlements) {
		this.levelingSeatSettlement = new LevelingSeatSettlement(election);
		this.settlements = settlements;
	}

	public static PartialLevelingSeatSettlementBuilder levelingSeatSettlementBuilder(Election election, List<Settlement> settlements) {
		return new PartialLevelingSeatSettlementBuilder(election, settlements);
	}

	public LevelingSeatSettlement build() {
		settlements.forEach(this::acceptSettlementVisitors);
		electionVoteCountFactory.buildElectionVoteCounts();
		do {
			electionSeatFactory.buildElectionSeats();
			levelingSeatSummaryFactory.buildLevelingSeatSummaries();
			electionSettlementFactory.buildElectionSettlement();
		} while (electionSeatFactory.isMoreElectionSeatsNeeded());
		levelingSeatQuotientFactory.buildLevelingSeatQuotients();
		levelingSeatFactory.buildLevelingSeats();
		return levelingSeatSettlement;
	}

	private void acceptSettlementVisitors(Settlement settlement) {
		settlement.accept(electionVoteCountEventsFromSettlementModel);
		settlement.accept(levelingSeatQuotientEventsFromSettlementModel);
		settlement.accept(levelingSeatFactory);
	}

	@Override
	public void consume(ElectionSettlement electionSettlement) {
		levelingSeatSettlement.addElectionSettlement(electionSettlement);
	}

	@Override
	public void consume(ElectionVoteCount electionVoteCount) {
		levelingSeatSettlement.addElectionVoteCount(electionVoteCount);
	}

	@Override
	public void consume(LevelingSeat levelingSeat) {
		levelingSeatSettlement.addLevelingSeat(levelingSeat);
	}

	@Override
	public void consume(LevelingSeatQuotient levelingSeatQuotient) {
		levelingSeatSettlement.addLevelingSeatQuotient(levelingSeatQuotient);
	}

	public static class PartialLevelingSeatSettlementBuilder {
		private LevelingSeatSettlementBuilder builder;

		public PartialLevelingSeatSettlementBuilder(Election election, List<Settlement> settlements) {
			this.builder = new LevelingSeatSettlementBuilder(election, settlements);
		}

		public PartialLevelingSeatSettlementBuilder with(ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel) {
			this.builder.electionVoteCountEventsFromSettlementModel = electionVoteCountEventsFromSettlementModel;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel) {
			this.builder.levelingSeatQuotientEventsFromSettlementModel = levelingSeatQuotientEventsFromSettlementModel;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(ElectionVoteCountFactory electionVoteCountFactory) {
			this.builder.electionVoteCountFactory = electionVoteCountFactory;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(ElectionSeatFactory electionSeatFactory) {
			this.builder.electionSeatFactory = electionSeatFactory;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(LevelingSeatSummaryFactory levelingSeatSummaryFactory) {
			this.builder.levelingSeatSummaryFactory = levelingSeatSummaryFactory;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(ElectionSettlementFactory electionSettlementFactory) {
			this.builder.electionSettlementFactory = electionSettlementFactory;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(LevelingSeatQuotientFactory levelingSeatQuotientFactory) {
			this.builder.levelingSeatQuotientFactory = levelingSeatQuotientFactory;
			return this;
		}

		public PartialLevelingSeatSettlementBuilder with(LevelingSeatFactory levelingSeatFactory) {
			this.builder.levelingSeatFactory = levelingSeatFactory;
			return this;
		}

		public LevelingSeatSettlementBuilder finish() {
			return builder;
		}
	}
}
