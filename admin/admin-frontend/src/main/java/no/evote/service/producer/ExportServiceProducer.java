package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.ExportService;

public class ExportServiceProducer extends AbstractServiceProducer<ExportService> {

	@Produces
	public ExportService getService(final InjectionPoint ip) {
		return produceService();
	}

}

