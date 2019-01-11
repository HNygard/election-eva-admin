package no.evote.service.producer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.felles.bakgrunnsjobb.service.BakgrunnsjobbService;

public class BakgrunnsjobbServiceProducer extends AbstractServiceProducer<BakgrunnsjobbService> {

	@Produces
	public BakgrunnsjobbService getService(final InjectionPoint ip) {
		return produceService();
	}

}

