package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;

public interface ElectionVoteCountConsumer extends EntityConsumer {
	void consume(ElectionVoteCount electionVoteCount);
}
