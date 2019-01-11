package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.LinkedHashMap;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.settlement.domain.consumer.AffiliationVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.AffiliationVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.AffiliationVoteCountEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;

public class AffiliationVoteCountFactory extends EntityFactory<AffiliationVoteCountFactory, AffiliationVoteCountConsumer>
		implements AffiliationVoteCountEventListener {
	private final Map<Affiliation, AffiliationVoteCount> affiliationVoteCountMap = new LinkedHashMap<>();

	@Override
	public void affiliationVoteCountDelta(AffiliationVoteCountEvent event) {
		Affiliation affiliation = event.getAffiliation();
		if (affiliationVoteCountMap.containsKey(affiliation)) {
			AffiliationVoteCount affiliationVoteCount = affiliationVoteCountMap.get(affiliation);
			affiliationVoteCount.incrementBallots(event.getBallots());
			affiliationVoteCount.incrementModifiedBallots(event.getModifiedBallots());
			affiliationVoteCount.incrementEarlyVotingBallots(event.getEarlyVotingBallots());
			affiliationVoteCount.incrementEarlyVotingModifiedBallots(event.getEarlyVotingModifiedBallots());
			affiliationVoteCount.incrementElectionDayBallots(event.getElectionDayBallots());
			affiliationVoteCount.incrementElectionDayModifiedBallots(event.getElectionDayModifiedBallots());
			affiliationVoteCount.incrementBaselineVotes(event.getBaselineVotes());
			affiliationVoteCount.incrementAddedVotes(event.getAddedVotes());
			affiliationVoteCount.incrementSubtractedVotes(event.getSubtractedVotes());
		} else {
			AffiliationVoteCount affiliationVoteCount = event.toAffiliationVoteCount();
			affiliationVoteCountMap.put(affiliation, affiliationVoteCount);
		}
	}

	public void buildAffiliationVoteCounts() {
		updateConsumers();
	}

	@Override
	protected void updateConsumer(AffiliationVoteCountConsumer affiliationVoteCountConsumer) {
		affiliationVoteCountMap.values().forEach(affiliationVoteCountConsumer::consume);
	}

	@Override
	protected AffiliationVoteCountFactory self() {
		return this;
	}
}
