package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.evote.service.configuration.ImportElectoralRollService;

public class ImportElectoralRollServiceProducer extends AbstractServiceProducer<ImportElectoralRollService> {

	@Produces
	public ImportElectoralRollService getService(final InjectionPoint ip) {
		return produceService();
	}

}
