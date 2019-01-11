package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.application.MapService;
import no.valg.eva.admin.application.map.AddressSearch;
import no.valg.eva.admin.application.map.HasGpsCoordinates;
import no.valg.eva.admin.application.map.LocationSearch;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.configuration.model.local.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.util.StringUtil.isNotBlank;

public class AddressLookupComponent {

    private AddressLookupComponent() {
    }

    public static String findGpsCoordinatesForPlace(PollingPlace pollingPlace, MvArea mvArea, MapService mapService) {
        return canLookupCoordinatesFor(pollingPlace)
                ? new AddressLookup(mapService, pollingPlace, mvArea).lookupGpsCoordinates()
                : "";
    }

    public static boolean canLookupCoordinatesFor(PollingPlace pp) {
        return isNotBlank(pp.getAddress(), pp.getPostalCode(), pp.getPostTown());
    }

    private static class AddressLookup {

        private static final Pattern ADDRESS_PATTERN = Pattern.compile("^(.+)(?:\\s([0-9]+)([A-Za-z]*)?)$");

        private static final int STREET_NAME_GROUP_INDEX = 1;
        private static final int HOUSE_NUMBER_GROUP_INDEX = 2;
        private static final int HOUSE_LETTER_GROUP_INDEX = 3;

        private MapService mapService;
        private PollingPlace pollingPlace;
        private Municipality municipality;

        private String streetName;
        private String houseNumber;
        private String houseLetter;

        private AddressLookup(MapService mapService, PollingPlace pollingPlace, MvArea mvArea) {
            this.mapService = mapService;
            this.pollingPlace = pollingPlace;
            this.municipality = toSimpleMunicipality(mvArea);
        }

        private Municipality toSimpleMunicipality(MvArea mvArea) {
            return Municipality.builder()
                    .id(mvArea.getMunicipalityId())
                    .name(mvArea.getMunicipalityName())
                    .build();
        }

        private String lookupGpsCoordinates() {
            resolveAddress();
            return shouldDoAddressSearch() ? doAddressSearch() : doLocationSearch();
        }

        private void resolveAddress() {
            final Matcher matcher = ADDRESS_PATTERN.matcher(pollingPlace.getAddress());
            streetName = matcher.matches() && matcher.groupCount() > 0 ? matcher.group(STREET_NAME_GROUP_INDEX) : pollingPlace.getAddress();
            houseNumber = matcher.matches() && matcher.groupCount() > 1 ? matcher.group(HOUSE_NUMBER_GROUP_INDEX) : "";
            houseLetter = matcher.matches() && matcher.groupCount() > 2 ? matcher.group(HOUSE_LETTER_GROUP_INDEX) : "";
        }

        private boolean shouldDoAddressSearch() {
            return isNotBlank(this.houseNumber);
        }

        private String doAddressSearch() {
            final AddressSearch addressSearch = AddressSearch.builder()
                    .streetName(streetName)
                    .houseNumber(houseNumber)
                    .houseLetter(houseLetter)
                    .municipality(municipality)
                    .postalCode(pollingPlace.getPostalCode())
                    .postTown(pollingPlace.getPostTown())
                    .build();

            return findFirstWithGpsCoordinates(mapService.addressSearch(addressSearch).getAddresses());
        }

        private String findFirstWithGpsCoordinates(List<? extends HasGpsCoordinates> list) {
            return list.stream()
                    .filter(address -> address.getGpsCoordinates() != null)
                    .map(address -> address.getGpsCoordinates().toString())
                    .findFirst()
                    .orElse("");
        }

        private String doLocationSearch() {
            final LocationSearch locationSearch = LocationSearch.builder()
                    .locationName(streetName)
                    .municipalities(singletonList(municipality))
                    .build();

            return findFirstWithGpsCoordinates(mapService.locationSearch(locationSearch).getLocations());
        }
    }
}
