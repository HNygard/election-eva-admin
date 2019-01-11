package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.rbac.RoleService;

public class RoleServiceProducer extends AbstractServiceProducer<RoleService> {

	@Produces
	public RoleService getService(final InjectionPoint ip) {
		return produceService();
	}

}
