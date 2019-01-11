package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;

public class ValghierarkiServiceProducer extends AbstractServiceProducer<ValghierarkiService> {

	@Produces
	public ValghierarkiService getService(final InjectionPoint ip) {
		return produceService();
	}

}
