package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.EligibilityService;

public class EligibilityServiceProducer extends AbstractServiceProducer<EligibilityService> {

	@Produces
	public EligibilityService getService(final InjectionPoint ip) {
		return produceService();
	}

}

