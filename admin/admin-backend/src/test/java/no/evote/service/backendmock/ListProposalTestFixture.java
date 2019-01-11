package no.evote.service.backendmock;

import static no.valg.eva.admin.common.AreaPath.OSLO_COUNTY_ID;

import java.util.HashSet;
import java.util.Set;

import no.evote.service.configuration.AffiliationServiceBean;
import no.evote.service.configuration.CountryServiceBean;
import no.evote.service.configuration.CountyServiceBean;
import no.evote.service.configuration.PartyServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
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
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeClass;



public class ListProposalTestFixture extends ElectionTestFixture {
	public static final String TEST_POSTAL_CODE = "0101";
	private static final String NB_NO = "nb-NO";
	private static final String VOTER_FIRSTNAME = "Firstname";
	private static final String VOTER_LASTNAME = "Lastname";
	private static final String VOTER_ID = "06058043956";
	private static final String VOTER_ID2 = "10016036213";
	private static final String VOTER_MUN_2 = "26094938332";
	private static final String VOTER_MUN_3 = "23110839537";
	private AffiliationServiceBean affiliationService;
	private AffiliationRepository affiliationRepository;
	private PartyServiceBean partyService;
	private VoterRepository voterRepository;
	private MunicipalityRepository municipalityRepository;
	private CountyServiceBean countyServiceBean;
	private CountryServiceBean countryService;
	private LocaleRepository localeRepository;
	private Affiliation testAffiliation;
	private Municipality testMunicipality;
	private Municipality testMunicipality2;
	private Municipality testMunicipality3;
	private Affiliation testAffiliation2;
	private MvArea mvArea;
	private Set<MvArea> mvAreasSet;
	private PartyCategoryRepository partyCategoryRepository;

	public ListProposalTestFixture(BackendContainer backend) {
		super(backend.getUserDataService(), backend.getAccessRepository(), backend.getElectionGroupRepository(),
				backend.getElectionRepository(),
				backend.getContestRepository(), backend.getLocaleRepository(), backend.getElectionEventRepository(), backend.getElectionEventService());
		this.partyCategoryRepository = backend.getPartyCategoryRepository();
		this.countryService = backend.getCountryService();
		this.affiliationService = backend.getAffiliationService();
		this.affiliationRepository = backend.getAffiliationRepository();
		this.partyService = backend.getPartyServiceBean();
		this.voterRepository = backend.getVoterRepository();
		this.municipalityRepository = backend.getMunicipalityRepository();
		this.countyServiceBean = backend.getCountyService();
		this.localeRepository = backend.getLocaleRepository();
	}

	@Override
	@BeforeClass
	public void init() {
		super.init();
		initElectionEvent();
		initListProposal();
		initElectoralRoll();
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
		countryService.create(getUserData(), testCountry);
		mvArea.setCountry(testCountry);

		County testCounty = new County();
		testCounty.setId("66");
		testCounty.setName("testCounty");
		testCounty.setCountry(testCountry);
		testCounty.setLocale(localeRepository.findById(NB_NO));
		countyServiceBean.create(getUserData(), testCounty);
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

		municipalityRepository.create(getUserData(), testMunicipality);
		municipalityRepository.create(getUserData(), testMunicipality2);
		municipalityRepository.create(getUserData(), testMunicipality3);

		mvAreasSet = new HashSet<>();
		mvAreasSet.add(mvArea);
	}

	private void initListProposal() {
		Party testParty = new Party();
		testParty.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		testParty.setId("TestId");
		testParty.setTranslatedPartyName("AName");
		testParty.setShortCode(9998);
		testParty.setElectionEvent(getElectionEvent());

		Party testParty2 = new Party();
		testParty2.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		testParty2.setId("TestIdTw");
		testParty2.setTranslatedPartyName("AName");
		testParty2.setShortCode(9997);
		testParty2.setElectionEvent(getElectionEvent());

		partyService.create(getUserData(), testParty2);

		testAffiliation2 = affiliationService.createNewAffiliation(getUserData(), getContest(), testParty2, getUserData().getLocale(),
				BallotStatus.BallotStatusValue.PENDING.getId());

		partyService.create(getUserData(), testParty);

		testAffiliation = affiliationService.createNewAffiliation(getUserData(), getContest(), testParty, getUserData().getLocale(),
				BallotStatus.BallotStatusValue.PENDING.getId());

	}

	private void initElectoralRoll() {
		Voter testVoter = new Voter();
		testVoter.setElectionEvent(getElectionEvent());
		testVoter.setId(VOTER_ID);
		testVoter.setCountyId(OSLO_COUNTY_ID);
		testVoter.setCountryId("47");
		testVoter.setFirstName(VOTER_FIRSTNAME);
		testVoter.setLastName(VOTER_LASTNAME);
		testVoter.setNameLine(VOTER_FIRSTNAME + " " + VOTER_LASTNAME);
		testVoter.setMunicipalityId(testMunicipality.getId());
		testVoter.setBoroughId("030104");
		testVoter.setPollingDistrictId("0404");
		testVoter.setEligible(true);
		testVoter.setDateTimeSubmitted(DateTime.now().toDate());
		testVoter.setAarsakskode("02");
		testVoter.setRegDato(LocalDate.now());
		testVoter.setSpesRegType('0');
		testVoter.setAddressLine1("Trettebakken");
		testVoter.setPostalCode("0755");
		testVoter.setApproved(true);

		Voter testVoter2 = new Voter();
		testVoter2.setElectionEvent(getElectionEvent());
		testVoter2.setId(VOTER_ID2);
		testVoter2.setCountyId(OSLO_COUNTY_ID);
		testVoter2.setCountryId("47");
		testVoter2.setFirstName("F");
		testVoter2.setLastName("L");
		testVoter2.setNameLine("F L");
		testVoter2.setMunicipalityId(testMunicipality.getId());
		testVoter2.setBoroughId("030104");
		testVoter2.setPollingDistrictId("0404");
		testVoter2.setEligible(true);
		testVoter2.setDateTimeSubmitted(DateTime.now().toDate());
		testVoter2.setAarsakskode("02");
		testVoter2.setRegDato(LocalDate.now());
		testVoter2.setSpesRegType('0');
		testVoter2.setAddressLine1("Trettebakken");
		testVoter2.setPostalCode("0755");
		testVoter2.setApproved(true);

		Voter testVoterMunicipality2 = new Voter();
		testVoterMunicipality2.setElectionEvent(getElectionEvent());
		testVoterMunicipality2.setId(VOTER_MUN_2);
		testVoterMunicipality2.setCountyId(OSLO_COUNTY_ID);
		testVoterMunicipality2.setCountryId("47");
		testVoterMunicipality2.setFirstName("F");
		testVoterMunicipality2.setLastName("L");
		testVoterMunicipality2.setNameLine("F L");
		testVoterMunicipality2.setMunicipalityId(testMunicipality2.getId());
		testVoterMunicipality2.setBoroughId("030104");
		testVoterMunicipality2.setPollingDistrictId("0404");
		testVoterMunicipality2.setEligible(true);
		testVoterMunicipality2.setDateTimeSubmitted(DateTime.now().toDate());
		testVoterMunicipality2.setAarsakskode("02");
		testVoterMunicipality2.setRegDato(LocalDate.now());
		testVoterMunicipality2.setSpesRegType('0');
		testVoterMunicipality2.setAddressLine1("Trettebakken");
		testVoterMunicipality2.setPostalCode("0755");
		testVoterMunicipality2.setApproved(true);

		Voter testVoterMunicipality3 = new Voter();
		testVoterMunicipality3.setElectionEvent(getElectionEvent());
		testVoterMunicipality3.setId(VOTER_MUN_3);
		testVoterMunicipality3.setCountyId(OSLO_COUNTY_ID);
		testVoterMunicipality3.setCountryId("47");
		testVoterMunicipality3.setFirstName("F");
		testVoterMunicipality3.setLastName("L");
		testVoterMunicipality3.setNameLine("F L");
		testVoterMunicipality3.setMunicipalityId(testMunicipality3.getId());
		testVoterMunicipality3.setBoroughId("030104");
		testVoterMunicipality3.setPollingDistrictId("0404");
		testVoterMunicipality3.setEligible(true);
		testVoterMunicipality3.setDateTimeSubmitted(DateTime.now().toDate());
		testVoterMunicipality3.setAarsakskode("02");
		testVoterMunicipality3.setRegDato(LocalDate.now());
		testVoterMunicipality3.setSpesRegType('0');
		testVoterMunicipality3.setAddressLine1("Trettebakken");
		testVoterMunicipality3.setPostalCode("0755");
		testVoterMunicipality3.setApproved(true);

		voterRepository.create(getUserData(), testVoter);
		voterRepository.create(getUserData(), testVoter2);
		voterRepository.create(getUserData(), testVoterMunicipality2);
		voterRepository.create(getUserData(), testVoterMunicipality3);
	}

	public Affiliation getTestAffiliation() {
		return testAffiliation;
	}

	public Long getTestBallotPk() {
		return testAffiliation.getBallot().getPk();
	}

	public void updateTestAffiliation() {
		this.testAffiliation = affiliationRepository.findAffiliationByPk(testAffiliation.getPk());
	}

	public Municipality getTestMunicipality() {
		return testMunicipality;
	}

	public Municipality getTestMunicipality2() {
		return testMunicipality2;
	}

	public Municipality getTestMunicipality3() {
		return testMunicipality3;
	}

	public Affiliation getTestAffiliation2() {
		return testAffiliation2;
	}

	public String getVoterFirstname() {
		return VOTER_FIRSTNAME;
	}

	public String getVoterLastName() {
		return VOTER_LASTNAME;
	}

	public String getVoterId() {
		return VOTER_ID;
	}

	public String getVoterId2() {
		return VOTER_ID2;
	}

	public String getVoterMun2Id() {
		return VOTER_MUN_2;
	}

	public String getVoterMun3Id() {
		return VOTER_MUN_3;
	}

	public MvArea getMvArea() {
		mvArea = new MvArea();
		mvArea.setElectionEventId(getElectionEvent().getId());
		mvArea.setCountryId("47");
		mvArea.setCountyId("66");

		return mvArea;
	}

	public MvArea getNewMvArea() {
		MvArea newMvArea = new MvArea();
		newMvArea.setElectionEventId(mvArea.getElectionEventId());
		newMvArea.setCountryId(mvArea.getCountryId());
		newMvArea.setCountyId(mvArea.getCountyId());

		newMvArea.setCountry(mvArea.getCountry());

		newMvArea.setCounty(mvArea.getCounty());

		return newMvArea;
	}

	public Set<MvArea> getMvAreasSet() {
		return mvAreasSet;
	}

}

