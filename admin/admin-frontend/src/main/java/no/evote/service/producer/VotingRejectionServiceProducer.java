package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.voting.VotingRejectionService;

public class VotingRejectionServiceProducer extends AbstractServiceProducer<VotingRejectionService> {

	@Produces
	public VotingRejectionService getService(final InjectionPoint ip) {
		return produceService();
	}

}
