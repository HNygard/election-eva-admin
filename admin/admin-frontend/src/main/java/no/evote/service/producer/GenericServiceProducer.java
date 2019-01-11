package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.GenericService;

public class GenericServiceProducer extends AbstractServiceProducer<GenericService> {

	@Produces
	public GenericService getService(final InjectionPoint ip) {
		return produceService();
	}

}

