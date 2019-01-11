package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.opptelling.service.OpptellingService;

public class OpptellingServiceProducer extends AbstractServiceProducer<OpptellingService> {

	@Produces
	public OpptellingService getService(final InjectionPoint ip) {
		return produceService();
	}

}
