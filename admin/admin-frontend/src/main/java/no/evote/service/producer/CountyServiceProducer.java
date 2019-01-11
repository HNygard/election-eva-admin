package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.CountyService;

public class CountyServiceProducer extends AbstractServiceProducer<CountyService> {

	@Produces
	public CountyService getService(final InjectionPoint ip) {
		return produceService();
	}

}

