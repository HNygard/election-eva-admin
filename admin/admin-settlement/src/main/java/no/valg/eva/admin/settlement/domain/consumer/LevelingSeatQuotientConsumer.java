package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;

public interface LevelingSeatQuotientConsumer extends EntityConsumer {
	void consume(LevelingSeatQuotient levelingSeatQuotient);
}
