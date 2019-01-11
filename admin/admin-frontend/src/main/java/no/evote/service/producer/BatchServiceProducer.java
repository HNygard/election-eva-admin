package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.BatchService;

public class BatchServiceProducer extends AbstractServiceProducer<BatchService> {

	@Produces
	public BatchService getService(final InjectionPoint ip) {
		return produceService();
	}

}

