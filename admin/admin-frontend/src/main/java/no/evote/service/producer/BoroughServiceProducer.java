package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.BoroughService;

public class BoroughServiceProducer extends AbstractServiceProducer<BoroughService> {

	@Produces
	public BoroughService getService(final InjectionPoint ip) {
		return produceService();
	}

}
