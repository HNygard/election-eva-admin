package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.settlement.service.LevelingSeatSettlementService;

public class LevelingSeatSettlementServiceProducer extends AbstractServiceProducer<LevelingSeatSettlementService> {

	@Produces
	public LevelingSeatSettlementService getService(final InjectionPoint ip) {
		return produceService();
	}
	
}
