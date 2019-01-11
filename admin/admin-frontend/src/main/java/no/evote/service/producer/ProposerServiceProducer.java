package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.ProposerService;

public class ProposerServiceProducer extends AbstractServiceProducer<ProposerService> {

	@Produces
	public ProposerService getService(final InjectionPoint ip) {
		return produceService();
	}

}
