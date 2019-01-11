package no.valg.eva.admin.settlement.domain.event.listener;

import no.valg.eva.admin.settlement.domain.event.LevelingSeatQuotientEvent;

public interface LevelingSeatQuotientEventListener extends EventListener {
	void levelingSeatQuotientDelta(LevelingSeatQuotientEvent event);
}
