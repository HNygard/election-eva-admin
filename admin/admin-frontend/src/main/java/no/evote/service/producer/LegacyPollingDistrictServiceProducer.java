package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.LegacyPollingDistrictService;

public class LegacyPollingDistrictServiceProducer extends AbstractServiceProducer<LegacyPollingDistrictService> {

	@Produces
	public LegacyPollingDistrictService getService(final InjectionPoint ip) {
		return produceService();
	}

}
