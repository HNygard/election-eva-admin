package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.CountryService;

public class CountryServiceProducer extends AbstractServiceProducer<CountryService> {

	@Produces
	public CountryService getService(final InjectionPoint ip) {
		return produceService();
	}

}

