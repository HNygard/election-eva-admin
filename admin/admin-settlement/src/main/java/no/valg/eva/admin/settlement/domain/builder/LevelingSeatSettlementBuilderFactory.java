package no.valg.eva.admin.settlement.domain.builder;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.event.factory.ElectionVoteCountEventsFromSettlementModel;
import no.valg.eva.admin.settlement.domain.event.factory.LevelingSeatQuotientEventsFromSettlementModel;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.domain.model.factory.ElectionSeatFactory;
import no.valg.eva.admin.settlement.domain.model.factory.ElectionSettlementFactory;
import no.valg.eva.admin.settlement.domain.model.factory.ElectionVoteCountFactory;
import no.valg.eva.admin.settlement.domain.model.factory.LevelingSeatFactory;
import no.valg.eva.admin.settlement.domain.model.factory.LevelingSeatQuotientFactory;
import no.valg.eva.admin.settlement.domain.model.factory.LevelingSeatSummaryFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public class LevelingSeatSettlementBuilderFactory {
	public LevelingSeatSettlementBuilderFactory() {

	}

	public LevelingSeatSettlementBuilder levelingSeatSettlementBuilder(Election election, List<Settlement> settlements) {
		// event factories
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		LevelingSeatQuotientEventsFromSettlementModel levelingSeatQuotientEventsFromSettlementModel = new LevelingSeatQuotientEventsFromSettlementModel();

		// entity factories
		ElectionVoteCountFactory electionVoteCountFactory = new ElectionVoteCountFactory(election.getLevelingSeatsVoteShareThreshold());
		ElectionSeatFactory electionSeatFactory = new ElectionSeatFactory(election.getSettlementFirstDivisor(), election.getLevelingSeats());
		LevelingSeatSummaryFactory levelingSeatSummaryFactory = new LevelingSeatSummaryFactory();
		ElectionSettlementFactory electionSettlementFactory = new ElectionSettlementFactory();
		LevelingSeatQuotientFactory levelingSeatQuotientFactory = new LevelingSeatQuotientFactory();
		LevelingSeatFactory levelingSeatFactory = new LevelingSeatFactory();

		// builder
		LevelingSeatSettlementBuilder levelingSeatSettlementBuilder = LevelingSeatSettlementBuilder.levelingSeatSettlementBuilder(election, settlements)
				.with(electionVoteCountEventsFromSettlementModel)
				.with(levelingSeatQuotientEventsFromSettlementModel)
				.with(electionVoteCountFactory)
				.with(electionSeatFactory)
				.with(levelingSeatSummaryFactory)
				.with(electionSettlementFactory)
				.with(levelingSeatQuotientFactory)
				.with(levelingSeatFactory)
				.finish();

		// bindings
		electionVoteCountEventsFromSettlementModel.addEventListener(electionVoteCountFactory);
		levelingSeatQuotientEventsFromSettlementModel.addEventListener(levelingSeatQuotientFactory);
		electionVoteCountFactory
				.addConsumer(levelingSeatSettlementBuilder)
				.addConsumer(electionSeatFactory)
				.addConsumer(levelingSeatSummaryFactory);
		electionSeatFactory
				.addConsumer(levelingSeatSummaryFactory)
				.addConsumer(electionSettlementFactory);
		levelingSeatSummaryFactory
				.addConsumer(electionSeatFactory)
				.addConsumer(electionSettlementFactory);
		electionSettlementFactory
				.addConsumer(levelingSeatSettlementBuilder)
				.addConsumer(levelingSeatFactory);
		levelingSeatQuotientFactory
				.addConsumer(levelingSeatFactory)
				.addConsumer(levelingSeatSettlementBuilder);
		levelingSeatFactory.addConsumer(levelingSeatSettlementBuilder);

		return levelingSeatSettlementBuilder;
	}
}
