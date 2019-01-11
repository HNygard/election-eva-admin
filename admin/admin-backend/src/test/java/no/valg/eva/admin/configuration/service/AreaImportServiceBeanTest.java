package no.valg.eva.admin.configuration.service;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.exception.EvoteException;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.evote.service.SigningKeyServiceBean;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.BaseTestFixture;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.evote.service.configuration.AreaImportServiceBean;
import no.evote.service.configuration.CountryServiceBean;
import no.evote.service.configuration.CountyServiceBean;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.List;

import static org.mockito.Mockito.mock;

@Test(groups = {TestGroups.SLOW, TestGroups.REPOSITORY})
public class AreaImportServiceBeanTest extends AbstractJpaTestBase {
    private static final String COUNTING_TEST_P12_PASSWORD = "IAT4CMC6KX4T95FG2AKH";
    private static final String COUNTING_TEST_P12_PATH = "counting-import-test/test-user.p12";
    private static final String COUNTING_TEST_P12_FILE_NAME = "test-user.p12";
    private static final String HELE_KOMMUNEN = "Hele kommunen";
    private AreaImportServiceBean areaImportService;
    private CountryServiceBean countryService;
    private CountyServiceBean countyService;
    private MunicipalityRepository municipalityRepository;
    private BoroughRepository boroughRepository;
    private PollingDistrictRepository pollingDistrictRepository;
    private PollingPlaceRepository pollingPlaceRepository;
    private SigningKeyRepository signingKeyRepository;
    private RBACTestFixture rbacTestFixture;
    private LegacyUserDataServiceBean userDataService;
    private LocaleRepository localeRepository;

    @BeforeMethod(alwaysRun = true)
    public void setupAreaImport() throws Exception {
        BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
        backend.initServices();
        SigningKeyServiceBean signingKeyService = backend.getSigningKeyService();
        signingKeyRepository = backend.getSigningKeyRepository();
        ElectionEventDomainService electionEventService = backend.getElectionEventService();
        areaImportService = backend.getAreaImportService();
        countryService = backend.getCountryService();
        countyService = backend.getCountyService();
        municipalityRepository = backend.getMunicipalityRepository();
        boroughRepository = backend.getBoroughRepository();
        pollingDistrictRepository = backend.getPollingDistrictRepository();
        pollingPlaceRepository = backend.getPollingPlaceRepository();

        userDataService = backend.getUserDataService();
        localeRepository = backend.getLocaleRepository();
        rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
        ElectionEventRepository electionEventRepository = backend.getElectionEventRepository();
        rbacTestFixture.init();
        ElectionEvent electionEvent = buildElectionEvent(electionEventRepository);
        ElectionEvent fromElectionEvent = electionEventRepository.findByPk(1L);

        electionEvent = electionEventService.create(rbacTestFixture.getSysAdminUserData(), electionEvent, true, VotingHierarchy.NONE,
                CountingHierarchy.AREA_HIERARCHY, fromElectionEvent, null);
        createElectionDays(electionEventRepository, electionEvent);

        SigningKey signingKey = createSigningKeyForElectionEvent(electionEvent);
        backend.getSystemPasswordStore().setPassword("");
        signingKeyService.create(rbacTestFixture.getSysAdminUserData(), signingKey, IOUtil.getBytes(BaseTestFixture.fileFromResources(COUNTING_TEST_P12_PATH)),
                COUNTING_TEST_P12_FILE_NAME, COUNTING_TEST_P12_PASSWORD, electionEvent);

    }

    private void createElectionDays(final ElectionEventRepository electionEventRepository, final ElectionEvent electionEvent) {
        ElectionDay electionDay = new ElectionDay();
        electionDay.setElectionEvent(electionEvent);
        electionDay.setDate(new LocalDate(2009, 10, 9));
        electionDay.setStartTime(new LocalTime(9, 0, 0));
        electionDay.setEndTime(new LocalTime(22, 0, 0));
        electionEventRepository.createElectionDay(rbacTestFixture.getSysAdminUserData(), electionDay);

        electionDay = new ElectionDay();
        electionDay.setElectionEvent(electionEvent);
        electionDay.setDate(new LocalDate(2009, 10, 10));
        electionDay.setStartTime(new LocalTime(9, 0, 0));
        electionDay.setEndTime(new LocalTime(22, 0, 0));
        electionEventRepository.createElectionDay(rbacTestFixture.getSysAdminUserData(), electionDay);
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@area.import.error_empty")
    public void importShouldFailForEmptyFile() throws Exception {
        importAreaHierarchyFromFile("empty.txt");
    }

    @Test
    public void everythingShouldBeImported() throws IOException, URISyntaxException {
        importAreaHierarchyFromFile("area-hierarchy-import.txt");
    }

    private void importAreaHierarchyFromFile(final String filename) throws IOException, URISyntaxException {
        UserData userData = userDataService.getUserData("03011700143", "valghendelse_admin", "999999", "999999", InetAddress.getLocalHost());
        File file = new File(this.getClass().getClassLoader().getResource(filename).toURI());
        if (!file.exists()) {
            throw new EvoteException("Unable to find file: " + file.toString());
        }

        areaImportService.importAreaHierarchy(userData, IOUtil.getBytes(file));

        // Test that everything was imported
        Country country = countryService.findCountryById(userData.getElectionEventPk(), "47");
        verify(country, "Norge");

        County county = countyService.findCountyById(country.getPk(), "10");
        verify(county, "Vest-Agder");

        Municipality municipality = municipalityRepository.findMunicipalityById(county.getPk(), "1001");
        verifyMunicipality(municipality, "Kristiansand", "nb-NO");

        Borough borough = boroughRepository.findBoroughById(municipality.getPk(), "100100");
        verifyBorough(borough, HELE_KOMMUNEN, true);

        PollingDistrict district = pollingDistrictRepository.findPollingDistrictById(borough.getPk(), "0005");
        verifyDistrict(district, "Hellemyr", false, false, false);

        PollingPlace pollingPlace = pollingPlaceRepository.findPollingPlaceById(district.getPk(), "0000");
        verifyPollingPlace(pollingPlace, HELE_KOMMUNEN, true, "foo", "bar", "zot", "3800", "Kristiansand", "Some text",
                new String[][]{new String[]{
                        "10.10.2009", "16.00", "20.00"}});

        district = pollingDistrictRepository.findPollingDistrictById(borough.getPk(), "0000");
        verifyDistrict(district, HELE_KOMMUNEN, true, false, false);

        pollingPlace = pollingPlaceRepository.findPollingPlaceById(district.getPk(), "0000");
        verifyPollingPlace(pollingPlace, HELE_KOMMUNEN, true, null, null, null, "3800", "Kristiansand", null, new String[][]{
                new String[]{"09.10.2009", "12.00", "19.00"}, new String[]{"10.10.2009", "16.00", "20.00"}});

        district = pollingDistrictRepository.findPollingDistrictById(borough.getPk(), "0009");
        verifyDistrict(district, "Grim/Kvadraturen", false, false, true);

        district = pollingDistrictRepository.findPollingDistrictById(borough.getPk(), "0008");
        verifyDistrict(district, "Kvadraturen", false, true, false);

        district = pollingDistrictRepository.findPollingDistrictById(borough.getPk(), "0007");
        verifyDistrict(district, "Grim", false, true, false);

        // Borough that is not "whole municipality"
        borough = boroughRepository.findBoroughById(municipality.getPk(), "100101");
        verifyBorough(borough, "Blah", false);

        // Other municipalities with misc. configuration
        municipality = municipalityRepository.findMunicipalityById(county.getPk(), "1002");
        verifyMunicipality(municipality, "Mandal", "nb-NO");

        municipality = municipalityRepository.findMunicipalityById(county.getPk(), "1003");
        verifyMunicipality(municipality, "Farsund", "nb-NO");
    }

    private void verifyPollingPlace(final PollingPlace pollingPlace, final String name, final boolean electionDay, final String addr1,
                                    final String addr2, final String addr3, final String postalCode, final String postTown, final String infoText,
                                    final String[][] expectedOpeningHours) {
        verify(pollingPlace, name);
        Assert.assertEquals(pollingPlace.getAddressLine1(), addr1);
        Assert.assertEquals(pollingPlace.getAddressLine2(), addr2);
        Assert.assertEquals(pollingPlace.getAddressLine3(), addr3);
        Assert.assertEquals(pollingPlace.getPostalCode(), postalCode);
        Assert.assertEquals(pollingPlace.getPostTown(), postTown);
        Assert.assertEquals(pollingPlace.isElectionDayVoting(), electionDay);
        Assert.assertEquals(pollingPlace.getInfoText(), infoText);

        int validated = 0;
        List<OpeningHours> actualOpeningHours = pollingPlaceRepository.findPollingPlaceWithOpeningHours(pollingPlace.getPk()).openingHoursAsList();
        for (OpeningHours actualOpeningHour : actualOpeningHours) {
            String date = DateUtil.getFormattedShortDate(actualOpeningHour.getElectionDay().getDate());
            String startTime = DateUtil.getFormattedTime(actualOpeningHour.getStartTime(),
                    DateTimeFormat.forPattern("HH.mm").withLocale(rbacTestFixture.getUserData().getJavaLocale()));
            String endTime = DateUtil.getFormattedTime(actualOpeningHour.getEndTime(),
                    DateTimeFormat.forPattern("HH.mm").withLocale(rbacTestFixture.getUserData().getJavaLocale()));

            for (String[] expectedOpeningHour : expectedOpeningHours) {
                if (expectedOpeningHour[0].equals(date) && expectedOpeningHour[1].equals(startTime) && expectedOpeningHour[2].equals(endTime)) {
                    validated++;
                }
            }
        }

        Assert.assertEquals(validated, expectedOpeningHours.length);
    }

    private void verifyBorough(final Borough borough, final String name, final boolean wholeMunicipality) {
        verify(borough, name);
        Assert.assertEquals(borough.isMunicipality1(), wholeMunicipality);

    }

    private void verifyMunicipality(final Municipality municipality, final String name, final String locale) {
        verify(municipality, name);
        Assert.assertEquals(municipality.getLocale().getId(), locale);
    }

    private <T extends Borough> void verify(final T entity, final String name) {
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getName(), name);
    }

    private <T extends Country> void verify(final T entity, final String name) {
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getName(), name);
    }

    private <T extends County> void verify(final T entity, final String name) {
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getName(), name);
    }

    private <T extends Municipality> void verify(final T entity, final String name) {
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getName(), name);
    }

    private <T extends PollingPlace> void verify(final T entity, final String name) {
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getName(), name);
    }

    private void verifyDistrict(final PollingDistrict district, final String name, final boolean wholeMunicipality, final boolean isChildDistrict,
                                final boolean isParentDistrict) {
        Assert.assertNotNull(district);
        Assert.assertEquals(district.getName(), name);
        Assert.assertEquals(district.isMunicipality(), wholeMunicipality);
        Assert.assertEquals(district.isChildPollingDistrict(), isChildDistrict);
        Assert.assertEquals(district.isParentPollingDistrict(), isParentDistrict);
    }

    private ElectionEvent buildElectionEvent(final ElectionEventRepository electionEventService) {
        ElectionEvent electionEvent = new ElectionEvent();
        electionEvent.setId("999999");
        electionEvent.setName("Valg 999999");
        electionEvent.setElectionEventStatus(electionEventService.findElectionEventStatusById(ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id()));

        electionEvent.setElectoralRollCutOffDate(LocalDate.now());
        electionEvent.setVotingCardDeadline(LocalDate.now());
        electionEvent.setVotingCardElectoralRollDate(LocalDate.now());
        electionEvent.setVoterNumbersAssignedDate(LocalDate.now());

        Locale locale = localeRepository.findById(BaseTestFixture.LOCALE_NB_NO);
        electionEvent.setLocale(locale);

        return electionEvent;
    }

    private SigningKey createSigningKeyForElectionEvent(ElectionEvent electionEvent) {
        SigningKey signingKey = new SigningKey();
        signingKey.setElectionEvent(electionEvent);
        signingKey.setKeyDomain(signingKeyRepository.findKeyDomainById("ADMIN_SIGNING"));
        return signingKey;
    }
}

