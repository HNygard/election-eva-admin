package no.valg.eva.admin.settlement.domain.event.factory;

import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.consumer.AffiliationVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateSeatEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;

public class CandidateSeatEventsFromAffiliationVoteCounts extends EventFactory<CandidateSeatEventListener>implements AffiliationVoteCountConsumer {
	@Override
	public void consume(AffiliationVoteCount affiliationVoteCount) {
		Set<Candidate> candidates = affiliationVoteCount.getAffiliationCandidates();
		for (Candidate candidate : candidates) {
			fireEvent(candidate.getAffiliation(), candidate, affiliationVoteCount.getVotes());
		}
	}

	private void fireEvent(Affiliation affiliation, Candidate candidate, int dividend) {
		for (CandidateSeatEventListener eventListener : eventListeners) {
			eventListener.candidateSeatDelta(new CandidateSeatEvent(affiliation, candidate, dividend));
		}
	}
}
