package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.reporting.service.JasperReportService;

public class JasperReportServiceProducer extends AbstractServiceProducer<JasperReportService> {

	@Produces
	public JasperReportService getService(final InjectionPoint ip) {
		return produceService();
	}

}
