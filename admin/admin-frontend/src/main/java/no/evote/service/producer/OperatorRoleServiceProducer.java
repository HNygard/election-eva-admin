package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.rbac.OperatorRoleService;

public class OperatorRoleServiceProducer extends AbstractServiceProducer<OperatorRoleService> {

	@Produces
	public OperatorRoleService getService(final InjectionPoint ip) {
		return produceService();
	}

}
