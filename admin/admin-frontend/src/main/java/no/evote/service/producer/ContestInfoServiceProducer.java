package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.ContestInfoService;


public class ContestInfoServiceProducer extends AbstractServiceProducer<ContestInfoService> {

	@Produces
	public ContestInfoService getService(final InjectionPoint ip) {
		return produceService();
	}

}

