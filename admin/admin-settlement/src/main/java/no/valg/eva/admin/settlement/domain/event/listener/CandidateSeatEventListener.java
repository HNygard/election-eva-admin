package no.valg.eva.admin.settlement.domain.event.listener;

import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;

public interface CandidateSeatEventListener extends EventListener {
	void candidateSeatDelta(CandidateSeatEvent event);
}
