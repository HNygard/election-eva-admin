package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.rbac.service.UserDataService;

public class UserDataServiceProducer extends AbstractServiceProducer<UserDataService> {
	@Produces
	public UserDataService getService(final InjectionPoint ip) {
		return produceService();
	}
}
