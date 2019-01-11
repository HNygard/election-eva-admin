package no.evote.service.configuration;

import java.util.HashSet;
import java.util.Set;

import no.evote.service.backendmock.BackendContainer;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;


public abstract class ListProposalBaseTest extends ElectionBaseTest {
	public static final String POLLING_DISTRICT_ID = "0404";
	public static final String TRETTEBAKKEN = "Trettebakken";
	public static final String BOROUGH_ID = "030104";
	public static final String POSTAL_CODE = "0755";
	private static final String NB_NO = "nb-NO";
	private static final String VOTER_FIRSTNAME = "Firstname";
	private static final String VOTER_LASTNAME = "Lastname";
	private static final String VOTER_ID = "06058043956";
	private static final String VOTER_ID2 = "10016036213";
	private static final String VOTER_MUN_2 = "26094938332";
	private static final String VOTER_MUN_3 = "23110839537";
	protected BackendContainer backend;
	private AffiliationServiceBean affiliationService;
	private AffiliationRepository affiliationRepository;
	private PartyServiceBean partyService;
	private PartyCategoryRepository partyCategoryRepository;
	private VoterRepository voterRepository;
	private MunicipalityRepository municipalityRepository;
	private CountryRepository countryRepository;
	private CountyRepository countyRepository;
	private LocaleRepository localeRepository;
	private Affiliation testAffiliation;
	private Municipality testMunicipality;
	private Municipality testMunicipality2;
	private Municipality testMunicipality3;
	private Party testParty2;
	private MvArea mvArea;
	private Set<MvArea> mvAreasSet;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		initServices();
		initElectionEvent();
		initListProposal();
		initElectoralRoll();
	}

	private void initServices() {
		backend = new BackendContainer(getEntityManager());
		backend.initServices();

		affiliationService = backend.getAffiliationService();
		affiliationRepository = backend.getAffiliationRepository();
		partyService = backend.getPartyServiceBean();
		partyCategoryRepository = backend.getPartyCategoryRepository();
		voterRepository = backend.getVoterRepository();
		municipalityRepository = backend.getMunicipalityRepository();
		countyRepository = backend.getCountyRepository();
		countryRepository = backend.getCountryRepository();
		localeRepository = backend.getLocaleRepository();
	}

	private void initElectionEvent() {
		setElectionEvent(buildElectionEvent());
		createElectionEvent(getElectionEvent());
		setElectionGroup(buildElectionGroup(getElectionEvent()));
		createElectionGroup(getElectionGroup());
		setElection(buildElection(getElectionGroup()));
		createElection(getElection());
		setContest(buildContest(getElection()));

		getElection().setSingleArea(true);

		mvArea = new MvArea();
		mvArea.setElectionEventId(getElectionEvent().getId());
		mvArea.setCountryId("47");
		mvArea.setCountyId("66");

		ContestArea contestArea = new ContestArea();
		contestArea.setMvArea(mvArea);

		createContest(getContest());

		Country testCountry = new Country();
		testCountry.setId("47");
		testCountry.setElectionEvent(getElectionEvent());
		testCountry.setName("TestCountry");
		countryRepository.create(rbacTestFixture.getUserData(), testCountry);
		mvArea.setCountry(testCountry);

		County testCounty = new County();
		testCounty.setId("66");
		testCounty.setName("testCounty");
		testCounty.setCountry(testCountry);
		testCounty.setLocale(localeRepository.findById(NB_NO));
		testCounty.setCountyStatus(countyRepository.findCountyStatusById(CountyStatusEnum.CENTRAL_CONFIGURATION.id()));
		countyRepository.create(rbacTestFixture.getUserData(), testCounty);
		mvArea.setCounty(testCounty);

		testMunicipality = new Municipality();
		testMunicipality.setId("6666");
		testMunicipality.setName("testMunicipality");
		testMunicipality.setCounty(testCounty);
		testMunicipality.setLocale(localeRepository.findById(NB_NO));
		testMunicipality.setElectronicMarkoffs(true);

		testMunicipality2 = new Municipality();
		testMunicipality2.setId("6677");
		testMunicipality2.setName("testMunicipality2");
		testMunicipality2.setCounty(testCounty);
		testMunicipality2.setLocale(localeRepository.findById(NB_NO));
		testMunicipality2.setElectronicMarkoffs(true);

		testMunicipality3 = new Municipality();
		testMunicipality3.setId("6688");
		testMunicipality3.setName("testMunicipality3");
		testMunicipality3.setCounty(testCounty);
		testMunicipality3.setLocale(localeRepository.findById(NB_NO));
		testMunicipality3.setElectronicMarkoffs(true);

		municipalityRepository.create(rbacTestFixture.getUserData(), testMunicipality);
		municipalityRepository.create(rbacTestFixture.getUserData(), testMunicipality2);
		municipalityRepository.create(rbacTestFixture.getUserData(), testMunicipality3);

		mvAreasSet = new HashSet<>();
		mvAreasSet.add(mvArea);
	}

	private void initListProposal() {
		Party testParty = new Party();
		testParty.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		testParty.setId("TestId");
		testParty.setTranslatedPartyName("TestParty");
		testParty.setShortCode(9998);
		testParty.setElectionEvent(getElectionEvent());

		testParty2 = new Party();
		testParty2.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		testParty2.setId("TestIdTw");
		testParty2.setTranslatedPartyName("TestParty2");
		testParty2.setShortCode(9997);
		testParty2.setElectionEvent(getElectionEvent());

		partyService.create(rbacTestFixture.getUserData(), testParty2);

		affiliationService.createNewAffiliation(rbacTestFixture.getUserData(), getContest(), testParty2, rbacTestFixture.getUserData().getLocale(),
				BallotStatus.BallotStatusValue.PENDING.getId());

		partyService.create(rbacTestFixture.getUserData(), testParty);

		testAffiliation = affiliationService.createNewAffiliation(rbacTestFixture.getUserData(), getContest(), testParty, rbacTestFixture.getUserData()
				.getLocale(),
				BallotStatus.BallotStatusValue.PENDING.getId());

	}

	@SuppressWarnings("deprecation")
	private void initElectoralRoll() {
		Voter testVoter = new Voter();
		testVoter.setElectionEvent(getElectionEvent());
		testVoter.setId(VOTER_ID);
		testVoter.setCountyId("03");
		testVoter.setCountryId("47");
		testVoter.setFirstName(VOTER_FIRSTNAME);
		testVoter.setLastName(VOTER_LASTNAME);
		testVoter.setNameLine(VOTER_FIRSTNAME + " " + VOTER_LASTNAME);
		testVoter.setMunicipalityId(testMunicipality.getId());
		testVoter.setBoroughId(BOROUGH_ID);
		testVoter.setPollingDistrictId(POLLING_DISTRICT_ID);
		testVoter.setEligible(true);
		testVoter.setDateTimeSubmitted(DateTime.now().toDate());
		testVoter.setAarsakskode("02");
		testVoter.setRegDato(LocalDate.now());
		testVoter.setSpesRegType('0');
		testVoter.setAddressLine1(TRETTEBAKKEN);
		testVoter.setPostalCode(POSTAL_CODE);
		testVoter.setApproved(true);

		Voter testVoter2 = new Voter();
		testVoter2.setElectionEvent(getElectionEvent());
		testVoter2.setId(VOTER_ID2);
		testVoter2.setCountyId("03");
		testVoter2.setCountryId("47");
		testVoter2.setFirstName("F");
		testVoter2.setLastName("L");
		testVoter2.setNameLine("F L");
		testVoter2.setMunicipalityId(testMunicipality.getId());
		testVoter2.setBoroughId(BOROUGH_ID);
		testVoter2.setPollingDistrictId(POLLING_DISTRICT_ID);
		testVoter2.setEligible(true);
		testVoter2.setDateTimeSubmitted(DateTime.now().toDate());
		testVoter2.setAarsakskode("02");
		testVoter2.setRegDato(LocalDate.now());
		testVoter2.setSpesRegType('0');
		testVoter2.setAddressLine1(TRETTEBAKKEN);
		testVoter2.setPostalCode(POSTAL_CODE);
		testVoter2.setApproved(true);

		Voter testVoterMunicipality2 = new Voter();
		testVoterMunicipality2.setElectionEvent(getElectionEvent());
		testVoterMunicipality2.setId(VOTER_MUN_2);
		testVoterMunicipality2.setCountyId("03");
		testVoterMunicipality2.setCountryId("47");
		testVoterMunicipality2.setFirstName("F");
		testVoterMunicipality2.setLastName("L");
		testVoterMunicipality2.setNameLine("F L");
		testVoterMunicipality2.setMunicipalityId(testMunicipality2.getId());
		testVoterMunicipality2.setBoroughId(BOROUGH_ID);
		testVoterMunicipality2.setPollingDistrictId(POLLING_DISTRICT_ID);
		testVoterMunicipality2.setEligible(true);
		testVoterMunicipality2.setDateTimeSubmitted(DateTime.now().toDate());
		testVoterMunicipality2.setAarsakskode("02");
		testVoterMunicipality2.setRegDato(LocalDate.now());
		testVoterMunicipality2.setSpesRegType('0');
		testVoterMunicipality2.setAddressLine1(TRETTEBAKKEN);
		testVoterMunicipality2.setPostalCode(POSTAL_CODE);
		testVoterMunicipality2.setApproved(true);

		Voter testVoterMunicipality3 = new Voter();
		testVoterMunicipality3.setElectionEvent(getElectionEvent());
		testVoterMunicipality3.setId(VOTER_MUN_3);
		testVoterMunicipality3.setCountyId("03");
		testVoterMunicipality3.setCountryId("47");
		testVoterMunicipality3.setFirstName("F");
		testVoterMunicipality3.setLastName("L");
		testVoterMunicipality3.setNameLine("F L");
		testVoterMunicipality3.setMunicipalityId(testMunicipality3.getId());
		testVoterMunicipality3.setBoroughId(BOROUGH_ID);
		testVoterMunicipality3.setPollingDistrictId(POLLING_DISTRICT_ID);
		testVoterMunicipality3.setEligible(true);
		testVoterMunicipality3.setDateTimeSubmitted(DateTime.now().toDate());
		testVoterMunicipality3.setAarsakskode("02");
		testVoterMunicipality3.setRegDato(LocalDate.now());
		testVoterMunicipality3.setSpesRegType('0');
		testVoterMunicipality3.setAddressLine1(TRETTEBAKKEN);
		testVoterMunicipality3.setPostalCode(POSTAL_CODE);
		testVoterMunicipality3.setApproved(true);

		voterRepository.create(rbacTestFixture.getUserData(), testVoter);
		voterRepository.create(rbacTestFixture.getUserData(), testVoter2);
		voterRepository.create(rbacTestFixture.getUserData(), testVoterMunicipality2);
		voterRepository.create(rbacTestFixture.getUserData(), testVoterMunicipality3);
	}

	protected Affiliation getTestAffiliation() {
		return testAffiliation;
	}

	protected void updateTestAffiliation() {
		this.testAffiliation = affiliationRepository.findByPk(testAffiliation.getPk());
	}

	protected Party getTestParty2() {
		return testParty2;
	}

	protected MvArea getMvArea() {
		mvArea = new MvArea();
		mvArea.setElectionEventId(getElectionEvent().getId());
		mvArea.setCountryId("47");
		mvArea.setCountyId("66");

		return mvArea;
	}

	protected Set<MvArea> getMvAreasSet() {
		return mvAreasSet;
	}
}

