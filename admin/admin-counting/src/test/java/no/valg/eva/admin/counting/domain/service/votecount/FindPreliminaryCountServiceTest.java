package no.valg.eva.admin.counting.domain.service.votecount;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.common.mockups.ReportingUnitMockups;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.mockup.ContestReportMockups;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountStatus.NEW;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.voteCountCategoryVo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FindPreliminaryCountServiceTest extends MockUtilsTestCase {
    private static final int LATE_VALIDATION_COVERS = 1;
    private static final long MARK_OFF_COUNT = 1L;
	private static final String CONTEST_PATH = "111111.11.11.111111";
	private FindPreliminaryCountService service;

	private MvElection contestMvElection;
	private AreaPath operatorAreaPath;
	private ElectionPath contestPath;
	private CountContext context;
	private Municipality municipality;
	private MvArea countingMvArea;
	private ReportingUnit reportingUnit;
	private VoteCount voteCount;
	private ReportingUnitTypeId typeId;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(FindPreliminaryCountService.class);

		operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		contestPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		CountCategory category = CountCategory.VO;
		context = new CountContext(contestPath, category);
		municipality = MunicipalityMockups.municipality(true);
		countingMvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		contestMvElection = MvElectionMockups.contestMvElection();
		reportingUnit = ReportingUnitMockups.reportingUnit(countingMvArea);

		ContestReport contestReport = ContestReportMockups.contestReport(contestMvElection.getContest(), reportingUnit);

		voteCount = new VoteCount();
		voteCount.setId("FVO1");
		voteCount.setVoteCountStatus(new VoteCountStatus(SAVED.getId()));
		voteCount.setVoteCountCategory(voteCountCategoryVo());
		voteCount.setRejectedBallots(1);
		voteCount.setManualCount(true);
		voteCount.setContestReport(contestReport);

		typeId = ReportingUnitTypeId.VALGSTYRET;
		Mockito.reset(getInjectMock(VoteCountService.class));
		when(getInjectMock(VoteCountService.class).reportingUnitTypeForPreliminaryCount(context, municipality, contestMvElection)).thenReturn(typeId);
		when(getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(typeId, operatorAreaPath, countingMvArea)).thenReturn(operatorAreaPath);
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(operatorAreaPath, ReportingUnitTypeId.VALGSTYRET)).thenReturn(reportingUnit);
	}

	@Test
	public void testNoExistingVoteCount() {
		when(getInjectMock(VoteCountService.class).findVoteCount(any(ReportingUnit.class), any(CountContext.class), any(MvArea.class),
				any(MvElection.class), any(CountQualifier.class))).thenReturn(null);
		assertThat(service.findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection).getStatus()).isEqualTo(NEW);
	}

	@Test
	public void testExistingVoteCount() {
		when(getInjectMock(VoteCountService.class).findVoteCount(reportingUnit, context, countingMvArea, contestMvElection, CountQualifier.PRELIMINARY))
				.thenReturn(voteCount);
		assertThat(service.findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection).getStatus()).isEqualTo(SAVED);
	}

	@Test
	public void testExistingVoteCountNotVo() {
		CountContext otherContext = new CountContext(contestPath, CountCategory.FO);
		when(getInjectMock(VoteCountService.class).reportingUnitTypeForPreliminaryCount(otherContext, municipality, contestMvElection)).thenReturn(typeId);
		when(getInjectMock(VoteCountService.class).findVoteCount(reportingUnit, otherContext, countingMvArea, contestMvElection, CountQualifier.PRELIMINARY))
				.thenReturn(voteCount);

		assertThat(service.findPreliminaryCount(operatorAreaPath, otherContext, countingMvArea, contestMvElection).getStatus()).isEqualTo(SAVED);
	}

	@Test
    public void findPreliminaryCount_whenFoAndByTechnicalPollingDistrict_returnPreliminaryCountWithExpectedBallotCountZero() {
		CountContext context = new CountContext(contestPath, CountCategory.FO);

		when(getInjectMock(VoteCountService.class)
				.findVoteCount(any(ReportingUnit.class), any(CountContext.class), any(MvArea.class), any(MvElection.class), any(CountQualifier.class)))
				.thenReturn(null);
		when(getInjectMock(VoteCountService.class).countingMode(any(CountContext.class), any(Municipality.class), any(MvElection.class)))
				.thenReturn(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);

		PreliminaryCount preliminaryCount = service.findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection);

		assertThat(preliminaryCount.getExpectedBallotCount()).isEqualTo(0);
	}

	@Test
    public void findPreliminaryCount_whenFoAndByTechnicalPollingDistrictAndFirstTechnicalPollingDistrict_returnPreliminaryCountForFirstTechnicalPollingDistrict() {
		CountContext context = new CountContext(contestPath, CountCategory.FO);
		MvElection stubContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		MvArea stubOtherMvArea = mock(MvArea.class);
		VoteCount stubVoteCount1 = stubVoteCount(countingMvArea);
		VoteCount stubVoteCount2 = stubVoteCount(stubOtherMvArea, 11, 22);

		initContestMvElection(stubContestMvElection);
		initVoteCountService(stubVoteCount1, stubVoteCount2);
		initAntallStemmesedlerLagtTilSideDomainService();

		PreliminaryCount preliminaryCount = service.findPreliminaryCount(operatorAreaPath, context, countingMvArea, stubContestMvElection);

		assertThat(preliminaryCount.getMarkOffCount()).isEqualTo((int) MARK_OFF_COUNT);
		assertThat(preliminaryCount.getLateValidationCovers()).isEqualTo(LATE_VALIDATION_COVERS);
		assertThat(preliminaryCount.getTotalBallotCountForOtherPollingDistricts()).isEqualTo(33);
	}

	private VoteCount stubVoteCount(MvArea mvArea) {
		return stubVoteCount(mvArea, null, null);
	}

	private VoteCount stubVoteCount(MvArea mvArea, Integer rejectedBallots, Integer approvedBallots) {
		VoteCount stubVoteCount = stub(VoteCount.class);
		when(stubVoteCount.getMvArea()).thenReturn(mvArea);
		when(stubVoteCount.getRejectedBallots()).thenReturn(rejectedBallots);
		when(stubVoteCount.getApprovedBallots()).thenReturn(approvedBallots);
		return stubVoteCount;
	}

	private void initVoteCountService(VoteCount... voteCounts) {
		VoteCountService voteCountService = getInjectMock(VoteCountService.class);
		when(voteCountService
				.findVoteCount(any(ReportingUnit.class), any(CountContext.class), any(MvArea.class), any(MvElection.class), any(CountQualifier.class)))
				.thenReturn(null);
		when(voteCountService.countingMode(any(CountContext.class), any(Municipality.class), any(MvElection.class)))
				.thenReturn(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
		when(voteCountService
				.markOffCountForPreliminaryCount(any(CountContext.class), any(MvElection.class), any(MvArea.class), any(CountCategory.class)))
				.thenReturn(MARK_OFF_COUNT);
		when(voteCountService
				.findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(any(ReportingUnit.class), any(ElectionPath.class), any(CountCategory.class)))
				.thenReturn(asList(voteCounts));
		ContestReport stubContestReport = mock(ContestReport.class);
		when(voteCountService
				.findContestReport(any(ReportingUnit.class), any(MvElection.class)))
				.thenReturn(stubContestReport);
	}

	private void initContestMvElection(MvElection stubContestMvElection) {
		when(stubContestMvElection.getPath()).thenReturn(CONTEST_PATH);
		when(stubContestMvElection.getElectionPath()).thenReturn(CONTEST_PATH);
	}

	private void initAntallStemmesedlerLagtTilSideDomainService() {
		AntallStemmesedlerLagtTilSideDomainService stubAntallStemmesedlerLagtTilSideDomainService = getInjectMock(AntallStemmesedlerLagtTilSideDomainService.class);
		AntallStemmesedlerLagtTilSide stubAntallStemmesedlerLagtTilSide = stub(AntallStemmesedlerLagtTilSide.class);

		when(stubAntallStemmesedlerLagtTilSideDomainService.hentAntallStemmesedlerLagtTilSide(any(Municipality.class))).thenReturn(stubAntallStemmesedlerLagtTilSide);
		when(stubAntallStemmesedlerLagtTilSide.getTotaltAntallStemmesedlerLagtTilSideForValg()).thenReturn(LATE_VALIDATION_COVERS);
	}

	@Test
    public void findPreliminaryCount_whenFoAndByTechnicalPollingDistrictAndNotFirstTechnicalPollingDistrict_returnPreliminaryCountForOtherTechnicalPollingDistrict() {
		CountContext context = new CountContext(contestPath, CountCategory.FO);
		VoteCountService voteCountService = getInjectMock(VoteCountService.class);

		when(voteCountService
				.findVoteCount(any(ReportingUnit.class), any(CountContext.class), any(MvArea.class), any(MvElection.class), any(CountQualifier.class)))
				.thenReturn(null);
		when(voteCountService.countingMode(any(CountContext.class), any(Municipality.class), any(MvElection.class)))
				.thenReturn(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
		when(voteCountService
				.markOffCountForPreliminaryCount(any(CountContext.class), any(MvElection.class), any(MvArea.class), any(CountCategory.class)))
				.thenReturn(null);

		PreliminaryCount preliminaryCount = service.findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection);

		assertThat(preliminaryCount.getMarkOffCount()).isNull();
	}
}

