package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.security.SigningKeyService;

public class SigningKeyServiceProducer extends AbstractServiceProducer<SigningKeyService> {

	@Produces
	public SigningKeyService getService(final InjectionPoint ip) {
		return produceService();
	}

}
