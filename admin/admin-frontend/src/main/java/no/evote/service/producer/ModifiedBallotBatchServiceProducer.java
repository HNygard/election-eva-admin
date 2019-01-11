package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;

public class ModifiedBallotBatchServiceProducer extends AbstractServiceProducer<ModifiedBallotBatchService> {

	@Produces
	public ModifiedBallotBatchService getService(final InjectionPoint ip) {
		return produceService();
	}

}
