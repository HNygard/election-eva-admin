package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.AffiliationService;

public class AffiliationServiceProducer extends AbstractServiceProducer<AffiliationService> {

	@Produces
	public AffiliationService getService(final InjectionPoint ip) {
		return produceService();
	}

}
