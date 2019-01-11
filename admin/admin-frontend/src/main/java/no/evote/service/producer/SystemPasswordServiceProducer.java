package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.security.SystemPasswordService;

public class SystemPasswordServiceProducer extends AbstractServiceProducer<SystemPasswordService> {

	@Produces
	public SystemPasswordService getService(final InjectionPoint ip) {
		return produceService();
	}

}
