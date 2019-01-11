package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.SpecialPurposeReportService;

public class SpecialPurposeReportServiceProducer extends AbstractServiceProducer<SpecialPurposeReportService> {

	@Produces
	public SpecialPurposeReportService getService(final InjectionPoint ip) {
		return produceService();
	}

}

