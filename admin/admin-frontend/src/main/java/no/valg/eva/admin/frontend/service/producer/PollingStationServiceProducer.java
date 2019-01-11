package no.valg.eva.admin.frontend.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.configuration.service.PollingStationService;

public class PollingStationServiceProducer extends AbstractServiceProducer<PollingStationService> {

	@Produces
	public PollingStationService getService(final InjectionPoint ip) {
		return produceService();
	}

}
