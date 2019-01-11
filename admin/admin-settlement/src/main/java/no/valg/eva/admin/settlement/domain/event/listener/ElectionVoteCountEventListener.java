package no.valg.eva.admin.settlement.domain.event.listener;

import no.valg.eva.admin.settlement.domain.event.ElectionVoteCountEvent;

public interface ElectionVoteCountEventListener extends EventListener {
	void electionVoteCountDelta(ElectionVoteCountEvent event);
}
