package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.CandidateRank;

public interface CandidateRankConsumer extends EntityConsumer {
	void consume(CandidateRank candidateRank);
}
