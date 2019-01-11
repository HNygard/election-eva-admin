package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.configuration.service.OpptellingskategoriService;

public class OpptellingskategoriServiceProducer extends AbstractServiceProducer<OpptellingskategoriService> {

	@Produces
	public OpptellingskategoriService getService(final InjectionPoint ip) {
		return produceService();
	}

}
