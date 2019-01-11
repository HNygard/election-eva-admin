package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.CountingService;


public class CountingServiceProducer extends AbstractServiceProducer<CountingService> {

	@Produces
	public CountingService getService(final InjectionPoint ip) {
		return produceService();
	}

}

