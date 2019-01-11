package no.evote.service.producer;

import no.valg.eva.admin.common.configuration.service.ElectionEventService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class ElectionEventServiceProducer extends AbstractServiceProducer<ElectionEventService> {

	@Produces
	public ElectionEventService getService(final InjectionPoint ip) {
		return produceService();
	}

}

