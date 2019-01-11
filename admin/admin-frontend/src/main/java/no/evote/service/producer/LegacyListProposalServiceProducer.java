package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.LegacyListProposalService;

public class LegacyListProposalServiceProducer extends AbstractServiceProducer<LegacyListProposalService> {

	@Produces
	public LegacyListProposalService getService(final InjectionPoint ip) {
		return produceService();
	}

}
