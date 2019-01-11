package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.ContestAreaService;

public class ContestAreaServiceProducer extends AbstractServiceProducer<ContestAreaService> {

	@Produces
	public ContestAreaService getService(final InjectionPoint ip) {
		return produceService();
	}

}

