package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.counting.ContestReportService;

public class ContestReportServiceProducer extends AbstractServiceProducer<ContestReportService> {

	@Produces
	public ContestReportService getService(final InjectionPoint ip) {
		return produceService();
	}

}
