package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.LegacyUserDataService;

public class LegacyUserDataServiceProducer extends AbstractServiceProducer<LegacyUserDataService> {
	@Produces
	public LegacyUserDataService getService(final InjectionPoint ip) {
		return produceService();
	}

}
