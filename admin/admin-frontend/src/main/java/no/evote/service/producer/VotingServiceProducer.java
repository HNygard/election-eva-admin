package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.voting.VotingService;

public class VotingServiceProducer extends AbstractServiceProducer<VotingService> {

	@Produces
	@EjbProxy
	public VotingService getService(final InjectionPoint ip) {
		return produceService();
	}

}
