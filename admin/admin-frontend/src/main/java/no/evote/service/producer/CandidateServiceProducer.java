package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.CandidateService;

public class CandidateServiceProducer extends AbstractServiceProducer<CandidateService> {

	@Produces
	public CandidateService getService(final InjectionPoint ip) {
		return produceService();
	}

}
