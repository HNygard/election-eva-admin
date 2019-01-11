package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.BinaryDataService;

public class BinaryDataServiceProducer extends AbstractServiceProducer<BinaryDataService> {

	@Produces
	public BinaryDataService getService(final InjectionPoint ip) {
		return produceService();
	}

}

