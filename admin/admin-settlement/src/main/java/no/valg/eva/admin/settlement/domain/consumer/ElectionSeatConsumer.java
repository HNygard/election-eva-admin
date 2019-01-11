package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.ElectionSeat;

public interface ElectionSeatConsumer extends EntityConsumer {
	void consume(ElectionSeat electionSeat);
}
