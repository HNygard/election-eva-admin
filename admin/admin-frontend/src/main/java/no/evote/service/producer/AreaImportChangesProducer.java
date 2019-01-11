package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.configuration.service.AreaImportChangesService;

@SuppressWarnings("unused")
public class AreaImportChangesProducer extends AbstractServiceProducer<AreaImportChangesService> {

	@SuppressWarnings("unused")
	@Produces
	public AreaImportChangesService getService(final InjectionPoint ip) {
		return produceService();
	}
}
