package no.valg.eva.admin.settlement.domain.event.listener;

import no.valg.eva.admin.settlement.domain.event.CandidateVoteCountEvent;

public interface CandidateVoteCountEventListener extends EventListener {
	void candidateVoteCountDelta(CandidateVoteCountEvent event);
}
