package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;

public class ValgnattReportServiceProducer extends AbstractServiceProducer<ValgnattReportService> {

	@Produces
	public ValgnattReportService getService(final InjectionPoint ip) {
		return produceService();
	}

}
