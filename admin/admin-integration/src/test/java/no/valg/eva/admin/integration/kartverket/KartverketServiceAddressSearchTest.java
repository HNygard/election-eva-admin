package no.valg.eva.admin.integration.kartverket;

import no.valg.eva.admin.application.map.AddressSearch;
import no.valg.eva.admin.application.map.AddressSearchResult;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.integration.kartverket.model.KartverketAddress;
import no.valg.eva.admin.integration.kartverket.model.KartverketAddressSearchResult;
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

public class KartverketServiceAddressSearchTest extends MockUtilsTestCase {

    private static final int PAGE_NUMBER_0 = 0;
    private static final int RESULT_PER_PAGE = 100;
    private static final int RESULT_PER_PAGE_0 = 0;

    private KartverketService kartverketService;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        this.kartverketService = initializeMocks(KartverketService.class);
    }

    @Test(dataProvider = "addressSearchTestData")
    public void addressSearch_givenAddressSearch_executesSearchWithSearchString(AddressSearch addressSearch, String expectedSearchString,
                                                                                int expectedMaxResultPerPage, int expectedPageNumber) {
        KartverketRestServiceClient kartverketRestServiceClient = getInjectMock(KartverketRestServiceClient.class);
        when(kartverketRestServiceClient.searchAddress(anyString(), anyInt(), anyInt()))
                .thenReturn(KartverketAddressSearchResult.builder().adresser(singletonList(KartverketAddress.builder().build())).build());

        kartverketService.addressSearch(addressSearch);

        if (expectedSearchString.isEmpty()) {
            verifyZeroInteractions(kartverketRestServiceClient);
        } else {
            verify(kartverketRestServiceClient).searchAddress(expectedSearchString, expectedMaxResultPerPage, expectedPageNumber);
        }

    }

    @DataProvider
    public Object[][] addressSearchTestData() {
        Municipality municipality = municipality();
        return new Object[][]{
                {AddressSearch.builder()
                        .streetName("gatenavn")
                        .houseNumber("husnummer")
                        .municipality(municipality)
                        .build(),
                        "gatenavn husnummer," + municipality.getName(), RESULT_PER_PAGE, PAGE_NUMBER_0
                },
                {AddressSearch.builder()
                        .streetName("gatenavn")
                        .houseNumber("husnummer")
                        .build(),
                        "gatenavn husnummer", RESULT_PER_PAGE, PAGE_NUMBER_0
                },
                {AddressSearch.builder()
                        .municipality(municipality)
                        .build(),
                        "", RESULT_PER_PAGE_0, PAGE_NUMBER_0
                },
                {AddressSearch.builder()
                        .streetName("gatenavn")
                        .build(),
                        "gatenavn", RESULT_PER_PAGE, PAGE_NUMBER_0
                },
                {AddressSearch.builder().build(),
                        "", RESULT_PER_PAGE_0, PAGE_NUMBER_0
                }
        };
    }

    private Municipality municipality() {
        return Municipality.builder()
                .name("kommunenavn")
                .build();
    }

    @Test
    public void addressSearch_givenEmptySearchString_returnsEmpyAddressSearchResult() {
        AddressSearch addressSearch = AddressSearch.builder().build();

        AddressSearchResult searchResult = kartverketService.addressSearch(addressSearch);

        assertThat(searchResult.getAddresses().size()).isEqualTo(0);
    }

    @Test(dataProvider = "removesNonInterestingResults")
    public void addressSearch_givenAddressSearch_removesNonInterestingResults(AddressSearch addressSearch, KartverketAddressSearchResult kartverketAddressSearchResult) {
        KartverketRestServiceClient kartverketRestServiceClient = getInjectMock(KartverketRestServiceClient.class);
        when(kartverketRestServiceClient.searchAddress(anyString(), anyInt(), anyInt())).thenReturn(kartverketAddressSearchResult);

        AddressSearchResult addressSearchResult = kartverketService.addressSearch(addressSearch);

        assertThat(addressSearchResult.getAddresses().size()).isEqualTo(1);
    }

    @DataProvider
    public Object[][] removesNonInterestingResults() {
        return new Object[][]{
                {
                        AddressSearch.builder().streetName("tekst1").build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("Tekst1").build(),
                                KartverketAddress.builder().adressenavn("Tekst2").build(),
                                KartverketAddress.builder().build()
                        )).build()
                },
                {
                        AddressSearch.builder().streetName("adresse").houseNumber("tekst1").build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").husnr("Tekst1").build(),
                                KartverketAddress.builder().adressenavn("adresse").husnr("tekst2").build(),
                                KartverketAddress.builder().adressenavn("adresse").build()
                        )).build()
                },
                {
                        AddressSearch.builder().streetName("adresse").houseLetter("tekst1").build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").bokstav("Tekst1").build(),
                                KartverketAddress.builder().adressenavn("adresse").bokstav("tekst2").build(),
                                KartverketAddress.builder().adressenavn("adresse").build()
                        )).build()
                },
                {
                        AddressSearch.builder().streetName("adresse").postalCode("tekst1").build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").postnr("Tekst1").build(),
                                KartverketAddress.builder().adressenavn("adresse").postnr("tekst2").build(),
                                KartverketAddress.builder().adressenavn("adresse").build()
                        )).build()
                },
                {
                        AddressSearch.builder().streetName("adresse").postTown("tekst1").build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").poststed("Tekst1").build(),
                                KartverketAddress.builder().adressenavn("adresse").poststed("tekst2").build(),
                                KartverketAddress.builder().adressenavn("adresse").build()
                        )).build()
                },
                {
                        AddressSearch.builder().streetName("adresse").municipality(
                                Municipality.builder().name("kommune1").build()
                        ).build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").kommunenavn("Kommune1").build(),
                                KartverketAddress.builder().adressenavn("adresse").kommunenavn("Kommune2").build(),
                                KartverketAddress.builder().adressenavn("adresse").build()
                        )).build()
                },
                {
                        AddressSearch.builder()
                                .streetName("adresse")
                                .municipality(Municipality.builder().name("kommune1").build())
                                .postalCode("3240")
                                .build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").kommunenavn("Kommune1").postnr("3240").build(),
                                KartverketAddress.builder().adressenavn("adresse").kommunenavn("Kommune1").postnr("3241").build(),
                                KartverketAddress.builder().adressenavn("adresse").build()
                        )).build()
                },
                {
                        AddressSearch.builder()
                                .streetName("adresse")
                                .municipality(Municipality.builder().name("kommune1").build())
                                .postalCode("0341")
                                .build(),
                        KartverketAddressSearchResult.builder().adresser(Arrays.asList(
                                KartverketAddress.builder().adressenavn("adresse").kommunenavn("Kommune1").postnr("341").build()
                        )).build()
                }
        };
    }
}
