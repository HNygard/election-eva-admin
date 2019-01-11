package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.rbac.service.AdminOperatorService;

public class AdminOperatorServiceProducer extends AbstractServiceProducer<AdminOperatorService> {

	@Produces
	public AdminOperatorService getService(final InjectionPoint ip) {
		return produceService();
	}

}

