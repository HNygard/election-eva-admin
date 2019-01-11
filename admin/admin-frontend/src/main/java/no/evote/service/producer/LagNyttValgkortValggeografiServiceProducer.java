package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.valggeografi.service.LagNyttValgkortValggeografiService;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;

public class LagNyttValgkortValggeografiServiceProducer extends AbstractServiceProducer<ValggeografiService> {

	@Produces
	@LagNyttValgkortValggeografiService
	public ValggeografiService getSlettValgoppgjoerService(final InjectionPoint ip) {
		return produceService("LagNyttValgkortValggeografiService");
	}

}
