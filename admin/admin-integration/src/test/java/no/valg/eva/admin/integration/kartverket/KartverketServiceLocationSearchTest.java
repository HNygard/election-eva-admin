package no.valg.eva.admin.integration.kartverket;

import no.valg.eva.admin.application.map.LocationSearch;
import no.valg.eva.admin.application.map.LocationSearchResult;
import no.valg.eva.admin.common.configuration.model.County;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.integration.kartverket.model.KartverketLocation;
import no.valg.eva.admin.integration.kartverket.model.KartverketLocationSearchResult;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class KartverketServiceLocationSearchTest extends MockUtilsTestCase {

    private static final int PAGE_NUMBER_0 = 0;
    private static final int PAGE_NUMBER_5 = 5;
    private static final int RESULT_PER_PAGE_10 = 10;
    private static final int RESULT_PER_PAGE_50 = 50;
    private static final int RESULT_PER_PAGE_0 = 0;
    private static final int TOTAL_HITS_5 = 5;
    private static final int TOTAL_HITS_10 = 10;
    private static final int TOTAL_HITS_50 = 50;
    private static final int TOTAL_HITS_100 = 100;

    private KartverketService kartverketService;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        this.kartverketService = initializeMocks(KartverketService.class);
    }

    @Test(dataProvider = "locationSearchTestData")
    public void locationSearch_givenLocationSearch_executesSearchWithSearchString(LocationSearch locationSearch, String expectedSearchString,
                                                                                  String expectedMunicipalityCountyList, int expectedMaxResultPerPage,
                                                                                  int expectedPageNumber) {

        KartverketRestServiceClient kartverketRestServiceClient = getInjectMock(KartverketRestServiceClient.class);
        when(kartverketRestServiceClient.searchLocation(anyString(), anyString(), anyInt(), anyInt())).thenReturn(new KartverketLocationSearchResult());

        kartverketService.locationSearch(locationSearch);

        if (expectedSearchString.isEmpty()) {
            verifyZeroInteractions(kartverketRestServiceClient);
        } else {
            verify(kartverketRestServiceClient).searchLocation(expectedSearchString, expectedMunicipalityCountyList, expectedMaxResultPerPage, expectedPageNumber);
        }
    }

    @DataProvider
    public Object[][] locationSearchTestData() {
        return new Object[][]{
                {LocationSearch.builder()
                        .locationName("Kaupang")
                        .maxResultsPerPage(RESULT_PER_PAGE_10)
                        .build(),
                        "Kaupang", "", RESULT_PER_PAGE_10, PAGE_NUMBER_0},
                {LocationSearch.builder()
                        .locationName("Kaupang")
                        .municipalities(Arrays.asList(Municipality.builder().id("07").build(), Municipality.builder().id("08").build()))
                        .build(),
                        "Kaupang", "07,08", RESULT_PER_PAGE_50, PAGE_NUMBER_0},
                {LocationSearch.builder()
                        .locationName("Kaupang")
                        .counties(Arrays.asList(County.builder().id("0707").build(), County.builder().id("0710").build()))
                        .build(),
                        "Kaupang", "0707,0710", RESULT_PER_PAGE_50, PAGE_NUMBER_0},
                {LocationSearch.builder()
                        .build(),
                        "", "", RESULT_PER_PAGE_0, PAGE_NUMBER_0},
                {LocationSearch.builder()
                        .counties(singletonList(County.builder()
                                .id("07")
                                .build()))
                        .build(),
                        "", "07", RESULT_PER_PAGE_10, PAGE_NUMBER_0},
        };
    }

    @Test
    public void locationSearch_givenEmptySearchString_returnsEmpyLocationSearchResult() {
        LocationSearch locationSearch = LocationSearch.builder().build();

        LocationSearchResult searchResult = kartverketService.locationSearch(locationSearch);

        assertThat(searchResult.getNumberOfResults()).isEqualTo(0);
        assertThat(searchResult.isMoreResults()).isFalse();
        assertThat(searchResult.getLocations().size()).isEqualTo(0);
    }

    @Test(dataProvider = "locationSearchResultHits")
    public void locationSearch_givenPaging_returnsHasMoreResults(LocationSearch locationSearch, KartverketLocationSearchResult kartverketLocationSearchResult,
                                                                 boolean expected) {
        when(getInjectMock(KartverketRestServiceClient.class)
                .searchLocation(locationSearch.getLocationName(), "", locationSearch.getMaxResultsPerPage(), locationSearch.getPageNumber()))
                .thenReturn(kartverketLocationSearchResult);

        LocationSearchResult locationSearchResult = kartverketService.locationSearch(locationSearch);

        assertThat(locationSearchResult.isMoreResults()).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] locationSearchResultHits() {

        return new Object[][]{
                {buildLocationSearch(PAGE_NUMBER_0), buildExternalLocationSearchResult(TOTAL_HITS_5), false},
                {buildLocationSearch(PAGE_NUMBER_0), buildExternalLocationSearchResult(TOTAL_HITS_10), false},
                {buildLocationSearch(PAGE_NUMBER_0), buildExternalLocationSearchResult(TOTAL_HITS_50), true},
                {buildLocationSearch(PAGE_NUMBER_5), buildExternalLocationSearchResult(TOTAL_HITS_50), false},
                {buildLocationSearch(PAGE_NUMBER_5), buildExternalLocationSearchResult(TOTAL_HITS_100), true},
        };
    }

    private LocationSearch buildLocationSearch(int pageNumber) {
        return LocationSearch.builder()
                .locationName("locationName")
                .pageNumber(pageNumber)
                .maxResultsPerPage(RESULT_PER_PAGE_10)
                .build();
    }

    private KartverketLocationSearchResult buildExternalLocationSearchResult(int totalHits) {
        KartverketLocationSearchResult kartverketLocationSearchResult = new KartverketLocationSearchResult();
        for (int i = 0; i < totalHits; i++) {
            kartverketLocationSearchResult.getStedsnavn().add(kartverketLocation());
        }
        kartverketLocationSearchResult.setTotaltAntallTreff(totalHits);
        return kartverketLocationSearchResult;
    }

    private KartverketLocation kartverketLocation() {
        KartverketLocation kartverketLocation = new KartverketLocation();
        kartverketLocation.setStedsnavn("place");
        kartverketLocation.setAust("237682.04");
        kartverketLocation.setNord("6576386.85");
        kartverketLocation.setEpsgKode("25833");
        return kartverketLocation;
    }

    @Test
    public void locationSearch_givenLocation_returnsFormattedLocation() {
        KartverketLocationSearchResult kartverketLocationSearchResult = new KartverketLocationSearchResult();
        kartverketLocationSearchResult.getStedsnavn().add(kartverketLocation());
        when(getInjectMock(KartverketRestServiceClient.class).searchLocation(anyString(), anyString(), anyInt(), anyInt())).thenReturn(kartverketLocationSearchResult);

        LocationSearchResult locationSearchResult = kartverketService.locationSearch(buildLocationSearch(PAGE_NUMBER_0));

        assertThat(locationSearchResult.getLocations().size()).isEqualTo(1);
        assertThat(locationSearchResult.getLocations().get(0).getGpsCoordinates().toString()).isEqualTo("59.24496, 10.3991");
    }

}
