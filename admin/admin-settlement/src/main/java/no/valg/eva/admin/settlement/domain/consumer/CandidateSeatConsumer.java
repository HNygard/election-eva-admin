package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.CandidateSeat;

public interface CandidateSeatConsumer extends EntityConsumer {
	void consume(CandidateSeat candidateSeat);
}
