package no.valg.eva.admin.frontend.service.producer;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class VotingRegistrationServiceProducer extends AbstractServiceProducer<VotingRegistrationService> {

    private static final long serialVersionUID = 3656876140465022106L;

    @Produces
    public VotingRegistrationService getService(final InjectionPoint ip) {
        return produceService();
    }

}
