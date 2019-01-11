package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.ModifiedBallotService;

public class ModifiedBallotServiceProducer extends AbstractServiceProducer<ModifiedBallotService> {

	@Produces
	public ModifiedBallotService getService(final InjectionPoint ip) {
		return produceService();
	}

}
