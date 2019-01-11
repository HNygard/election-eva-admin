package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.configuration.service.PartiService;

public class PartiServiceProducer extends AbstractServiceProducer<PartiService> {

	@Produces
	public PartiService getService(final InjectionPoint ip) {
		return produceService();
	}

}
