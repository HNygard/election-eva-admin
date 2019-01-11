package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.common.counting.service.AntallStemmesedlerLagtTilSideService;

public class AntallStemmesedlerLagtTilSideServiceProducer extends AbstractServiceProducer<AntallStemmesedlerLagtTilSideService> {

	@Produces
	public AntallStemmesedlerLagtTilSideService getService(final InjectionPoint ip) {
		return produceService();
	}

}
