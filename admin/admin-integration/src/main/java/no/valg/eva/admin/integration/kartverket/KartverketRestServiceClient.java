package no.valg.eva.admin.integration.kartverket;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.integration.kartverket.model.KartverketAddressSearchResult;
import no.valg.eva.admin.integration.kartverket.model.KartverketLocationSearchResult;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import static no.valg.eva.admin.integration.ResteasyProxyClientFactory.buildHttpProxyClient;
import static no.valg.eva.admin.integration.kartverket.KartverketConstants.KARTVERKET_WS_ENDPOINT;

@Singleton
@Lock(LockType.READ)
@NoArgsConstructor
public class KartverketRestServiceClient {

    private KartverketRestService kartverketRestService;

    @PostConstruct
    public void postConstruct() {
        ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(instance);
        instance.registerProvider(ResteasyJackson2Provider.class);

        ResteasyClient client = buildHttpProxyClient();
        ResteasyWebTarget httpsTarget = client.target(KARTVERKET_WS_ENDPOINT);
        kartverketRestService = httpsTarget.proxy(KartverketRestService.class);
    }

    public KartverketAddressSearchResult searchAddress(String searchString, int maxResultsPerPage, int pageNumber) {
        return kartverketRestService.searchAddress(searchString, maxResultsPerPage, pageNumber);
    }

    public KartverketLocationSearchResult searchLocation(String searchString, String municipalityCountyList, int maxResultsPerPage, int pageNumber) {
        return kartverketRestService.searchLocation(searchString, municipalityCountyList, maxResultsPerPage, pageNumber);
    }
}
