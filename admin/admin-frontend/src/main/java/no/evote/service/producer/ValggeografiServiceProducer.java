package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;

public class ValggeografiServiceProducer extends AbstractServiceProducer<ValggeografiService> {

	@Produces
	public ValggeografiService getService(final InjectionPoint ip) {
		return produceService();
	}

}
