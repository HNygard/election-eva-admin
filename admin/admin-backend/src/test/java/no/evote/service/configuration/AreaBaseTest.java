package no.evote.service.configuration;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.application.MunicipalityApplicationService;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.domain.service.MunicipalityDomainService;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import javax.enterprise.event.Event;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.mockito.Mockito.mock;

//           Dette ville muliggjort integrasjonstester av tjenester direkte i admin-configuration (og "senere" moduler")
//           Før det må imidlertid klassene som ligger referert her skrives om og flyttes ihht nytt arkitekturmålbilde
public abstract class AreaBaseTest extends AbstractJpaTestBase {
    protected static final String LOCALE_NB_NO = "nb-NO";
    protected Country country;
    protected County county;
    protected Municipality municipality;
    protected Borough borough;
    protected PollingDistrict pollingDistrict;
    protected PollingPlace pollingPlace;
    protected ElectionDay electionDay;
    protected ElectionEventDomainService electionEventService;
    protected CountryServiceBean countryServiceBean;
    protected CountyServiceBean countyService;
    protected CountyRepository countyRepository;
    protected MunicipalityDomainService municipalityService;
    protected MunicipalityRepository municipalityRepository;
    protected BoroughServiceBean boroughService;
    protected PollingDistrictServiceBean pollingDistrictService;
    protected PollingDistrictRepository pollingDistrictRepository;
    protected PollingPlaceDomainService pollingPlaceApplicationService;
    protected PollingPlaceRepository pollingPlaceRepository;
    protected PollingStationRepository pollingStationRepository;
    protected LocaleRepository localeRepository;
    protected ServiceBackedRBACTestFixture rbacTestFixture;
    protected GenericTestRepository genericTestRepository;
    protected BackendContainer backend;
    protected ElectionEventRepository electionEventRepository;
    protected MunicipalityApplicationService municipalityApplicationService;
    protected VoterRepository voterRepository;
    protected MvAreaRepository mvAreaRepository;
    private Validator validator;
    private ElectionEvent electionEvent;

    @BeforeMethod(alwaysRun = true)
    public void initDependencies() throws Exception {
        backend = new BackendContainer(getEntityManager(), mock(Event.class));
        backend.initServices();

        electionEventService = backend.getElectionEventService();
        countryServiceBean = backend.getCountryService();
        countyService = backend.getCountyService();
        countyRepository = backend.getCountyRepository();
        municipalityService = backend.getMunicipalityService();
        municipalityRepository = backend.getMunicipalityRepository();
        boroughService = backend.getBoroughService();
        pollingDistrictService = backend.getPollingDistrictService();
        pollingDistrictRepository = backend.getPollingDistrictRepository();
        pollingPlaceApplicationService = backend.getPollingPlaceDomainService();
        pollingPlaceRepository = backend.getPollingPlaceRepository();
        pollingStationRepository = backend.getPollingStationRepository();
        localeRepository = backend.getLocaleRepository();
        municipalityApplicationService = backend.getMunicipalityApplicationService();
        voterRepository = backend.getVoterRepository();
        mvAreaRepository = backend.getMvAreaRepository();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
        rbacTestFixture.init();
        genericTestRepository = new GenericTestRepository(getEntityManager());
        electionEventRepository = backend.getElectionEventRepository();
    }

    protected ElectionEvent buildElectionEvent() {
        ElectionEvent electionEvent = new ElectionEvent();
        electionEvent.setId("209901");
        electionEvent.setName("Valg 2099");
        electionEvent.setElectionEventStatus(electionEventRepository.findElectionEventStatusById(0));

        electionEvent.setElectoralRollCutOffDate(LocalDate.now());
        electionEvent.setVotingCardDeadline(LocalDate.now());
        electionEvent.setVotingCardElectoralRollDate(LocalDate.now());

        Locale locale = localeRepository.findById(LOCALE_NB_NO);
        electionEvent.setLocale(locale);

        Set<ConstraintViolation<ElectionEvent>> constraintViolations = validator.validate(electionEvent);
        Assert.assertEquals(constraintViolations.size(), 0);

        return electionEvent;
    }

    protected Country buildCountry(final ElectionEvent electionEvent) {
        Country country = new Country();
        country.setElectionEvent(electionEvent);
        country.setId("01");
        country.setName("Danmark");

        Set<ConstraintViolation<Country>> constraintViolations = validator.validate(country);
        Assert.assertEquals(constraintViolations.size(), 0);

        return country;
    }

    protected County buildCounty(final Country country) {
        County county = new County();
        county.setCountry(country);
        county.setId("01");
        county.setName("Jylland");
        county.setLocale(localeRepository.findById(LOCALE_NB_NO));

        Set<ConstraintViolation<County>> constraintViolations = validator.validate(county);
        Assert.assertEquals(constraintViolations.size(), 0);

        return county;
    }

    protected Municipality buildMunicipality(final County county) {
        Municipality municipality = new Municipality();
        municipality.setCounty(county);
        municipality.setId("0506");
        municipality.setName("Skagen");
        Locale locale = localeRepository.findById(LOCALE_NB_NO);
        municipality.setLocale(locale);
        municipality.setElectronicMarkoffs(true);

        Set<ConstraintViolation<Municipality>> constraintViolations = validator.validate(municipality);
        Assert.assertEquals(constraintViolations.size(), 0);

        return municipality;
    }

    Borough buildBorough(final Municipality municipality) {
        Borough borough = new Borough();
        borough.setMunicipality(municipality);
        borough.setId("010101");
        borough.setName("Sentrum");
        borough.setMunicipality1(false);

        Set<ConstraintViolation<Borough>> constraintViolations = validator.validate(borough);
        Assert.assertEquals(constraintViolations.size(), 0);

        return borough;
    }

    PollingDistrict buildPollingDistrict(final Borough borough) {
        PollingDistrict pollingDistrict = new PollingDistrict();
        pollingDistrict.setBorough(borough);
        pollingDistrict.setId("7771");
        pollingDistrict.setName("Polling district 1");

        Set<ConstraintViolation<PollingDistrict>> constraintViolations = validator.validate(pollingDistrict);
        Assert.assertEquals(constraintViolations.size(), 0);

        return pollingDistrict;
    }

    PollingPlace buildPollingPlace(final PollingDistrict pollingDistrict) {
        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setPollingDistrict(pollingDistrict);
        pollingPlace.setId("9987");
        pollingPlace.setName("Polling place 1");
        pollingPlace.setAddressLine1("Address 1");
        pollingPlace.setAddressLine2("Address 2");
        pollingPlace.setAddressLine3("Address 3");
        pollingPlace.setInfoText("Info");
        pollingPlace.setPostalCode("0000");
        Set<ConstraintViolation<PollingPlace>> constraintViolations = validator.validate(pollingPlace);
        Assert.assertEquals(constraintViolations.size(), 0);

        return pollingPlace;
    }

    PollingStation buildPollingStation(final PollingPlace pollingPlace) {
        PollingStation pollingStation = new PollingStation();
        pollingStation.setId("01");
        pollingStation.setFirst("AA");
        pollingStation.setLast("RR");
        pollingStation.setPollingPlace(pollingPlace);

        Set<ConstraintViolation<PollingStation>> constraintViolations = validator.validate(pollingStation);
        Assert.assertEquals(constraintViolations.size(), 0);

        return pollingStation;
    }

    protected void createElectionEvent(final ElectionEvent electionEvent) {
        electionEventService.create(rbacTestFixture.getSysAdminUserData(), electionEvent, false, VotingHierarchy.NONE, CountingHierarchy.NONE, null,
                null);

        // Asserts
        Assert.assertNotNull(electionEvent);
        Assert.assertNotNull(electionEvent.getPk());
        Assert.assertTrue(electionEvent.getPk() > 0);
    }

    protected void createCountry(final Country country) {
        countryServiceBean.create(rbacTestFixture.getUserData(), country);

        // Asserts
        Assert.assertNotNull(country);
        Assert.assertNotNull(country.getPk());
        Assert.assertTrue(country.getPk() > 0);
    }

    protected void createCounty(final County county) {
        countyService.create(rbacTestFixture.getUserData(), county);

        // Asserts
        Assert.assertNotNull(county);
        Assert.assertNotNull(county.getPk());
        Assert.assertTrue(county.getPk() > 0);
    }

    protected void createMunicipality(final Municipality municipality) {
        municipalityRepository.create(rbacTestFixture.getUserData(), municipality);

        // Asserts
        Assert.assertNotNull(municipality);
        Assert.assertNotNull(municipality.getPk());
        Assert.assertTrue(municipality.getPk() > 0);
    }

    protected void createBorough(final Borough borough) {
        boroughService.create(rbacTestFixture.getUserData(), borough);

        // Asserts
        Assert.assertNotNull(borough);
        Assert.assertNotNull(borough.getPk());
        Assert.assertTrue(borough.getPk() > 0);
    }

    void createPollingDistrict(final PollingDistrict pollingDistrict) {
        pollingDistrictRepository.create(rbacTestFixture.getUserData(), pollingDistrict);

        // Asserts
        Assert.assertNotNull(pollingDistrict);
        Assert.assertNotNull(pollingDistrict.getPk());
        Assert.assertTrue(pollingDistrict.getPk() > 0);
    }

    void createPollingPlace(final PollingPlace pollingPlace) {
        pollingPlaceApplicationService.create(rbacTestFixture.getUserData(), pollingPlace);

        // Asserts
        Assert.assertNotNull(pollingPlace);
        Assert.assertNotNull(pollingPlace.getPk());
        Assert.assertTrue(pollingPlace.getPk() > 0);
    }

    void createPollingStation(final PollingStation pollingStation) {
        pollingStationRepository.create(rbacTestFixture.getUserData(), pollingStation);

        // Asserts
        Assert.assertNotNull(pollingStation);
        Assert.assertNotNull(pollingStation.getPk());
        Assert.assertTrue(pollingStation.getPk() > 0);
    }

    protected Validator getValidator() {
        return validator;
    }

    protected ElectionEvent getElectionEvent() {
        return electionEvent;
    }

    protected void setElectionEvent(final ElectionEvent electionEvent) {
        this.electionEvent = electionEvent;
    }

    protected Country getCountry() {
        return country;
    }

    protected void setCountry(final Country country) {
        this.country = country;
    }

    protected County getCounty() {
        return county;
    }

    protected void setCounty(final County county) {
        this.county = county;
    }

    protected Municipality getMunicipality() {
        return municipality;
    }

    protected void setMunicipality(final Municipality municipality) {
        this.municipality = municipality;
    }

    protected Borough getBorough() {
        return borough;
    }

    protected void setBorough(final Borough borough) {
        this.borough = borough;
    }

    protected PollingDistrict getPollingDistrict() {
        return pollingDistrict;
    }

    protected void setPollingDistrict(final PollingDistrict pollingDistrict) {
        this.pollingDistrict = pollingDistrict;
    }

    protected PollingPlace getPollingPlace() {
        return pollingPlace;
    }

    protected void setPollingPlace(final PollingPlace pollingPlace) {
        this.pollingPlace = pollingPlace;
    }

    CountryServiceBean getCountryServiceBean() {
        return countryServiceBean;
    }

    CountyServiceBean getCountyServiceBean() {
        return countyService;
    }

    CountyRepository getCountyRepository() {
        return countyRepository;
    }

    MunicipalityDomainService getMunicipalityService() {
        return municipalityService;
    }

    MunicipalityRepository getMunicipalityRepository() {
        return municipalityRepository;
    }

    BoroughServiceBean getBoroughService() {
        return boroughService;
    }

    PollingDistrictRepository getPollingDistrictRepository() {
        return pollingDistrictRepository;
    }

    protected ElectionDay getElectionDay() {
        return electionDay;
    }

    protected void setElectionDay(final ElectionDay electionDay) {
        this.electionDay = electionDay;
    }

    protected ElectionEventRepository getElectionEventRepository() {
        return electionEventRepository;
    }

    CountryServiceBean getCountryService() {
        return countryServiceBean;
    }
}

