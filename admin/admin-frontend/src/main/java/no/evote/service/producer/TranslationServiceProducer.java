package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.TranslationService;

public class TranslationServiceProducer extends AbstractServiceProducer<TranslationService> {

	@Produces
	public TranslationService getService(final InjectionPoint ip) {
		return produceService();
	}

}
