package no.valg.eva.admin.settlement.domain.event.listener;

import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;

public interface CandidateRankEventListener extends EventListener {
	void candidateRankDelta(CandidateRankEvent event);
}
