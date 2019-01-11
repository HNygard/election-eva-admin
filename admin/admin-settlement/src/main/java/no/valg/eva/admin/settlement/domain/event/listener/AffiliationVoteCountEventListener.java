package no.valg.eva.admin.settlement.domain.event.listener;

import no.valg.eva.admin.settlement.domain.event.AffiliationVoteCountEvent;

public interface AffiliationVoteCountEventListener extends EventListener {
	void affiliationVoteCountDelta(AffiliationVoteCountEvent event);
}
