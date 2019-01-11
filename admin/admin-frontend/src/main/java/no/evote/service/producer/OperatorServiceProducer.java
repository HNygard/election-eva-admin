package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.rbac.OperatorService;

public class OperatorServiceProducer extends AbstractServiceProducer<OperatorService> {

	@Produces
	public OperatorService getService(final InjectionPoint ip) {
		return produceService();
	}

}
