package no.valg.eva.admin.counting.domain;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.common.mockups.ReportingUnitMockups;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.VoteCountServiceTest;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.mockup.ContestReportMockups;
import no.valg.eva.admin.counting.mockup.VoteCountMockups;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class VoteCountFactoryTest {

	private static final Long NON_EXISTENT_PK = 2001L;

	private VoteCountFactory voteCountFactory;

	@Mock
	private ManualContestVotingRepository manualContestVotingRepository;
	@Mock
	private VotingRepository votingRepository;
	@Mock
	private ReportCountCategoryRepository reportCountCategoryRepository;
	@Mock
	private ReportingUnitRepository reportingUnitRepository;
	@Mock
	private CountingCodeValueRepository countingCodeValueRepository;
	@Mock
	private VoteCountService voteCountService;
	@Mock
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	@Mock
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;
	
	private CountContext context;
	private MvArea pollingDistrict;
	private MvElection contest;
	private ReportingUnit reportingUnit;
	private AreaPath areaPath;
	private ContestReport contestReport;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void setUp() {
		voteCountFactory = new VoteCountFactory(reportingUnitRepository, reportCountCategoryRepository,
				votingRepository, manualContestVotingRepository, countingCodeValueRepository, voteCountService, antallStemmesedlerLagtTilSideDomainService,
				voteCountStatusendringTrigger);

		areaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT);
		ElectionPath contestPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		CountCategory category = CountCategory.VO;
		context = new CountContext(contestPath, category);
		Municipality municipality = MunicipalityMockups.municipality(true);
		pollingDistrict = MvAreaMockups.pollingDistrictMvArea(municipality);
		contest = MvElectionMockups.contestMvElection();
		reportingUnit = ReportingUnitMockups.reportingUnit(pollingDistrict);

		when(countingCodeValueRepository.findCountQualifierById(CountQualifier.PROTOCOL.getId())).thenReturn(VoteCountMockups.protocolCountQualifier());
		when(countingCodeValueRepository.findCountQualifierById(CountQualifier.PRELIMINARY.getId())).thenReturn(VoteCountMockups.preliminaryCountQualifier());
		when(countingCodeValueRepository.findVoteCountCategoryById(CountCategory.VO.getId())).thenReturn(VoteCountMockups.voteCountCategoryVo());

		contestReport = ContestReportMockups.contestReport(contest.getContest(), reportingUnit);
		contestReport.setReportingUnit(reportingUnit);
		when(voteCountService
				.createContestReport(any(ContestReport.class)))
						.thenReturn(contestReport);
	}

	@Test
    public void createProtocolVoteCount_givenProtocolCountWithElectronicMarkOffsFalseAndNoSpecialCovers_returnsVoteCountWithZeroSpecialAndForeignSpecialCovers() {
		ProtocolCount count = new ProtocolCount("PVO1", areaPath, "", "", true);
		count.setElectronicMarkOffs(false);
		count.setSpecialCovers(0);
		count.setForeignSpecialCovers(null);
		count.setEmergencySpecialCovers(null);
		Affiliation blankAffiliation = new Affiliation();
		VoteCount protocolVoteCount = voteCountFactory.createProtocolVoteCount(reportingUnit, count, pollingDistrict, contest, blankAffiliation);
		assertThat(protocolVoteCount.getSpecialCovers()).isZero();
		assertThat(protocolVoteCount.getForeignSpecialCovers()).isZero();
		assertThat(protocolVoteCount.getEmergencySpecialCovers()).isNull();
	}

	@Test
    public void createProtocolVoteCount_givenProtocolCountWithElectronicMarkOffsTrueAndNoSpecialCovers_returnsVoteCountWithZeroSpecialAndEmergencySpecialCovers() {
		ProtocolCount count = new ProtocolCount("PVO1", areaPath, "", "", true);
		count.setElectronicMarkOffs(true);
		count.setSpecialCovers(0);
		count.setForeignSpecialCovers(null);
		count.setEmergencySpecialCovers(null);
		Affiliation blankAffiliation = new Affiliation();
		VoteCount protocolVoteCount = voteCountFactory.createProtocolVoteCount(reportingUnit, count, pollingDistrict, contest, blankAffiliation);
		assertThat(protocolVoteCount.getSpecialCovers()).isZero();
		assertThat(protocolVoteCount.getEmergencySpecialCovers()).isZero();
		assertThat(protocolVoteCount.getForeignSpecialCovers()).isNull();
	}

	@Test
	public void testCreateProtocolCount() {
		ProtocolCount count = new ProtocolCount("PVO1", areaPath, "", "", true);
		Affiliation blankAffiliation = new Affiliation();
		VoteCount protocolVoteCount = voteCountFactory.createProtocolVoteCount(reportingUnit, count, pollingDistrict, contest, blankAffiliation);
		assertThat(protocolVoteCount.getId()).isEqualTo("PVO1");
	}

	@Test
	public void testCreateProtocolCountNoContestReport() {
		ReportingUnit reportingUnit2 = ReportingUnitMockups.reportingUnit();
		reportingUnit2.setPk(NON_EXISTENT_PK);
		contestReport.setReportingUnit(reportingUnit2);
		ProtocolCount count = new ProtocolCount("PVO1", areaPath, "", "", true);
		Affiliation blankAffiliation = new Affiliation();
		VoteCount protocolVoteCount = voteCountFactory.createProtocolVoteCount(reportingUnit, count, pollingDistrict, contest, blankAffiliation);
		assertThat(protocolVoteCount.getId()).isEqualTo("PVO1");
	}

	@Test
	public void protocolVoteCount_withBallotsForOtherContests_isCreated() {
		final int ballotCountForOtherContests = 100;
		ProtocolCount count = new ProtocolCount("PVO1", areaPath, "anAreaName", "aReportingUnitAreaName", true);
		count.setBallotCountForOtherContests(ballotCountForOtherContests);

		VoteCount protocolVoteCount = voteCountFactory.createProtocolVoteCount(reportingUnit, count, pollingDistrict, contest, new Affiliation());
		assertThat(protocolVoteCount.getBallotsForOtherContests()).isEqualTo(ballotCountForOtherContests);
	}

	@Test
	public void testCreateVoteCount() {
		PreliminaryCount count = (PreliminaryCount) new VoteCountServiceTest().preliminaryCount();
		count.setStatus(CountStatus.NEW);
		Map<String, Ballot> ballots = new HashMap<>();
		VoteCount protocolVoteCount = voteCountFactory.createPreliminaryVoteCount(reportingUnit, context, count, pollingDistrict, contest, ballots);
		assertThat(protocolVoteCount.getId()).isEqualTo("FVO1");
	}

	@Test(expectedExceptions = EvoteException.class)
	public void createPreliminaryVoteCount_withFoAndIngenStemmesedlerLagtTilSide_throwsEvoteException() {
		PreliminaryCount count = (PreliminaryCount) new VoteCountServiceTest().preliminaryCount();
		count.setStatus(CountStatus.NEW);
		Map<String, Ballot> ballots = new HashMap<>();
		context = new CountContext(new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST), CountCategory.FO);
		voteCountFactory.createPreliminaryVoteCount(reportingUnit, context, count, pollingDistrict, contest, ballots);
	}
}
