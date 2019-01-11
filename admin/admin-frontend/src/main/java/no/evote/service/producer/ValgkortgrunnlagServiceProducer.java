package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.configuration.service.ValgkortgrunnlagService;

public class ValgkortgrunnlagServiceProducer extends AbstractServiceProducer<ValgkortgrunnlagService> {

	@Produces
	public ValgkortgrunnlagService getService(final InjectionPoint ip) {
		return produceService();
	}

}

