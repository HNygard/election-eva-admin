package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.LevelingSeat;

public interface LevelingSeatConsumer extends EntityConsumer {
	void consume(LevelingSeat levelingSeat);
}
