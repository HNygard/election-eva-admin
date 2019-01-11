package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.MvElectionService;

public class MvElectionServiceProducer extends AbstractServiceProducer<MvElectionService> {

	@Produces
	public MvElectionService getService(final InjectionPoint ip) {
		return produceService();
	}

}
