package no.valg.eva.admin.frontend.service.producer;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.voter.service.VoterAuditService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class VoterAuditServiceProducer extends AbstractServiceProducer<VoterAuditService> {

    private static final long serialVersionUID = 2705962750730440152L;

    @Produces
    public VoterAuditService getService(final InjectionPoint ip) {
        return produceService();
    }

}
