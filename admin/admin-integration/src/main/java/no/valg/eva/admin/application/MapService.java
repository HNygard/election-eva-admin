package no.valg.eva.admin.application;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.application.map.AddressSearch;
import no.valg.eva.admin.application.map.AddressSearchResult;
import no.valg.eva.admin.application.map.LocationSearch;
import no.valg.eva.admin.application.map.LocationSearchResult;
import no.valg.eva.admin.integration.kartverket.KartverketService;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;


@Stateless(name = "MapService")
@NoArgsConstructor
@Default
public class MapService {

    private KartverketService kartverketService;

    @Inject
    public MapService(KartverketService kartverketService) {
        this.kartverketService = kartverketService;
    }

    public AddressSearchResult addressSearch(AddressSearch addressSearch) {
        return kartverketService.addressSearch(addressSearch);
    }

    public LocationSearchResult locationSearch(LocationSearch locationSearch) {
        return kartverketService.locationSearch(locationSearch);
    }
}
