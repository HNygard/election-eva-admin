package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.AreaImportService;

public class AreaImportServiceProducer extends AbstractServiceProducer<AreaImportService> {

	@Produces
	public AreaImportService getService(final InjectionPoint ip) {
		return produceService();
	}

}
