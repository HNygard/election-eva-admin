package no.valg.eva.admin.frontend.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.rapport.service.RapportService;

public class RapportServiceProducer extends AbstractServiceProducer<RapportService> {

	@Produces
	public RapportService getService(final InjectionPoint ip) {
		return produceService();
	}
}
