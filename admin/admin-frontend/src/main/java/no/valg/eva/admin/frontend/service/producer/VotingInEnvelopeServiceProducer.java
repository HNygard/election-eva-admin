package no.valg.eva.admin.frontend.service.producer;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class VotingInEnvelopeServiceProducer extends AbstractServiceProducer<VotingInEnvelopeService> {

    private static final long serialVersionUID = 682084760589723885L;

    @Produces
    public VotingInEnvelopeService getService(final InjectionPoint ip) {
        return produceService();
    }

}
