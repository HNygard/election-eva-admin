package no.valg.eva.admin.frontend.service.producer;

import no.evote.service.producer.AbstractServiceProducer;
import no.valg.eva.admin.common.configuration.service.ScanningConfigService;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class ScanningConfigServiceProducer extends AbstractServiceProducer<ScanningConfigService> {
    @Produces
    public ScanningConfigService getService(final InjectionPoint ip) {
        return produceService();
    }
}
