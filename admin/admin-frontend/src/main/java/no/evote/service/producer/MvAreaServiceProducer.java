package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.MvAreaService;

public class MvAreaServiceProducer extends AbstractServiceProducer<MvAreaService> {

	@Produces
	public MvAreaService getService(final InjectionPoint ip) {
		return produceService();
	}

}
