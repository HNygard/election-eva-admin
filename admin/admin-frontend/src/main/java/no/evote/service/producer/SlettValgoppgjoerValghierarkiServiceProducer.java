package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.valghierarki.service.SlettValgoppgjoerValghierarkiService;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;

public class SlettValgoppgjoerValghierarkiServiceProducer extends AbstractServiceProducer<ValghierarkiService> {

	@Produces
	@SlettValgoppgjoerValghierarkiService
	public ValghierarkiService getSlettValgoppgjoerService(final InjectionPoint ip) {
		return produceService("SlettValgoppgjoerValghierarkiService");
	}

}
