package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.PartyService;

public class PartyServiceProducer extends AbstractServiceProducer<PartyService> {

	@Produces
	public PartyService getService(final InjectionPoint ip) {
		return produceService();
	}

}
