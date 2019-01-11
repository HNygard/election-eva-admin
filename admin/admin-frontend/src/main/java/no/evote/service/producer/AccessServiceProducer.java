package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.rbac.service.AccessService;

public class AccessServiceProducer extends AbstractServiceProducer<AccessService> {

	@Produces
	public AccessService getService(final InjectionPoint ip) {
		return produceService();
	}

}
