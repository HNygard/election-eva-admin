package no.valg.eva.admin.application;

import lombok.extern.log4j.Log4j;
import no.valg.eva.admin.application.map.Address;
import no.valg.eva.admin.application.map.AddressSearch;
import no.valg.eva.admin.application.map.AddressSearchResult;
import no.valg.eva.admin.application.map.LocationSearch;
import no.valg.eva.admin.application.map.LocationSearchResult;
import no.valg.eva.admin.common.configuration.model.County;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.integration.kartverket.KartverketRestServiceClient;
import no.valg.eva.admin.integration.kartverket.KartverketService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * DEV-NOTE:
 * Testene i denne klassen utfører kall mot Kartverket.
 * De er fine for å eksperimentere med koden manuelt, men skal ikke brukes som integrasjonstester. Derfor er alle testense satt til enabled = false.
 */
@Test
@Log4j
public class MapServiceTest extends MockUtilsTestCase {

    private static final int MAX_RESULTS = 100;

    private MapService service;

    @BeforeMethod
    public void setUp() {
        KartverketRestServiceClient kartverketRestServiceClient = new KartverketRestServiceClient();
        kartverketRestServiceClient.postConstruct();
        service = new MapService(new KartverketService(kartverketRestServiceClient));
    }

    @Test(enabled = false)
    public void testName() {
        AddressSearchResult adresser = service.addressSearch(AddressSearch.builder()
                .streetName("Storgata")
                .houseNumber("7")
                .houseLetter("B")
                .postalCode("3256")
                .postTown("Larvik")
                .municipality(Municipality.builder()
                        .name("Larvik")
                        .build())
                .maxResults(MAX_RESULTS)
                .build());


        List<Address> addresses = adresser.getAddresses();
        log.debug(addresses.size());
        for (Address address : addresses) {
            log.debug(address);
        }
    }

    @Test(enabled = false, dataProvider = "addressSearch")
    public void testAddressSearch(AddressSearch addressSearch) {

        AddressSearchResult searchResult = service.addressSearch(addressSearch);

        assertThat(searchResult.getAddresses().size()).isNotZero();
    }

    @DataProvider
    public Object[][] addressSearch() {
        return new Object[][]{
                {AddressSearch.builder()
                        .streetName("Rambergveien")
                        .build()
                },
                {AddressSearch.builder()
                        .streetName("Rambergveien")
                        .houseNumber("9")
                        .build()
                },
                {AddressSearch.builder()
                        .streetName("Rambergveien")
                        .houseNumber("9")
                        .municipality(Municipality.builder()
                                .name("Tønsberg")
                                .build())
                        .build()
                }
        };
    }

    @Test(enabled = false, dataProvider = "locationSearch")
    public void testLocatonSearch(LocationSearch locationSearch) {
        LocationSearchResult locationSearchResult = service.locationSearch(locationSearch);

        assertThat(locationSearchResult.getLocations().size()).isNotZero();

        log.debug(locationSearchResult);
    }

    @DataProvider
    public static Object[][] locationSearch() {
        return new Object[][]{
                {LocationSearch.builder()
                        .locationName("Kaupang")
                        .build()},
                {LocationSearch.builder()
                        .locationName("Kaupang")
                        .counties(singletonList(County.builder().id("07").build()))
                        .build()}
        };
    }

    @Test(enabled = false)
    public void testLocationSearchEttTreff() {
        LocationSearch locationSearch = LocationSearch.builder().locationName("Hortenstunnelen").build();
        LocationSearchResult locationSearchResult = service.locationSearch(locationSearch);
        assertThat(locationSearchResult.getLocations().size()).isEqualTo(1);
    }
}
