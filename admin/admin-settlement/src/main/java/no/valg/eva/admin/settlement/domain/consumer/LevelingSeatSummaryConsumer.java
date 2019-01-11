package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;

public interface LevelingSeatSummaryConsumer extends EntityConsumer {
	void consume(LevelingSeatSummary levelingSeatSummary);
}
