package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.CountingOverviewService;

public class CountingOverviewServiceProducer extends AbstractServiceProducer<CountingOverviewService> {

	@Produces
	public CountingOverviewService getService(final InjectionPoint ip) {
		return produceService();
	}

}
