package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.ElectionSeatConsumer;
import no.valg.eva.admin.settlement.domain.consumer.ElectionVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatSummaryConsumer;
import no.valg.eva.admin.settlement.domain.model.ElectionSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;

public class LevelingSeatSummaryFactory extends EntityFactory<LevelingSeatSummaryFactory, LevelingSeatSummaryConsumer>
		implements ElectionVoteCountConsumer, ElectionSeatConsumer {
	private final Map<Party, ElectionVoteCount> electionVoteCountMap = new HashMap<>();
	private final Map<Party, LevelingSeatSummary> levelingSeatSummaryMap = new LinkedHashMap<>();

	@Override
	public void consume(ElectionVoteCount electionVoteCount) {
		if (electionVoteCount.isEligibleForLevelingSeats()) {
			electionVoteCountMap.put(electionVoteCount.getParty(), electionVoteCount);
		}
	}

	@Override
	public void consume(ElectionSeat electionSeat) {
		LevelingSeatSummary levelingSeatSummary = levelingSeatSummary(electionSeat.getParty());
		if (electionSeat.isElected()) {
			levelingSeatSummary.incrementElectionSeats(1);
		}
	}

	private LevelingSeatSummary levelingSeatSummary(Party party) {
		return optional(levelingSeatSummaryMap.get(party)).orElseGet(() -> createLevelingSeatSummary(party));
	}

	private Optional<LevelingSeatSummary> optional(LevelingSeatSummary levelingSeatSummary) {
		return Optional.ofNullable(levelingSeatSummary);
	}

	private LevelingSeatSummary createLevelingSeatSummary(Party party) {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setParty(party);
		levelingSeatSummary.setContestSeats(electionVoteCountMap.get(party).getContestSeats());
		levelingSeatSummaryMap.put(party, levelingSeatSummary);
		return levelingSeatSummary;
	}

	@Override
	protected void updateConsumer(LevelingSeatSummaryConsumer consumer) {
		levelingSeatSummaryMap.values().forEach(consumer::consume);
	}

	@Override
	protected LevelingSeatSummaryFactory self() {
		return this;
	}

	public void buildLevelingSeatSummaries() {
		updateConsumers();
		levelingSeatSummaryMap.clear();
	}
}
