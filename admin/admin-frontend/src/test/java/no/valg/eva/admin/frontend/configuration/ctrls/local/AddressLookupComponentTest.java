package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.application.MapService;
import no.valg.eva.admin.application.map.Address;
import no.valg.eva.admin.application.map.GpsCoordinates;
import no.valg.eva.admin.application.map.Location;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.model.local.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddressLookupComponentTest {
    
    private PollingPlace place;
    private MvArea mvArea;
    private MapService mapService;
    
    @BeforeMethod
    public void before() {
        this.mapService = mock(MapService.class, RETURNS_DEEP_STUBS);
        this.place = new AdvancePollingPlace(AreaPath.from("111111.22.33.4444"));
        this.mvArea = MvArea.builder()
                .municipalityId("0301")
                .municipalityName("Oslo")
                .build();
    }

    @Test
    public void addressWithHouseNumber_doesAddressLookup() {
        givenPlaceWithAddressIncludingHouseNumber();
        whenDoingAddressLookup();
        thenAddressLookupShouldBeCalled();
    }
    
    private void givenPlaceWithAddressIncludingHouseNumber() {
        place.setAddress("Husekleppveien 54");
        place.setPostalCode("0405");
        place.setPostTown("Oslo");
    }
    
    private void whenDoingAddressLookup() {
        // Mock of list doesnt do stream handling properly. Need to return the real deal!
        when(mapService.addressSearch(any()).getAddresses()).thenReturn(emptyList());
        when(mapService.locationSearch(any()).getLocations()).thenReturn(emptyList());

        AddressLookupComponent.findGpsCoordinatesForPlace(place, mvArea, mapService);
    }
    
    private void thenAddressLookupShouldBeCalled() {
        // Include first time call from Mockito.when()
        verify(mapService, times(1)).locationSearch(any());
        verify(mapService, times(2)).addressSearch(any());
    }

    @Test
    public void addressWithoutHouseNumber_doesPlaceLookup() {
        givenPlaceWithAddressExcludingHouseNumber();
        whenDoingAddressLookup();
        thenLocationLookupShouldBeCalled();
    }

    private void givenPlaceWithAddressExcludingHouseNumber() {
        place.setAddress("Husekleppveien");
        place.setPostalCode("0405");
        place.setPostTown("Oslo");
    }

    private void thenLocationLookupShouldBeCalled() {
        // Include first time call from Mockito.when()
        verify(mapService, times(1)).addressSearch(any());
        verify(mapService, times(2)).locationSearch(any());
    }
    
    @Test
    public void addressFoundReturnsLatLng() {
        givenPlaceWithAddressIncludingHouseNumber();
        
        final double expectedLat = 59.65432;
        final double expectedLon = 11.23456;
        when(mapService.addressSearch(any()).getAddresses())
                .thenReturn(singletonList(Address.builder().gpsCoordinates(new GpsCoordinates(expectedLat, expectedLon)).build()));
        final String actualCoordinates = AddressLookupComponent.findGpsCoordinatesForPlace(place, mvArea, mapService);
        
        assertThat(actualCoordinates).isEqualTo(valueOf(expectedLat) + ", " + valueOf(expectedLon));
    }

    @Test
    public void locationFoundReturnsLatLng() {
        givenPlaceWithAddressExcludingHouseNumber();

        final double expectedLat = 59.65432;
        final double expectedLon = 11.23456;
        when(mapService.locationSearch(any()).getLocations())
                .thenReturn(singletonList(Location.builder().gpsCoordinates(new GpsCoordinates(expectedLat, expectedLon)).build()));
        final String actualCoordinates = AddressLookupComponent.findGpsCoordinatesForPlace(place, mvArea, mapService);

        assertThat(actualCoordinates).isEqualTo(valueOf(expectedLat) + ", " + valueOf(expectedLon));
    }

    @Test
    public void addressNotFoundReturnsBlank() {
        givenPlaceWithAddressIncludingHouseNumber();

        final String expectedCoordinates = "";
        when(mapService.addressSearch(any()).getAddresses()).thenReturn(emptyList());
        final String actualCoordinates = AddressLookupComponent.findGpsCoordinatesForPlace(place, mvArea, mapService);

        assertThat(actualCoordinates).isEqualTo(expectedCoordinates);
    }

    @Test
    public void locationNotFoundReturnsBlank() {
        givenPlaceWithAddressExcludingHouseNumber();

        final String expectedCoordinates = "";
        when(mapService.locationSearch(any()).getLocations()).thenReturn(emptyList());
        final String actualCoordinates = AddressLookupComponent.findGpsCoordinatesForPlace(place, mvArea, mapService);

        assertThat(actualCoordinates).isEqualTo(expectedCoordinates);
    }
}
