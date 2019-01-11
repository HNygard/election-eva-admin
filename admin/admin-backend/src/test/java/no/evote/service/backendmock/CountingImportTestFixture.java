package no.evote.service.backendmock;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.CountingHierarchy;
import no.evote.constants.EvoteConstants;
import no.evote.constants.VotingHierarchy;
import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.evote.service.SigningKeyServiceBean;
import no.evote.service.configuration.AffiliationServiceBean;
import no.evote.service.configuration.BoroughServiceBean;
import no.evote.service.configuration.ContestServiceBean;
import no.evote.service.configuration.CountryServiceBean;
import no.evote.service.configuration.CountyServiceBean;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.MaritalStatus;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.ElectionVoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionReportingUnitsRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.service.AccessServiceBean;
import org.joda.time.LocalDate;
import org.testng.annotations.AfterClass;

import javax.transaction.TransactionSynchronizationRegistry;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.AreaPath.OSLO_COUNTY_ID;
import static org.testng.AssertJUnit.fail;

/**
 * Creates a new election event, new area and election hierarchies, as well as other configuration required for the tests in CountingImportServiceTest.
 */
public class CountingImportTestFixture extends BaseTestFixture {

    private static final String ID_MOSS = "0104";
    private static final String COUNTING_TEST_P12_PASSWORD = "IAT4CMC6KX4T95FG2AKH";
    private static final String COUNTING_TEST_P12_PATH = "counting-import-test/test-user.p12";
    private static final String COUNTING_TEST_P12_FILE_NAME = "test-user.p12";
    private static final String NB_NO = "nb-NO";
    private AccessServiceBean accessService;
    private BoroughServiceBean boroughService;
    private ElectionEventRepository electionEventRepository;
    private ElectionRepository electionRepository;
    private MvAreaRepository mvAreaRepository;
    private MvElectionRepository mvElectionRepository;
    private MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository;
    private VoteCountCategoryRepository voteCountCategoryRepository;
    private PollingPlaceDomainService pollingPlaceDomainService;
    private PollingDistrictRepository pollingDistrictRepository;
    private VoteCountRepository voteCountRepository;
    private UserData userData;
    private ElectionEvent electionEvent;
    private ContestServiceBean contestService;
    private ContestAreaRepository contestAreaRepository;
    private ReportCountCategoryRepository reportCountCategoryRepository;
    private SigningKeyServiceBean signingKeyService;
    private SigningKeyRepository signingKeyRepository;
    private CountryServiceBean countryService;
    private CountyServiceBean countyService;
    private ElectionGroupRepository electionGroupRepository;
    private MunicipalityRepository municipalityRepository;
    private CandidateRepository candidateRepository;
    private AffiliationServiceBean affiliationService;
    private ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository;
    private LocaleRepository localeRepository;
    private ContestRepository contestRepository;
    private ElectionEventDomainService electionEventService;
    private PartyCategoryRepository partyCategoryRepository;
    private PartyRepository partyRepository;
    private ReportingUnitRepository reportingUnitRepository;
    private TransactionSynchronizationRegistry registry;

    private CountingImportTestFixture(
            BoroughServiceBean boroughService, ElectionEventRepository electionEventRepository,
            ElectionRepository electionRepository, MvAreaRepository mvAreaRepository,
            MvElectionRepository mvElectionRepository,
            VoteCountCategoryRepository voteCountCategoryRepository,
            PollingPlaceDomainService pollingPlaceDomainService,
            PollingDistrictRepository pollingDistrictRepository, VoteCountRepository voteCountRepository,
            LegacyUserDataServiceBean userDataService, AccessServiceBean accessService,
            AccessRepository accessRepository,
            ContestServiceBean contestService,
            ContestAreaRepository contestAreaRepository,
            ReportCountCategoryRepository reportCountCategoryRepository, SigningKeyServiceBean signingKeyService, SigningKeyRepository signingKeyRepository,
            CountryServiceBean countryService, CountyServiceBean countyService, ElectionGroupRepository electionGroupRepository,
            MunicipalityRepository municipalityRepository, MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository,
            CandidateRepository candidateRepository, AffiliationServiceBean affiliationService,
            ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository, LocaleRepository localeRepository,
            ContestRepository contestRepository, ElectionEventDomainService electionEventService, PartyCategoryRepository partyCategoryRepository,
            PartyRepository partyRepository, ReportingUnitRepository reportingUnitRepository, TransactionSynchronizationRegistry registry) {
        super(userDataService, accessRepository);
        this.accessService = accessService;
        this.boroughService = boroughService;
        this.electionEventRepository = electionEventRepository;
        this.electionRepository = electionRepository;
        this.mvAreaRepository = mvAreaRepository;
        this.mvElectionRepository = mvElectionRepository;
        this.mvElectionReportingUnitsRepository = mvElectionReportingUnitsRepository;
        this.voteCountCategoryRepository = voteCountCategoryRepository;
        this.pollingPlaceDomainService = pollingPlaceDomainService;
        this.pollingDistrictRepository = pollingDistrictRepository;
        this.voteCountRepository = voteCountRepository;
        this.contestService = contestService;
        this.contestAreaRepository = contestAreaRepository;
        this.reportCountCategoryRepository = reportCountCategoryRepository;
        this.signingKeyService = signingKeyService;
        this.signingKeyRepository = signingKeyRepository;
        this.countryService = countryService;
        this.countyService = countyService;
        this.electionGroupRepository = electionGroupRepository;
        this.municipalityRepository = municipalityRepository;
        this.candidateRepository = candidateRepository;
        this.affiliationService = affiliationService;
        this.electionVoteCountCategoryRepository = electionVoteCountCategoryRepository;
        this.localeRepository = localeRepository;
        this.contestRepository = contestRepository;
        this.electionEventService = electionEventService;
        this.partyCategoryRepository = partyCategoryRepository;
        this.partyRepository = partyRepository;
        this.reportingUnitRepository = reportingUnitRepository;
        this.registry = registry;
    }

    public CountingImportTestFixture(BackendContainer backend) {
        this(backend.getBoroughService(), backend.getElectionEventRepository(), backend.getElectionRepository(),
                backend.getMvAreaRepository(), backend.getMvElectionRepository(), backend.getVoteCountCategoryRepository(), backend.getPollingPlaceDomainService(),
                backend.getPollingDistrictRepository(), backend.getVoteCountRepository(), backend.getUserDataService(), backend.getAccessService(),
                backend.getAccessRepository(), backend.getContestServiceBean(), backend.getContestAreaRepository(), backend.getReportCountCategoryRepository(),
                backend.getSigningKeyService(), backend.getSigningKeyRepository(), backend.getCountryService(), backend.getCountyService(),
                backend.getElectionGroupRepository(), backend.getMunicipalityRepository(), backend.getMvElectionReportingUnitsRepository(),
                backend.getCandidateRepository(), backend.getAffiliationService(), backend.getElectionVoteCountCategoryRepository(),
                backend.getLocaleRepository(), backend.getContestRepository(), backend.getElectionEventService(), backend.getPartyCategoryRepository(),
                backend.getPartyRepository(), backend.getReportingUnitRepository(), backend.getTransactionSynchronizationRegistry());
    }

    public ElectionEvent getElectionEvent() {
        return electionEvent;
    }

    private UserData getElectionEventAdmin(final ElectionEvent electionEvent) {
        UserData userData = null;
        try {
            userData = userDataService.getUserData("03011700143", "valghendelse_admin", electionEvent.getId(), electionEvent.getId(),
                    InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            fail(e.getMessage());
        }
        userData.setAccessCache(accessService.findAccessCacheFor(userData));
        return userData;
    }

    public void init() {
        super.init();
        userData = getSysAdminUserData();

        try {
            createElectionEvent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        createAreaHierarchy(electionEvent);
        ElectionGroup electionGroup = createElectionGroup(electionEvent);
        Election election = createElection(electionGroup);

        List<Party> parties = createParties();
        ElectionVoteCountCategory vo = configureElectionEventWithVoteCount(electionGroup, CountCategory.VO.getId());
        ElectionVoteCountCategory fo = configureElectionEventWithVoteCount(electionGroup, CountCategory.FO.getId());
        List<Contest> contests = contestRepository.findByElectionPk(election.getPk());
        for (Contest contest : contests) {
            MvArea mvArea = contestAreaRepository.findMvAreasForContest(contest.getPk()).get(0);
            configureCountCategories(electionGroup, mvArea, vo);

            if (mvArea.getCountyId().equals(OSLO_COUNTY_ID)) {
                contest = configureOslo(electionGroup, fo, contestService, contest, mvArea);
            }
            configureListProposals(contest, parties);
        }

        createReportingUnits();
    }

    /**
     * Oslo is configured to have FO counted on technical polling districts.
     */
    private Contest configureOslo(final ElectionGroup electionGroup, final ElectionVoteCountCategory fo, final ContestServiceBean contestService,
                                  Contest contest,
                                  final MvArea mvArea) {
        contest.setPenultimateRecount(false); // Oslo municipality should not process final count
        contest = contestService.update(getElectionEventAdmin(electionEvent), contest, registry);

        for (MvArea mvAreaMunicipality : mvAreaRepository.findByPathAndLevel(mvArea.getAreaPath(), AreaLevelEnum.MUNICIPALITY.getLevel())) {
            Municipality municipality = mvAreaMunicipality.getMunicipality();

            ReportCountCategory rcc = new ReportCountCategory();
            rcc.setTechnicalPollingDistrictCount(true);
            rcc.setPollingDistrictCount(false);
            rcc.setElectionGroup(electionGroup);
            rcc.setMunicipality(municipality);
            rcc.setEnabled(true);

            rcc.setVoteCountCategory(fo.getVoteCountCategory());
            reportCountCategoryRepository.create(getSysAdminUserData(), rcc);
        }
        return contest;
    }

    @AfterClass
    public void cleanupAfterTest() {
        String valghendelseId = getElectionEvent().getId();
        ValghierarkiSti valghierarkiSti = new no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti(valghendelseId);
        ValggeografiSti valggeografiSti = new ValghendelseSti(valghendelseId);
        voteCountRepository.slettOpptellinger(valghierarkiSti, valggeografiSti, null, null);
        electionEventRepository.deleteElectionEvent(getSysAdminUserData(), getElectionEvent().getPk());
    }

    /**
     * Create a new election event from scratch, so that we have complete control over its configuration
     */
    private void createElectionEvent() throws Exception {
        electionEvent = new ElectionEvent("900010", "Stortingsvalg", userData.getLocale());
        electionEvent.setElectionEventStatus(electionEventRepository.findElectionEventStatusById(ElectionEventStatusEnum.FINISHED_CONFIGURATION.id()));
        ElectionEvent fromElectionEvent = electionEventRepository.findByPk(2L);
        electionEvent = electionEventService.create(userData, electionEvent, true, VotingHierarchy.NONE, CountingHierarchy.NONE, fromElectionEvent, null);
        createSigningKeys();

    }

    private void createSigningKeys() throws IOException, URISyntaxException {
        SigningKey adminSigningKey = new SigningKey();
        adminSigningKey.setElectionEvent(electionEvent);
        adminSigningKey.setKeyDomain(signingKeyRepository.findKeyDomainById("ADMIN_SIGNING"));
        signingKeyService.create(getSysAdminUserData(), adminSigningKey, IOUtil.getBytes(fileFromResources(COUNTING_TEST_P12_PATH)),
                COUNTING_TEST_P12_FILE_NAME, COUNTING_TEST_P12_PASSWORD, electionEvent);
    }

    private void createReportingUnits() {
        createReportingUnit("900010", reportingUnitRepository.findReportingUnitTypeByPk(7));
        createReportingUnit("900010.01", reportingUnitRepository.findReportingUnitTypeByPk(5));
        createReportingUnit("900010.01.01", reportingUnitRepository.findReportingUnitTypeByPk(4));
    }

    private void createReportingUnit(String path, ReportingUnitType reportingUnitType) {
        final MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from(path).tilValghierarkiSti());
        mvElection.setReportingUnit(true);

        mvElectionReportingUnitsRepository.updateMvElectionReportingUnits(userData, new ArrayList<MvElection>() {
            {
                add(mvElection);
            }
        }, reportingUnitType);
    }

    private ElectionGroup createElectionGroup(final ElectionEvent electionEvent) {
        ElectionGroup electionGroup = new ElectionGroup("01", "EG1", electionEvent);
        electionGroupRepository.create(userData, electionGroup);
        return electionGroup;
    }

    private void createAreaHierarchy(final ElectionEvent electionEvent) {
        Country country = new Country("47", "Norge", electionEvent);
        countryService.create(userData, country);

        County countyOstfold = new County("01", "Østfold", country);
        countyOstfold.setLocale(localeRepository.findById(NB_NO));
        County countyOslo = new County(OSLO_COUNTY_ID, "Oslo", country);
        countyOslo.setLocale(localeRepository.findById(NB_NO));

        countyService.create(userData, countyOstfold);
        countyService.create(userData, countyOslo);

        createMunicipalityAndLowerAreas(countyOstfold, "0101", "Halden", "010100");
        createMunicipalityAndLowerAreas(countyOstfold, "0104", "Moss", "010400");
        createMunicipalityAndLowerAreas(countyOslo, AreaPath.OSLO_MUNICIPALITY_ID, "Oslo", "030100");
    }

    private void createMunicipalityAndLowerAreas(final County county, final String municipalityId, final String municipalityName, final String boroughId) {
        Municipality municipality = createMunicipality(county, municipalityId, municipalityName);
        Borough borough = createDefaultBoroughAndPollingDistrict(municipality, boroughId);

        if ("0101".equals(municipalityId)) {
            createPollingDistrict(borough, "0001", "Halden", "Konservativen");
            createPollingDistrict(borough, "0002", "Hjortsberg", "Hjortsberghallen");
        } else if ("0104".equals(municipalityId)) {
            createPollingDistrict(borough, "0001", "Sentrum", "Samfunnshuset");
            createPollingDistrict(borough, "0002", "Jeløy Nord", "Ramberg skole");
        } else if (AreaPath.OSLO_MUNICIPALITY_ID.equals(municipalityId)) {
            createTechnicalPollingDistricts(borough);

            createPollingDistrict(createBorough(municipality, "030101", "Gamle Oslo"), "0101", "Tøyen skole", "Tøyen skole");
            createPollingDistrict(createBorough(municipality, "030102", "Grünerløkka"), "0201", "Hasle skole", "Tøyen skole");
        }
    }

    private void createTechnicalPollingDistricts(final Borough borough) {
        PollingDistrict pollingDistrict = new PollingDistrict("0080", "TK1", borough);
        pollingDistrict.setTechnicalPollingDistrict(true);
        pollingDistrictRepository.create(userData, pollingDistrict);

        pollingDistrict = new PollingDistrict("0081", "TK2", borough);
        pollingDistrict.setTechnicalPollingDistrict(true);
        pollingDistrictRepository.create(userData, pollingDistrict);
    }

    private Borough createDefaultBoroughAndPollingDistrict(final Municipality municipality, final String boroughId) {
        Borough borough = createBorough(municipality, boroughId, "Hele kommunen");

        PollingDistrict pollingDistrict = new PollingDistrict("0000", "Hele kommunen", borough);
        pollingDistrict.setMunicipality(true);
        pollingDistrictRepository.create(userData, pollingDistrict);

        return borough;
    }

    private Borough createBorough(final Municipality municipality, final String boroughId, final String boroughName) {
        Borough borough = new Borough(boroughId, boroughName, municipality);
        boroughService.create(userData, borough);
        return borough;
    }

    private Municipality createMunicipality(final County county, final String municipalityId, final String municipalityName) {
        Municipality municipality = new Municipality(municipalityId, municipalityName, county);
        municipality.setElectronicMarkoffs(true);
        municipality.setLocale(userData.getLocale());
        municipality = municipalityRepository.create(userData, municipality);
        return municipality;
    }

    private void createPollingDistrict(final Borough borough, final String pollingDistrictId, final String pollingDistrictname, final String pollingPlaceName) {
        PollingDistrict pollingDistrict = new PollingDistrict(pollingDistrictId, pollingDistrictname, borough);
        pollingDistrictRepository.create(userData, pollingDistrict);

        PollingPlace pollingPlace = new PollingPlace(pollingDistrictId, pollingPlaceName, "0000", pollingDistrict);
        pollingPlace.setElectionDayVoting(true);
        pollingPlace.setUsingPollingStations(false);
        pollingPlace.setAdvanceVoteInBallotBox(false);

        pollingPlaceDomainService.create(userData, pollingPlace);
    }

    private void configureListProposals(final Contest contest, final List<Party> parties) {
        List<Affiliation> affiliations = new ArrayList<>();
        for (Party party : parties) {
            affiliations.add(affiliationService.createNewAffiliation(userData, contest, party, getSysAdminUserData().getLocale(),
                    BallotStatus.BallotStatusValue.APPROVED.getId()));
        }

        addCandidates(affiliations.get(0), "00000000000", "00000000001");
        addCandidates(affiliations.get(1), "00000000002", "00000000003");
        addCandidates(affiliations.get(2), "00000000004", "00000000005");
        addCandidates(affiliations.get(3), "00000000006", "00000000007");
    }

    private void addCandidates(final Affiliation aff1, final String... ids) {
        Ballot ballot1 = aff1.getBallot();

        int displayOrder = 1;
        for (String id : ids) {
            createCandidate(id, aff1, ballot1, id, id, displayOrder++);
        }
    }

    private void createCandidate(String id, Affiliation aff1, Ballot ballot, String firstName, String lastName, int displayOrder) {
        Candidate candidate = new Candidate();
        candidate.setId(id);
        candidate.setAffiliation(aff1);
        candidate.setBallot(ballot);
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setNameLine();
        candidate.setDisplayOrder(displayOrder);
        candidate.setMaritalStatus(candidateRepository.findMaritalStatusById(MaritalStatus.UOPPGITT));
        candidateRepository.createCandidate(userData, candidate);
    }

    private List<Party> createParties() {
        return new ArrayList<Party>() {
            {
                add(createParty(partyRepository, "TESTIDA", 9998));
                add(createParty(partyRepository, "TESTIDB", 9997));
                add(createParty(partyRepository, "TESTIDC", 9996));
                add(createParty(partyRepository, "TESTIDD", 9995));
            }
        };
    }

    private Party createParty(final PartyRepository partyRepository, final String id, final int shortCode) {
        Party party = new Party(id, shortCode, partyCategoryRepository.findPartyCategoryByPk(1L), electionEvent);
        partyRepository.create(getUserData(), party);
        return party;
    }

    private void configureCountCategories(final ElectionGroup electionGroup, final MvArea mvArea, final ElectionVoteCountCategory vo) {
        for (MvArea mvAreaMunicipality : mvAreaRepository.findByPathAndLevel(mvArea.getAreaPath(), AreaLevelEnum.MUNICIPALITY.getLevel())) {
            Municipality municipality = mvAreaMunicipality.getMunicipality();

            ReportCountCategory rcc = new ReportCountCategory();

            if (municipality.getId().equals(ID_MOSS) && "VO".equals(vo.getVoteCountCategory().getId())) {
                // Lokalt fordelt på krets for Moss
                rcc.setCentralPreliminaryCount(false);
                rcc.setPollingDistrictCount(true);
            } else {
                rcc.setCentralPreliminaryCount(vo.isCentralPreliminaryCount());
                rcc.setPollingDistrictCount(true);
            }
            rcc.setElectionGroup(electionGroup);
            rcc.setMunicipality(municipality);
            rcc.setEnabled(true);
            rcc.setVoteCountCategory(vo.getVoteCountCategory());
            reportCountCategoryRepository.create(getSysAdminUserData(), rcc);
        }
    }

    private ElectionVoteCountCategory configureElectionEventWithVoteCount(final ElectionGroup electionGroup, final String voteCountCategoryId) {
        final ElectionVoteCountCategory vo = new ElectionVoteCountCategory();
        vo.setVoteCountCategory(voteCountCategoryRepository.findById(voteCountCategoryId));
        vo.setElectionGroup(electionGroup);
        vo.setCountCategoryEnabled(true);
        vo.setCentralPreliminaryCount(true);
        vo.setPollingDistrictCount(false);

        electionVoteCountCategoryRepository.update(getSysAdminUserData(), new ArrayList<ElectionVoteCountCategory>() {
            {
                add(vo);
            }
        });

        return vo;
    }

    private Election createElection(final ElectionGroup electionGroup) {
        ElectionType electionType = electionRepository.findElectionTypeById(EvoteConstants.ELECTION_TYPE_CALCULATED);
        Election election = new Election("01", "E1", electionType, AreaLevelEnum.COUNTY.getLevel(), electionGroup);
        election.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
        election.setSettlementFirstDivisor(BigDecimal.valueOf(1.4));
        election.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);
        election.setEndDateOfBirth(LocalDate.now().minusYears(18));
        election.setSingleArea(true);
        election.setRenumber(false);
        election.setStrikeout(true);
        election.setWritein(true);

        Election electionSaved = electionRepository.create(userData, election);
        contestService.createContestsForElection(userData, electionSaved);
        return electionSaved;
    }
}
