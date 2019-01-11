package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.application.service.StatusService;

public class StatusServiceProducer extends AbstractServiceProducer<StatusService> {

	@Produces
	public StatusService getService(final InjectionPoint ip) {
		return produceService();
	}

}

