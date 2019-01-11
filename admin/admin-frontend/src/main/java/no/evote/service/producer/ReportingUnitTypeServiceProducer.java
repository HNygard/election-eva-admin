package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.ReportingUnitTypeService;

public class ReportingUnitTypeServiceProducer extends AbstractServiceProducer<ReportingUnitTypeService> {

	@Produces
	public ReportingUnitTypeService getService(final InjectionPoint ip) {
		return produceService();
	}

}
