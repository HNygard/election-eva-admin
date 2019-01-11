package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.VoteCountCategoryService;

public class VoteCountCategoryServiceProducer extends AbstractServiceProducer<VoteCountCategoryService> {

	@Produces
	public VoteCountCategoryService getService(final InjectionPoint ip) {
		return produceService();
	}

}
