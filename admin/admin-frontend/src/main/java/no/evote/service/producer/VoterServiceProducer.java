package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.VoterService;

public class VoterServiceProducer extends AbstractServiceProducer<VoterService> {

	@Produces
	public VoterService getService(final InjectionPoint ip) {
		return produceService();
	}

}
