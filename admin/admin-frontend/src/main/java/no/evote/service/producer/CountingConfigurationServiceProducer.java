package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.CountingConfigurationService;


public class CountingConfigurationServiceProducer extends AbstractServiceProducer<CountingConfigurationService> {

	@Produces
	public CountingConfigurationService getService(final InjectionPoint ip) {
		return produceService();
	}

}

