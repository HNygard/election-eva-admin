package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.CastBallotService;

public class CastBallotServiceProducer extends AbstractServiceProducer<CastBallotService> {

	@Produces
	public CastBallotService getService(final InjectionPoint ip) {
		return produceService();
	}

}
