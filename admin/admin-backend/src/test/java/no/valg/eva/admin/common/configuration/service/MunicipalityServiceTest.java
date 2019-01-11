package no.evote.service.configuration;

import no.evote.dto.ConfigurationDto;
import no.evote.util.MockUtils;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.test.TestGroups;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {TestGroups.REPOSITORY})
public class MunicipalityServiceTest extends AreaBaseTest {
    private static final String TEST_ELECTION_EVENT_ID = "209901";
    private static final String SECOND_TEST_MUNICIPALITY_ID = "0032";
    private static final String FIRST_TEST_MUNICIPALITY_ID = "0031";

    @BeforeMethod(alwaysRun = true)
    public void init() {
        setElectionEvent(buildElectionEvent());
        createElectionEvent(getElectionEvent());

        setCountry(buildCountry(getElectionEvent()));
        createCountry(getCountry());

        setCounty(buildCounty(getCountry()));
        createCounty(getCounty());
    }

    @Test
    public void testCreate() {
        setMunicipality(buildMunicipality(getCounty()));

        getMunicipalityRepository().create(rbacTestFixture.getUserData(), getMunicipality());

        Assert.assertTrue(getMunicipality().getPk() != null);
        Assert.assertTrue(getMunicipality().getPk() > 0);
        Assert.assertTrue(getMunicipality().isElectronicMarkoffs());
    }

    @Test
    public void municipalityShouldNotHaveBoroughs() {
        setMunicipality(buildMunicipality(getCounty()));
        Assert.assertFalse(countyService.getCountiesWithoutMunicipalities(getElectionEvent().getPk()).isEmpty());

        getMunicipalityRepository().create(rbacTestFixture.getUserData(), getMunicipality());
        Assert.assertTrue(countyService.getCountiesWithoutMunicipalities(getElectionEvent().getPk()).isEmpty());
        Assert.assertFalse(getMunicipalityService().getMunicipalitiesWithoutBoroughs(getElectionEvent().getPk()).isEmpty());
    }

    @Test
    public void findPollingDistrictIdsWithVotersOnlyInElectoralRoll() throws Exception {
        // Builds a Municipality entry
        setMunicipality(buildMunicipality(getCounty()));
        Municipality m = municipalityRepository.create(rbacTestFixture.getUserData(), getMunicipality());
        Borough borough1 = buildBorough(m);
        createBorough(borough1);
        Voter voter = new Voter();
        voter.setElectionEvent(getElectionEvent());
        voter.setId("08023311705");
        voter.setFirstName("Benny");
        voter.setLastName("Bennersen");
        voter.setNameLine("Bennersen Benny");
        voter.setCountryId(getCountry().getId());
        voter.setCountyId(getCounty().getId());
        voter.setMunicipalityId(m.getId());
        voter.setBoroughId(borough1.getId());
        voter.setPollingDistrictId("1111");
        voter.setEligible(true);
        voter.setDateTimeSubmitted(DateTime.now().toDate());
        voter.setApproved(false);
        voterRepository.create(rbacTestFixture.getUserData(), voter);

        // To avoid tracing this, overwrite the electionEventPk field
        // ElectionEvent here is 209901, but user is on event 200701.
        MockUtils.setPrivateField(rbacTestFixture.getUserData(), "electionEventPk", getElectionEvent().getPk());
        KommuneSti kommuneSti = ValggeografiSti.kommuneSti(m.areaPath());
        List<ConfigurationDto> result = municipalityApplicationService.findVotersWithoutPollingDistricts(
                rbacTestFixture.getUserData(), kommuneSti);
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo("0506.010101.1111");
        assertThat(result.get(0).getName()).isEqualTo("Skagen, Sentrum");
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testCreateIdTooLong() {
        setMunicipality(buildMunicipality(getCounty()));
        getMunicipality().setId("12345");

        getMunicipalityRepository().create(rbacTestFixture.getUserData(), getMunicipality());
    }

    @Test(expectedExceptions = PersistenceException.class)
    public void testCreateDuplicateId() {
        setMunicipality(buildMunicipality(getCounty()));
        getMunicipalityRepository().create(rbacTestFixture.getUserData(), getMunicipality());

        getMunicipalityRepository().create(rbacTestFixture.getUserData(), buildMunicipality(getCounty()));
    }

    @Test
    public void testUpdate() {
        Municipality municipality = buildMunicipality(getCounty());
        setMunicipality(municipality);
        createMunicipality(municipality);
        municipality = getMunicipalityRepository().findByPk(municipality.getPk());
        String updatedName = "updatedName";
        municipality.setName(updatedName);

        Municipality updatedMunicipality = getMunicipalityRepository().update(rbacTestFixture.getUserData(), municipality);

        Assert.assertNotNull(updatedMunicipality);
        Assert.assertTrue(updatedMunicipality.getName().equals(updatedName));
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testUpdateIdTooLong() {
        setMunicipality(buildMunicipality(getCounty()));
        createMunicipality(getMunicipality());
        getMunicipality().setId("12345");

        getMunicipalityRepository().update(rbacTestFixture.getUserData(), getMunicipality());
    }

    @Test(expectedExceptions = PersistenceException.class)
    public void testUpdateDuplicateId() {
        Municipality municipality1 = buildMunicipality(getCounty());
        municipality1.setId("0051");
        createMunicipality(municipality1);
        Municipality municipality2 = buildMunicipality(getCounty());
        municipality2.setId("0052");
        createMunicipality(municipality2);
        municipality2.setId("0051");

        getMunicipalityRepository().update(rbacTestFixture.getUserData(), municipality2);
    }

    @Test
    public void testFindMunicipalityById() {
        setMunicipality(buildMunicipality(getCounty()));
        createMunicipality(getMunicipality());

        Municipality municipality = getMunicipalityRepository().findMunicipalityById(getCounty().getPk(), getMunicipality().getId());

        Assert.assertNotNull(municipality);
        Assert.assertTrue(municipality.getId().equals(getMunicipality().getId()));
    }

    @Test
    public void testFindMunicipalityByIdNotFound() {
        Municipality municipality = getMunicipalityRepository().findMunicipalityById(getCounty().getPk(), "X");

        Assert.assertNull(municipality);
    }

    @Test
    public void findMunicipalitiesByCountryPk() {
        Municipality municipality1 = buildMunicipality(getCounty());
        municipality1.setId(FIRST_TEST_MUNICIPALITY_ID);
        createMunicipality(municipality1);

        Municipality municipality2 = buildMunicipality(getCounty());
        municipality2.setId(SECOND_TEST_MUNICIPALITY_ID);
        createMunicipality(municipality2);

        List<Municipality> municipalityList = getMunicipalityRepository().findMunicipalitiesByCountryPk(getCountry().getPk());

        Assert.assertNotNull(municipalityList);
        Assert.assertEquals(municipalityList.size(), 2);
    }

    @Test
    public void testGetLocale() {
        Municipality municipality1 = buildMunicipality(getCounty());
        getMunicipalityRepository().create(rbacTestFixture.getUserData(), municipality1);
        Assert.assertEquals(municipalityRepository.getLocale(municipality1).getId(), AreaBaseTest.LOCALE_NB_NO);
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsFindsPollingPlace() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality = buildMunicipality(getCounty());
        municipality.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality.setElectronicMarkoffs(false);

        createMunicipality(municipality);

        Borough borough = buildBorough(municipality);
        createBorough(borough);

        PollingDistrict pollingDistrict = buildPollingDistrict(borough);
        createPollingDistrict(pollingDistrict);

        PollingPlace pollingPlace = buildPollingPlace(pollingDistrict);
        pollingPlace.setUsingPollingStations(true);
        pollingPlace.setElectionDayVoting(true);
        createPollingPlace(pollingPlace);
        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 1);
        Assert.assertEquals(municipalities.get(0), municipality);
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsNotFindPollingPlaceWithoutElectionDayVoting() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality = buildMunicipality(getCounty());
        municipality.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality.setElectronicMarkoffs(false);
        createMunicipality(municipality);

        Borough borough = buildBorough(municipality);
        createBorough(borough);

        PollingDistrict pollingDistrict = buildPollingDistrict(borough);
        createPollingDistrict(pollingDistrict);

        PollingPlace pollingPlace = buildPollingPlace(pollingDistrict);
        pollingPlace.setUsingPollingStations(true);
        createPollingPlace(pollingPlace);
        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 0);
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsNotFindPollingPlaceWithoutUsingPollingStations() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality = buildMunicipality(getCounty());
        municipality.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality.setElectronicMarkoffs(false);
        createMunicipality(municipality);

        Borough borough = buildBorough(municipality);
        createBorough(borough);

        PollingDistrict pollingDistrict = buildPollingDistrict(borough);
        createPollingDistrict(pollingDistrict);

        PollingPlace pollingPlace = buildPollingPlace(pollingDistrict);
        pollingPlace.setElectionDayVoting(true);
        createPollingPlace(pollingPlace);
        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 0);
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsNotFindPollingPlaceWithPollingStationsConigured() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality = buildMunicipality(getCounty());
        municipality.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality.setElectronicMarkoffs(false);
        createMunicipality(municipality);

        Borough borough = buildBorough(municipality);
        createBorough(borough);

        PollingDistrict pollingDistrict = buildPollingDistrict(borough);
        createPollingDistrict(pollingDistrict);

        PollingPlace pollingPlace = buildPollingPlace(pollingDistrict);
        pollingPlace.setUsingPollingStations(true);
        pollingPlace.setElectionDayVoting(true);
        createPollingPlace(pollingPlace);

        PollingStation pollingStation = buildPollingStation(pollingPlace);
        createPollingStation(pollingStation);

        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 0);
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsNotReturningDuplicates() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality = buildMunicipality(getCounty());
        municipality.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality.setElectronicMarkoffs(false);
        createMunicipality(municipality);

        Borough borough = buildBorough(municipality);
        createBorough(borough);

        PollingDistrict pollingDistrict1 = buildPollingDistrict(borough);
        createPollingDistrict(pollingDistrict1);

        PollingDistrict pollingDistrict2 = buildPollingDistrict(borough);
        pollingDistrict2.setId("7772");
        createPollingDistrict(pollingDistrict2);

        PollingPlace pollingPlace1 = buildPollingPlace(pollingDistrict1);
        pollingPlace1.setUsingPollingStations(true);
        pollingPlace1.setElectionDayVoting(true);
        createPollingPlace(pollingPlace1);

        PollingPlace pollingPlace2 = buildPollingPlace(pollingDistrict2);
        pollingPlace2.setUsingPollingStations(true);
        pollingPlace2.setId("9986");
        pollingPlace2.setElectionDayVoting(true);
        createPollingPlace(pollingPlace2);

        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 1);
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsReturningTwoMunicipalities() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality1 = buildMunicipality(getCounty());
        municipality1.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality1.setElectronicMarkoffs(false);
        createMunicipality(municipality1);

        Municipality municipality2 = buildMunicipality(getCounty());
        municipality2.setId(SECOND_TEST_MUNICIPALITY_ID);
        municipality2.setElectronicMarkoffs(false);
        createMunicipality(municipality2);

        Borough borough1 = buildBorough(municipality1);
        createBorough(borough1);

        Borough borough2 = buildBorough(municipality2);
        createBorough(borough2);

        PollingDistrict pollingDistrict1 = buildPollingDistrict(borough1);
        createPollingDistrict(pollingDistrict1);

        PollingDistrict pollingDistrict2 = buildPollingDistrict(borough2);
        pollingDistrict2.setId("7772");
        createPollingDistrict(pollingDistrict2);

        PollingPlace pollingPlace1 = buildPollingPlace(pollingDistrict1);
        pollingPlace1.setUsingPollingStations(true);
        pollingPlace1.setElectionDayVoting(true);
        createPollingPlace(pollingPlace1);

        PollingPlace pollingPlace2 = buildPollingPlace(pollingDistrict2);
        pollingPlace2.setUsingPollingStations(true);
        pollingPlace2.setId("9986");
        pollingPlace2.setElectionDayVoting(true);
        createPollingPlace(pollingPlace2);

        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 2);

        Assert.assertTrue(municipalities.contains(municipality1));
        Assert.assertTrue(municipalities.contains(municipality2));
    }

    @Test
    public void testGetMunicipalitiesWithPollingPlacesWithoutPollingStationsNotFindIfMunicipalityUsesElectronicMarkoffs() {
        rbacTestFixture.getUserData().getOperatorRole().getMvArea().setAreaPath(TEST_ELECTION_EVENT_ID);

        Municipality municipality = buildMunicipality(getCounty());
        municipality.setId(FIRST_TEST_MUNICIPALITY_ID);
        municipality.setElectronicMarkoffs(true);
        createMunicipality(municipality);

        Borough borough = buildBorough(municipality);
        createBorough(borough);

        PollingDistrict pollingDistrict = buildPollingDistrict(borough);
        createPollingDistrict(pollingDistrict);

        PollingPlace pollingPlace = buildPollingPlace(pollingDistrict);
        pollingPlace.setUsingPollingStations(true);
        pollingPlace.setElectionDayVoting(true);
        createPollingPlace(pollingPlace);

        List<Municipality> municipalities = municipalityRepository.getMunicipalitiesWithPollingPlacesWithoutPollingStations(rbacTestFixture.getUserData());

        Assert.assertEquals(municipalities.size(), 0);
    }

    @Test
    public void testStoreValidInfoText() {
        Municipality municipality = createValidMunicipality("1452");
        municipality.setElectionCardText("test valid data");
        municipality = municipalityRepository.create(rbacTestFixture.getUserData(), municipality);
        try {
            Assert.assertEquals(municipality.getElectionCardText(), "test valid data");
        } finally {
            municipalityRepository.delete(rbacTestFixture.getUserData(), municipality.getPk());
        }
    }

    @Test
    public void testStoreXSSInfoText() {
        Municipality municipality = createValidMunicipality("1452");
        municipality.setElectionCardText("<script>alert('X')</script>");
        municipality = municipalityRepository.create(rbacTestFixture.getUserData(), municipality);
        try {
            Assert.assertEquals(municipality.getElectionCardText(), "");
        } finally {
            municipalityRepository.delete(rbacTestFixture.getUserData(), municipality.getPk());
        }
    }

    private Municipality createValidMunicipality(final String id) {
        Municipality municipality = new Municipality();
        municipality.setId(id);
        Locale locale = localeRepository.findById(AreaBaseTest.LOCALE_NB_NO);
        municipality.setLocale(locale);
        municipality.setName("muni");
        municipality.setCounty(getCounty());
        municipality.setElectronicMarkoffs(true);

        return municipality;
    }
}
