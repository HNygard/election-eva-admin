package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.CastBallotBinaryDataService;

public class CastBallotBinaryDataServiceProducer extends AbstractServiceProducer<CastBallotBinaryDataService> {

	@Produces
	public CastBallotBinaryDataService getService(final InjectionPoint ip) {
		return produceService();
	}

}
