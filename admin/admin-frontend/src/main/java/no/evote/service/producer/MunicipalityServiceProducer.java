package no.evote.service.producer;

import no.valg.eva.admin.common.configuration.service.MunicipalityService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class MunicipalityServiceProducer extends AbstractServiceProducer<MunicipalityService> {

    private static final long serialVersionUID = -9002427450971256263L;

    @Produces
    public MunicipalityService getService(final InjectionPoint ip) {
        return produceService();
    }

}
