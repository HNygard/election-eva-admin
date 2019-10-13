package no.valg.eva.admin.integration.kartverket;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import no.valg.eva.admin.application.map.Address;
import no.valg.eva.admin.application.map.AddressSearch;
import no.valg.eva.admin.application.map.AddressSearchResult;
import no.valg.eva.admin.application.map.GpsCoordinates;
import no.valg.eva.admin.application.map.Location;
import no.valg.eva.admin.application.map.LocationSearch;
import no.valg.eva.admin.application.map.LocationSearchResult;
import no.valg.eva.admin.common.configuration.model.County;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.integration.kartverket.model.KartverketAddress;
import no.valg.eva.admin.integration.kartverket.model.KartverketAddressSearchResult;
import no.valg.eva.admin.integration.kartverket.model.KartverketLocation;
import no.valg.eva.admin.integration.kartverket.model.KartverketLocationSearchResult;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Stateless
@NoArgsConstructor
@Log4j
@Default
public class KartverketService {

    private static final int FIRST_PAGE = 0;
    private static final int MAX_RESULTS = 100;
    private static final int LNG_ZONE_33 = 33;
    private static final char LAT_ZONE_W = 'W';
    private static final int THREE_DIGITS = 3;

    private KartverketRestServiceClient kartverketRestServiceClient;

    @Inject
    public KartverketService(KartverketRestServiceClient kartverketRestServiceClient) {
        this.kartverketRestServiceClient = kartverketRestServiceClient;
    }

    public AddressSearchResult addressSearch(AddressSearch addressSearch) {
        if (!addressSearch.valid()) {
            return AddressSearchResult.builder().build();
        }
        String searchString = createAddressSearchString(addressSearch);
        log.trace("Søkestreng: " + searchString);
        long start = System.currentTimeMillis();
        KartverketAddressSearchResult kartverketAddressSearchResult = kartverketRestServiceClient.searchAddress(searchString, MAX_RESULTS, FIRST_PAGE);
        log.trace("Kall til kartverket: " + (System.currentTimeMillis() - start) + "ms");
        log.trace("Search hits: " + kartverketAddressSearchResult.getTotaltAntallTreff());
        return mapToInternalAddressSearchResult(kartverketAddressSearchResult, addressSearch);
    }

    private String createAddressSearchString(AddressSearch addressSearch) {
        StringBuilder stringBuilder = new StringBuilder(addressSearch.getStreetName());
        if (isNotBlank(addressSearch.getHouseNumber())) {
            stringBuilder.append(" ").append(addressSearch.getHouseNumber());
            if (isNotBlank(addressSearch.getHouseLetter())) {
                stringBuilder.append(" ").append(addressSearch.getHouseLetter());
            }
        }
        if (addressSearch.getMunicipality() != null && isNotBlank(addressSearch.getMunicipality().getName())) {
            stringBuilder.append(",").append(addressSearch.getMunicipality().getName());
        }
        return stringBuilder.toString();
    }

    private AddressSearchResult mapToInternalAddressSearchResult(KartverketAddressSearchResult kartverketAddressSearchResult, AddressSearch addressSearch) {
        List<Address> addresses = collectAddresses(kartverketAddressSearchResult, addressSearch);
        return AddressSearchResult.builder()
                .addresses(addresses)
                .build();
    }

    private List<Address> collectAddresses(KartverketAddressSearchResult kartverketAddressSearchResult, AddressSearch addressSearch) {
        return kartverketAddressSearchResult.getAdresser().stream()
                .map(this::ensureFourDigitPostalCode)
                .filter(kartverketAddress -> addessOfInterest(kartverketAddress, addressSearch))
                .limit(addressSearch.getMaxResults())
                .map(this::buildAddress)
                .collect(toList());
    }
    
    private KartverketAddress ensureFourDigitPostalCode(KartverketAddress ka) {
        if (ka.getPostnr() != null && ka.getPostnr().length() == THREE_DIGITS) {
            ka.setPostnr("0"+ka.getPostnr());
        }
        return ka;
    }

    private boolean addessOfInterest(KartverketAddress kartverketAddress, AddressSearch addressSearch) {
        if (null == kartverketAddress.getAdressenavn()) {
            return false;
        }
        if (notMatching(addressSearch.getStreetName(), kartverketAddress.getAdressenavn())) {
            log.trace("Removed address with uninteresting street name: " + kartverketAddress);
            return false;
        }
        if (notMatching(addressSearch.getHouseNumber(), kartverketAddress.getHusnr())) {
            log.trace("Removed address with uninteresting house number: " + kartverketAddress);
            return false;
        }
        if (notMatching(addressSearch.getHouseLetter(), kartverketAddress.getBokstav())) {
            log.trace("Removed address with uninteresting house letter: " + kartverketAddress);
            return false;
        }
        if (notMatching(addressSearch.getPostalCode(), kartverketAddress.getPostnr())) {
            log.trace("Removed address with uninteresting postal code: " + kartverketAddress);
            return false;
        }
        if (notMatching(addressSearch.getPostTown(), kartverketAddress.getPoststed())) {
            log.trace("Removed address with uninteresting post town: " + kartverketAddress);
            return false;
        }
        if (null != addressSearch.getMunicipality() && notMatching(addressSearch.getMunicipality().getName(), kartverketAddress.getKommunenavn())) {
            log.trace("Removed address with uninteresting municipality name: " + kartverketAddress);
            return false;
        }
        return true;
    }

    private boolean notMatching(String addressSearchField, String kartverketAddressFiled) {
        return isNotBlank(addressSearchField) && (isBlank(kartverketAddressFiled) || !kartverketAddressFiled.toLowerCase().startsWith(addressSearchField.toLowerCase()));
    }

    private Address buildAddress(KartverketAddress kartverketAddress) {
        return Address.builder()
                .streetName(kartverketAddress.getAdressenavn())
                .houseNumber(kartverketAddress.getHusnr())
                .houseLetter(kartverketAddress.getBokstav())
                .postalCode(kartverketAddress.getPostnr())
                .postTown(kartverketAddress.getPoststed())
                .gpsCoordinates(buildGpsCoordinates(kartverketAddress))
                .municipality(buildMunicipality(kartverketAddress))
                .build();
    }

    private GpsCoordinates buildGpsCoordinates(KartverketAddress kartverketAddress) {
        return new GpsCoordinates(parseDouble(kartverketAddress.getNord()), parseDouble(kartverketAddress.getAust()));
    }

    private double parseDouble(String number) {
        double aDouble = 0;
        try {
            aDouble = Double.parseDouble(number);
        } catch (NullPointerException | NumberFormatException e) {
            log.error("Kartverket returnerte ikke en forventet verdi for koordinater", e);
        }
        return aDouble;
    }

    private Municipality buildMunicipality(KartverketAddress kartverketAddress) {
        return Municipality.builder()
                .name(kartverketAddress.getKommunenavn())
                .id(kartverketAddress.getKommunenr())
                .build();
    }

    public LocationSearchResult locationSearch(LocationSearch locationSearch) {
        if (!locationSearch.valid()) {
            return LocationSearchResult.builder().build();
        }
        String searchString = locationSearch.getLocationName();
        String municipalityCountyList = createMunicipalityCountyList(locationSearch);
        int maxResultsPerPage = locationSearch.getMaxResultsPerPage();
        int pageNumber = locationSearch.getPageNumber();
        KartverketLocationSearchResult kartverketLocationSearchResult = kartverketRestServiceClient.searchLocation(searchString, municipalityCountyList,
                maxResultsPerPage, pageNumber);
        return mapToInternalLocationSearchResult(kartverketLocationSearchResult, locationSearch);
    }

    private String createMunicipalityCountyList(LocationSearch locationSearch) {
        StringBuilder coyntiesAndMunicipalities = new StringBuilder();
        List<Municipality> municipalities = locationSearch.getMunicipalities();
        municipalities.forEach(municipality -> {
            addSeparator(coyntiesAndMunicipalities);
            coyntiesAndMunicipalities.append(municipality.getId());
        });
        List<County> counties = locationSearch.getCounties();
        counties.forEach(county -> {
            addSeparator(coyntiesAndMunicipalities);
            coyntiesAndMunicipalities.append(county.getId());
        });
        return coyntiesAndMunicipalities.toString();
    }

    private void addSeparator(StringBuilder coyntiesAndMunicipalities) {
        if (coyntiesAndMunicipalities.length() > 0) {
            coyntiesAndMunicipalities.append(",");
        }
    }

    private LocationSearchResult mapToInternalLocationSearchResult(KartverketLocationSearchResult kartverketLocationSearchResult, LocationSearch locationSearch) {
        List<Location> locations = collectLocations(kartverketLocationSearchResult);
        int totalHits = kartverketLocationSearchResult.getTotaltAntallTreff();
        boolean hasMoreResults = hasMoreResults(totalHits, locationSearch.getPageNumber(), locationSearch.getMaxResultsPerPage());
        return LocationSearchResult.builder()
                .locations(locations)
                .numberOfResults(totalHits)
                .moreResults(hasMoreResults)
                .build();
    }

    private List<Location> collectLocations(KartverketLocationSearchResult kartverketLocationSearchResult) {
        return kartverketLocationSearchResult.getStedsnavn().stream()
                .map(this::buildLocation)
                .collect(toList());
    }

    private Location buildLocation(KartverketLocation kartverketLocation) {
        return Location.builder()
                .nameType(kartverketLocation.getNavnetype())
                .locationName(kartverketLocation.getStedsnavn())
                .municipality(buildMunicipality(kartverketLocation))
                .county(buildCounty(kartverketLocation))
                .gpsCoordinates(buildGpsCoordinates(kartverketLocation))
                .build();
    }

    private Municipality buildMunicipality(KartverketLocation kartverketLocation) {
        return Municipality.builder()
                .name(kartverketLocation.getKommunenavn())
                .build();
    }

    private County buildCounty(KartverketLocation kartverketLocation) {
        return County.builder()
                .name(kartverketLocation.getFylkesnavn())
                .build();
    }

    private GpsCoordinates buildGpsCoordinates(KartverketLocation kartverketLocation) {
        LatLng latLng = convertToLatLng(kartverketLocation);
        return new GpsCoordinates(latLng.getLat(), latLng.getLng());
    }

    private LatLng convertToLatLng(KartverketLocation kartverketLocation) {
        if (!"25833".equals(kartverketLocation.getEpsgKode())) {
            log.error(String.format("Kartverket bruker en annen epskode (%s) enn forventet (25833). Konverteringen av koordinatene vil vil trolig bli feil.",
                    kartverketLocation.getEpsgKode()));
        }
        UTMRef utmRef = new UTMRef(parseDouble(kartverketLocation.getAust()), parseDouble(kartverketLocation.getNord()), LAT_ZONE_W, LNG_ZONE_33);
        return utmRef.toLatLng();
    }

    private boolean hasMoreResults(int totaltAntallTreff, int pageNumber, int maxResultsPerPage) {
        if (pageNumber < 1) {
            return maxResultsPerPage < totaltAntallTreff;
        }
        return maxResultsPerPage * pageNumber < totaltAntallTreff;
    }
}
