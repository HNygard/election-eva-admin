package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.rbac.service.ImportOperatorService;

public class ImportOperatorServiceProducer extends AbstractServiceProducer<ImportOperatorService> {

	@Produces
	public ImportOperatorService getService(final InjectionPoint ip) {
		return produceService();
	}

}

