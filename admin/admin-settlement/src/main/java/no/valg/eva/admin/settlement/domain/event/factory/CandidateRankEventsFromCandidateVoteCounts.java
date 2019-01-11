package no.valg.eva.admin.settlement.domain.event.factory;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.consumer.CandidateVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;

public class CandidateRankEventsFromCandidateVoteCounts extends EventFactory<CandidateRankEventListener>implements CandidateVoteCountConsumer {
	@Override
	public void consume(CandidateVoteCount candidateVoteCount) {
		Candidate candidate = candidateVoteCount.getCandidate();
		Affiliation candidateAffiliation = candidateVoteCount.getCandidateAffiliation();
		BigDecimal candidateVotes = candidateVoteCount.getVotes();
		fireEvent(candidate, candidateAffiliation, candidateVotes);
	}

	private void fireEvent(Candidate candidate, Affiliation affiliation, BigDecimal votes) {
		CandidateRankEvent event = new CandidateRankEvent(candidate, affiliation, votes, null);
		for (CandidateRankEventListener eventListener : eventListeners) {
			eventListener.candidateRankDelta(event);
		}
	}
}
