package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.BallotService;

public class BallotServiceProducer extends AbstractServiceProducer<BallotService> {

	@Produces
	public BallotService getService(final InjectionPoint ip) {
		return produceService();
	}

}
