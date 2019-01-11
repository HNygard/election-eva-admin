package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.ElectionVoteCountCategoryService;

public class ElectionVoteCountCategoryServiceProducer extends AbstractServiceProducer<ElectionVoteCountCategoryService> {

	@Produces
	public ElectionVoteCountCategoryService getService(final InjectionPoint ip) {
		return produceService();
	}

}
