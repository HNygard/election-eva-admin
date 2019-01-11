package no.valg.eva.admin.frontend.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;

public class ElectionGroupServiceProducer extends AbstractServiceProducer<ElectionGroupService> {

	@Produces
	public ElectionGroupService getService(final InjectionPoint ip) {
		return produceService();
	}

}
