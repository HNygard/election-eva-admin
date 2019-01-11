package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.LegacyContestService;

public class ContestServiceProducer extends AbstractServiceProducer<LegacyContestService> {

	@Produces
	public LegacyContestService getService(final InjectionPoint ip) {
		return produceService();
	}

}
