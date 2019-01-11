package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.CandidateVoteCount;

public interface CandidateVoteCountConsumer extends EntityConsumer {
	void consume(CandidateVoteCount candidateVoteCount);
}
