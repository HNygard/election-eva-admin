package no.valg.eva.admin.settlement.domain.model.factory;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.ElectionVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.ElectionVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.ElectionVoteCountEventListener;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;

public class ElectionVoteCountFactory extends EntityFactory<ElectionVoteCountFactory, ElectionVoteCountConsumer> implements ElectionVoteCountEventListener {
	private final Map<Party, ElectionVoteCount> electionVoteCountMap = new LinkedHashMap<>();
	private final BigDecimal levelingSeatsVoteShareThreshold;

	public ElectionVoteCountFactory(BigDecimal levelingSeatsVoteShareThreshold) {
		this.levelingSeatsVoteShareThreshold = levelingSeatsVoteShareThreshold;
	}

	@Override
	public void electionVoteCountDelta(ElectionVoteCountEvent event) {
		Party party = event.getParty();
		if (electionVoteCountMap.containsKey(party)) {
			ElectionVoteCount electionVoteCount = electionVoteCountMap.get(party);
			electionVoteCount.incrementEarlyVotingBallots(event.getEarlyVotingBallots());
			electionVoteCount.incrementEarlyVotingModifiedBallots(event.getEarlyVotingModifiedBallots());
			electionVoteCount.incrementElectionDayBallots(event.getElectionDayBallots());
			electionVoteCount.incrementElectionDayModifiedBallots(event.getElectionDayModifiedBallots());
			electionVoteCount.incrementBaselineVotes(event.getBaselineVotes());
			electionVoteCount.incrementAddedVotes(event.getAddedVotes());
			electionVoteCount.incrementSubtractedVotes(event.getSubtractedVotes());
			electionVoteCount.incrementContestSeats(event.getContestSeats());
		} else {
			electionVoteCountMap.put(party, event.toElectionVoteCount());
		}
	}

	public void buildElectionVoteCounts() {
		int totalVotes = electionVoteCountMap.values().stream().mapToInt(ElectionVoteCount::getVotes).sum();
		electionVoteCountMap.values().forEach(electionVoteCount -> {
			electionVoteCount.setTotalVotes(totalVotes);
			electionVoteCount.updateEligibleForLevelingSeats(levelingSeatsVoteShareThreshold);
		});
		updateConsumers();
	}

	@Override
	protected void updateConsumer(ElectionVoteCountConsumer electionVoteCountConsumer) {
		electionVoteCountMap.values().forEach(electionVoteCountConsumer::consume);
	}

	@Override
	protected ElectionVoteCountFactory self() {
		return this;
	}
}
