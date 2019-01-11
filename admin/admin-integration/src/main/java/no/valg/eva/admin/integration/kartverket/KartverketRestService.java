package no.valg.eva.admin.integration.kartverket;

import no.valg.eva.admin.integration.kartverket.model.KartverketAddressSearchResult;
import no.valg.eva.admin.integration.kartverket.model.KartverketLocationSearchResult;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static no.valg.eva.admin.integration.kartverket.KartverketConstants.ADRESSE_WS_ADRESSE_SOK;
import static no.valg.eva.admin.integration.kartverket.KartverketConstants.ADRESSE_WS_LOCATION_SOK;
import static no.valg.eva.admin.integration.kartverket.KartverketConstants.APPLICATION_JSON_CHARSET_UTF_8;

public interface KartverketRestService {

    @GET
    @Path(ADRESSE_WS_ADRESSE_SOK)
    @Produces(APPLICATION_JSON_CHARSET_UTF_8)
    @Consumes(APPLICATION_JSON_CHARSET_UTF_8)
    @GZIP
    KartverketAddressSearchResult searchAddress(@QueryParam("sokestreng") String addressString,
                                                @QueryParam("antPerSide") int maxResultsPerPage,
                                                @QueryParam("side") int pageNumber);

    @GET
    @Path(ADRESSE_WS_LOCATION_SOK)
    @Produces(APPLICATION_JSON_CHARSET_UTF_8)
    @Consumes(APPLICATION_JSON_CHARSET_UTF_8)
    KartverketLocationSearchResult searchLocation(@QueryParam("navn") String locationString,
                                                  @QueryParam("fylkeKommuneListe") String municipalityCountyList,
                                                  @QueryParam("antPerSide") int maxResultsPerPage,
                                                  @QueryParam("side") int pageNumber);
}
