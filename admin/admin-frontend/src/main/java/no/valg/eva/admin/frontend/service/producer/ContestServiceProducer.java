package no.valg.eva.admin.frontend.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.configuration.service.ContestService;

public class ContestServiceProducer extends AbstractServiceProducer<ContestService> {

	@Produces
	public ContestService getService(final InjectionPoint ip) {
		return produceService();
	}

}
