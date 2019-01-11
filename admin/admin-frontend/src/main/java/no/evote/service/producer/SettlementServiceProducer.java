package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.settlement.service.SettlementService;

public class SettlementServiceProducer extends AbstractServiceProducer<SettlementService> {

	@Produces
	public SettlementService getService(final InjectionPoint ip) {
		return produceService();
	}

}
