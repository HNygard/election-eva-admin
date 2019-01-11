package no.evote.service.producer;

import no.valg.eva.admin.configuration.application.ResponsibilityValidationService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class ResponsibilityValidationServiceProducer extends AbstractServiceProducer<ResponsibilityValidationService> {

    @Produces
    public ResponsibilityValidationService getService(final InjectionPoint ip) {
        return produceService();
    }
}
