package no.evote.service.producer;

import no.valg.eva.admin.common.configuration.service.ExportCandidateVotesService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class ExportCandidateVotesServiceProducer extends AbstractServiceProducer<ExportCandidateVotesService> {

    @Produces
    public ExportCandidateVotesService getService(final InjectionPoint ip) {
        return produceService();
    }

}
