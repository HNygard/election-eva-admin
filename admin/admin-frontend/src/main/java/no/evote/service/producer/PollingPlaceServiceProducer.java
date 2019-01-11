package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.LegacyPollingPlaceService;

public class PollingPlaceServiceProducer extends AbstractServiceProducer<LegacyPollingPlaceService> {

	@Produces
	public LegacyPollingPlaceService getService(final InjectionPoint ip) {
		return produceService();
	}

}
