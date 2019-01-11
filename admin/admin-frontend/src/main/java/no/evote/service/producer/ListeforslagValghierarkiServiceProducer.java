package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.valghierarki.service.ListeforslagValghierarkiService;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;

public class ListeforslagValghierarkiServiceProducer extends AbstractServiceProducer<ValghierarkiService> {

	@Produces
	@ListeforslagValghierarkiService
	public ValghierarkiService getListeforslagValghierarkiService(final InjectionPoint ip) {
		return produceService("ListeforslagValghierarkiService");
	}

}
