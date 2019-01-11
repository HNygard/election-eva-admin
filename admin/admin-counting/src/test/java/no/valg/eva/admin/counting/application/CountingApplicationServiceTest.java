package no.valg.eva.admin.counting.application;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.ValidateException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.mockups.BoroughMockups;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.common.mockups.ReportingUnitMockups;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.FinalCountMockups;
import no.valg.eva.admin.counting.domain.VoteCountFactory;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.FinalCountService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindPreliminaryCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindProtocolCountService;
import no.valg.eva.admin.counting.domain.validation.CountValidator;
import no.valg.eva.admin.counting.domain.validation.GetCountsValidator;
import no.valg.eva.admin.counting.mockup.PreliminaryCountMockups;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.evote.constants.EvoteConstants.BALLOT_BLANK;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.counting.builder.FinalCountMockups.finalCount;
import static no.valg.eva.admin.counting.mockup.ProtocolCountMockups.blankProtocolCount;
import static no.valg.eva.admin.counting.mockup.ProtocolCountMockups.loadedProtocolCount;
import static no.valg.eva.admin.counting.mockup.ProtocolCountMockups.newProtocolCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;


public class CountingApplicationServiceTest extends MockUtilsTestCase {

	private CountingApplicationService service;
	private UserData userData;
	private FinalCountService finalCountService;
	private VoteCountFactory voteCountFactory;

	private MvArea operatorMvArea;
	private MvElection contestMvElection;
	private AreaPath areaPath;
	private AreaPath operatorAreaPath;
	private ElectionPath contestPath;
	private CountCategory category;
	private CountContext context;
	private Municipality municipality;
	private MvArea countingMvArea;
	private ReportingUnit reportingUnit;
	private List<ProtocolCount> newProtocolCounts;
	private ProtocolCount newProtocolCount;
	private List<ProtocolCount> notApprovedProtocolCounts;
	private PreliminaryCount notApprovedPreliminaryCount;
	private List<ProtocolCount> approvedProtocolCounts;
	private PreliminaryCount approvedPreliminaryCount;
	private ProtocolCount approvedProtocolCount;
	private Affiliation affiliation;
	private VoteCount voteCount;
	private FinalCount finalCount;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(CountingApplicationService.class);
		userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		finalCountService = mockField("finalCountService", FinalCountService.class);
		voteCountFactory = mockField("voteCountFactory", VoteCountFactory.class);
		mockField("getCountsValidator", GetCountsValidator.class);

		areaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT);
		operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		contestPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		category = CountCategory.VO;
		context = new CountContext(contestPath, category);
		municipality = MunicipalityMockups.municipality(true);
		countingMvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		contestMvElection = MvElectionMockups.contestMvElection();
		reportingUnit = ReportingUnitMockups.reportingUnit(countingMvArea);

		newProtocolCounts = new ArrayList<>();
		newProtocolCount = newProtocolCount();
		newProtocolCounts.add(newProtocolCount);
		notApprovedProtocolCounts = new ArrayList<>();
		notApprovedProtocolCounts.add(blankProtocolCount());
		approvedProtocolCounts = new ArrayList<>();
		approvedProtocolCount = loadedProtocolCount(areaPath);
		approvedProtocolCounts.add(approvedProtocolCount);

		notApprovedPreliminaryCount = PreliminaryCountMockups.blankPreliminaryCount();
		approvedPreliminaryCount = PreliminaryCountMockups.loadedPreliminaryCount();
		approvedPreliminaryCount.setRequiredProtocolCount(true);

		finalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET);

		affiliation = new Affiliation();
		voteCount = mock(VoteCount.class, RETURNS_DEEP_STUBS);
		when(voteCount.getId()).thenReturn("PVO1");

		operatorMvArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(userData.getOperatorMvArea()).thenReturn(operatorMvArea);
		when(operatorMvArea.getAreaPath()).thenReturn(operatorAreaPath.path());
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(areaPath)).thenReturn(countingMvArea);
	}

	@Test
    public void getCounts_whenUserCanAccessReportingUnitForPreliminaryCount_returnsCountsIncludingPreliminaryCount() {
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				approvedProtocolCounts);
		when(getInjectMock(FindPreliminaryCountService.class).findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				approvedPreliminaryCount);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(contestMvElection);

		Counts counts = service.getCounts(userData, context, countingAreaPath);

		assertThat(counts.hasPreliminaryCount()).isTrue();
	}

	@Test
    public void getCounts_whenProtocolCountsNewAndUserCanAccessReportingUnitForPreliminaryCount_returnsCountsWithNewProtocolCounts() {
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				newProtocolCounts);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(contestMvElection);

		Counts counts = service.getCounts(userData, context, countingAreaPath);

		assertThat(counts.getProtocolCounts()).containsExactly(newProtocolCount);
	}

	@Test
    public void getCounts_always_includesMunicipalityName() {
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());
		String municipalityName = MvAreaMockups.MV_AREA_MUNICIPALITY_NAME;

		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				newProtocolCounts);

		Counts counts = service.getCounts(userData, context, countingAreaPath);

		assertThat(counts.getMunicipalityName()).isEqualTo(municipalityName);
	}

	@Test
	public void testNoApprovedProtocolCounts() {
		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(false);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				notApprovedProtocolCounts);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(contestMvElection);
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		assertThat(service.getCounts(userData, context, countingAreaPath).isProtocolCountsApproved()).isFalse();
	}

	@Test
	public void testProtocolCounts() {
		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(false);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				notApprovedProtocolCounts);
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		assertThat(service.getCounts(userData, context, countingAreaPath).hasProtocolCounts()).isTrue();
	}

	@Test
	public void testProtocolCountsAndPreliminaryCountAndTellekrets() {
		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(false);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				notApprovedProtocolCounts);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(FindPreliminaryCountService.class).findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				notApprovedPreliminaryCount);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(contestMvElection);
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());
		countingMvArea.setParentPollingDistrict(true);

		Counts counts = service.getCounts(userData, context, countingAreaPath);

		assertThat(counts.hasProtocolCounts()).isTrue();
		assertThat(counts.hasPreliminaryCount()).isTrue();
		assertThat(counts.isTellekrets()).isTrue();
	}

	@Test
	public void testProtocolAndPreliminaryCount() {
		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection)).thenReturn(CountingMode.BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(true);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				notApprovedProtocolCounts);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(FindPreliminaryCountService.class).findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				notApprovedPreliminaryCount);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(contestMvElection);
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		assertThat(service.getCounts(userData, context, countingAreaPath).hasProtocolAndPreliminaryCount()).isTrue();
	}

	@Test
	public void getCounts_includeFinalCountsIsTrue_hasFinalCounts() {
		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(false);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				approvedProtocolCounts);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(FindPreliminaryCountService.class).findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				approvedPreliminaryCount);
		when(getInjectMock(VoteCountService.class).includeMunicipalityFinalCounts(eq(operatorAreaPath), anyBoolean())).thenReturn(true);
		List<FinalCount> municipalityFinalCounts = singletonList(finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET));
		when(getInjectMock(FindCountService.class).findMunicipalityFinalCounts(operatorAreaPath, contestMvElection, countingMvArea, category)).thenReturn(
				municipalityFinalCounts);
		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		assertThat(service.getCounts(userData, context, countingAreaPath).hasFinalCounts()).isTrue();
	}

	@Test
    public void getCounts_whenUserCanAccessReportingUnitForCountyFinalCount_returnCountyFinalCounts() {

		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(false);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				approvedProtocolCounts);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(true);
		when(getInjectMock(ReportingUnitRepository.class).existsFor(operatorAreaPath, FYLKESVALGSTYRET)).thenReturn(true);
		List<FinalCount> municipalityFinalCounts = singletonList(finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET));
		when(getInjectMock(FindCountService.class).findMunicipalityFinalCounts(operatorAreaPath, contestMvElection, countingMvArea, category))
				.thenReturn(municipalityFinalCounts);
		List<FinalCount> countyFinalCounts = singletonList(finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET));
		when(getInjectMock(FindCountService.class).findCountyFinalCounts(operatorAreaPath, contestMvElection, countingMvArea, category))
				.thenReturn(countyFinalCounts);

		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		assertThat(service.getCounts(userData, context, countingAreaPath).hasCountyFinalCounts()).isTrue();

	}

	@Test
    public void getCounts_whenUserCannotAccessReportingUnitForCountyFinalCount_returnNoCountyFinalCounts() {

		when(getInjectMock(VoteCountService.class).useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)).thenReturn(false);
		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).thenReturn(
				approvedProtocolCounts);
		when(getInjectMock(VoteCountService.class).userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea))
				.thenReturn(false);
		when(getInjectMock(ReportingUnitRepository.class).existsFor(operatorAreaPath, ReportingUnitTypeId.VALGSTYRET)).thenReturn(true);
		when(getInjectMock(ReportingUnitRepository.class).existsFor(operatorAreaPath, FYLKESVALGSTYRET)).thenReturn(false);
		List<FinalCount> municipalityFinalCounts = singletonList(finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET));
		when(getInjectMock(FindCountService.class).findMunicipalityFinalCounts(operatorAreaPath, contestMvElection, countingMvArea, category)).thenReturn(
				municipalityFinalCounts);

		AreaPath countingAreaPath = AreaPath.from(countingMvArea.getAreaPath());

		assertThat(service.getCounts(userData, context, countingAreaPath).hasCountyFinalCounts()).isFalse();

	}

	@Test
	public void saveProtocolCountWhenCountDoesNotExist() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedProtocolCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(false);
		when(getInjectMock(AffiliationRepository.class).getAffiliationById(BALLOT_BLANK, null)).thenReturn(affiliation);
		when(voteCountFactory.createProtocolVoteCount(any(ReportingUnit.class), eq(approvedProtocolCount), eq(countingMvArea), any(MvElection.class),
				any(Affiliation.class))).thenReturn(voteCount);

		assertThat(service.saveCount(userData, context, approvedProtocolCount).getStatus()).isEqualTo(CountStatus.SAVED);
	}

	@Test
	public void saveProtocolCountWhenCountExists() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedProtocolCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);
		assertThat(service.saveCount(userData, context, approvedProtocolCount).getStatus()).isEqualTo(CountStatus.SAVED);
	}

	@Test
	public void approveProtocolCount() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedProtocolCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);
		assertThat(service.approveCount(userData, context, approvedProtocolCount).getStatus()).isEqualTo(CountStatus.APPROVED);
	}

	@Test
	public void revokeProtocolCount() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedProtocolCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);
		assertThat(service.revokeCount(userData, context, approvedProtocolCount).getStatus()).isEqualTo(CountStatus.REVOKED);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void savePreliminaryCountWhenCountDoesNotExist() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedPreliminaryCount), any(MvElection.class), any(MvArea.class),
				any(ReportingUnit.class))).thenReturn(false);
		when(getInjectMock(AffiliationRepository.class).findApprovedByContest(anyLong())).thenReturn(new ArrayList<>());
		when(voteCountFactory.createPreliminaryVoteCount(any(ReportingUnit.class), eq(context), eq(approvedPreliminaryCount), eq(countingMvArea),
				any(MvElection.class),
				any(Map.class))).thenReturn(voteCount);

		assertThat(service.saveCount(userData, context, approvedPreliminaryCount).getStatus()).isEqualTo(CountStatus.SAVED);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void savePreliminaryCountWhenCountExists() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedPreliminaryCount), any(MvElection.class), any(MvArea.class), eq(reportingUnit)))
				.thenReturn(true);
		when(voteCountFactory.createPreliminaryVoteCount(any(ReportingUnit.class), eq(context), eq(approvedPreliminaryCount), eq(countingMvArea),
				any(MvElection.class),
				anyMap())).thenReturn(voteCount);

		assertThat(service.saveCount(userData, context, approvedPreliminaryCount).getStatus()).isEqualTo(CountStatus.SAVED);
	}

	@Test
	public void approvePreliminaryCount() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedPreliminaryCount), any(MvElection.class), any(MvArea.class),
				any(ReportingUnit.class))).thenReturn(true);
		when(
				getInjectMock(FindProtocolCountService.class).findProtocolCounts(any(AreaPath.class), any(CountContext.class), any(MvArea.class),
						any(MvElection.class))).thenReturn(approvedProtocolCounts);
		assertThat(service.approveCount(userData, context, approvedPreliminaryCount).getStatus()).isEqualTo(CountStatus.APPROVED);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void approvePreliminaryCount_whenProtocolCountNotApproved_thenReject() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedPreliminaryCount), any(MvElection.class), any(MvArea.class),
				any(ReportingUnit.class))).thenReturn(true);
		when(
				getInjectMock(FindProtocolCountService.class).findProtocolCounts(any(AreaPath.class), any(CountContext.class), any(MvArea.class),
						any(MvElection.class))).thenReturn(notApprovedProtocolCounts);
		service.approveCount(userData, context, approvedPreliminaryCount);
	}

	@Test
	public void revokePreliminaryCount() {
		when(getInjectMock(VoteCountService.class).countExists(eq(approvedPreliminaryCount), any(MvElection.class), any(MvArea.class),
				any(ReportingUnit.class))).thenReturn(true);
		assertThat(service.revokeCount(userData, context, approvedPreliminaryCount).getStatus()).isEqualTo(CountStatus.REVOKED);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saveFinalCountWhenCountDoesNotExist() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", VALGSTYRET);

		when(getInjectMock(VoteCountService.class).countExists(eq(aFinalCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(false);
		when(getInjectMock(AffiliationRepository.class).findApprovedByContest(anyLong())).thenReturn(new ArrayList<>());
		when(finalCountService.saveNewFinalCount(any(ReportingUnit.class), eq(context), eq(aFinalCount), eq(countingMvArea), anyMap(),
				any(MvElection.class))).thenReturn(aFinalCount);

		assertThat(service.saveCount(userData, context, aFinalCount)).isEqualTo(aFinalCount);
	}

	@Test
	public void saveFinalCountWhenCountExists() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", VALGSTYRET);

		when(getInjectMock(VoteCountService.class).countExists(eq(aFinalCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);
		assertThat(service.saveCount(userData, context, aFinalCount)).isEqualTo(aFinalCount);
	}

	@Test
	public void approveCount_countGetsStatusApproved() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", VALGSTYRET);

		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class)).getContest().isOnBoroughLevel()).thenReturn(false);

		when(getInjectMock(VoteCountService.class).countExists(eq(aFinalCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);
		assertThat(service.approveCount(userData, context, aFinalCount).getStatus()).isEqualTo(CountStatus.APPROVED);
	}

	@Test
	public void approveCount_whenNoRejectedBallotsAndFinalCountNotReadyForSettlement_returnsCountApproved() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", VALGSTYRET);
		aFinalCount.getRejectedBallotCounts().get(0).setCount(0);
		when(getInjectMock(VoteCountService.class).isFinalCountReadyForSettlement(contestPath, aFinalCount)).thenReturn(false);
		when(getInjectMock(VoteCountService.class).countExists(eq(aFinalCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);

		FinalCount result = service.approveCount(userData, context, aFinalCount);

		assertThat(result.getStatus()).isEqualTo(CountStatus.APPROVED);
		assertThat(result.isRejectedBallotsProcessed()).isTrue();
	}

	@Test
	public void approveCount_whenNoRejectedBallotsAndFinalCountReadyForSettlement_returnsCountToSettlement() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET);
		aFinalCount.getRejectedBallotCounts().get(0).setCount(0);
		when(getInjectMock(VoteCountService.class).isFinalCountReadyForSettlement(contestPath, aFinalCount)).thenReturn(true);
		when(getInjectMock(VoteCountService.class).countExists(eq(aFinalCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);

		FinalCount result = service.approveCount(userData, context, aFinalCount);

		assertThat(result.getStatus()).isEqualTo(CountStatus.TO_SETTLEMENT);
		assertThat(result.isRejectedBallotsProcessed()).isTrue();
	}

	@Test
	public void revokeCount_countGetsStatusRevoked() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", VALGSTYRET);

		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(operatorAreaPath, ReportingUnitTypeId.VALGSTYRET)).thenReturn(reportingUnit);
		when(getInjectMock(VoteCountService.class).countExists(eq(aFinalCount), any(MvElection.class), any(MvArea.class), any(ReportingUnit.class)))
				.thenReturn(true);
		assertThat(service.revokeCount(userData, context, aFinalCount).getStatus()).isEqualTo(CountStatus.REVOKED);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void saveFinalCount_reportingUnitTypeIdInvalid_throwsException() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", STEMMESTYRET);
		service.saveCount(userData, context, aFinalCount);
	}

	@Test
	public void saveFinalCount_reportingUnitTypeIdIsValid_doesNotThrowException() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", OPPTELLINGSVALGSTYRET);
		service.saveCount(userData, context, aFinalCount);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void saveFinalCountShouldThrowExceptionIfReportingUnitIdAndOperatorRoleAreaLevelCombinationIsInvalid() {
		FinalCount aFinalCount = FinalCountMockups.finalCount(CountStatus.APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET);

		when(operatorMvArea.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY.getLevel());
		doThrow(new ValidateException()).when(operatorMvArea).validateAreaLevel(AreaLevelEnum.COUNTY);
		service.saveCount(userData, context, aFinalCount);
	}

	@Test
	public void getCounts_boroughElectionAndOneExistingCount_returnsOneProtocolCount() {

		// I denne testen kan vi godt mocke
		// findProtocolCountService
		// Innmaten i findProtocolCountService tester vi i FindProtocolCountServiceTest

		Borough borough = BoroughMockups.borough(municipality);
		MvArea boroughMvArea = MvAreaMockups.boroughMvArea(borough);
		MvElection boroughElection = MvElectionMockups.contestMvElection();
		ContestArea contestArea = new ContestArea();
		contestArea.setMvArea(boroughMvArea);
		boroughElection.getContest().getContestAreaSet().add(contestArea);
		CountContext context = new CountContext(new ElectionPath(boroughElection.getPath()), CountCategory.BF);

		AreaPath countingAreaPath = AreaPath.from(boroughMvArea.getPath());

		when(getInjectMock(VoteCountService.class).countingMode(context, municipality, contestMvElection))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(userData.getOperatorMvArea()).thenReturn(operatorMvArea);
		when(operatorMvArea.getAreaPath()).thenReturn(operatorAreaPath.path());
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(boroughElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(countingAreaPath)).thenReturn(boroughMvArea);

		when(getInjectMock(FindProtocolCountService.class).findProtocolCounts(operatorAreaPath, context, boroughMvArea, boroughElection)).thenReturn(
				approvedProtocolCounts);

		Counts counts = service.getCounts(userData, context, countingAreaPath);

		assertNotNull(counts.getFirstProtocolCount());
	}

	@Test
    public void findApprovedFinalCount_whenElectionEventAdminAndFylkesvalgstyretAndApprovedCountyFinalCount_returnsApprovedFinalCount() {
		when(userData.isElectionEventAdminUser()).thenReturn(true);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(
				getInjectMock(FindCountService.class).findApprovedCountyFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class),
						any(MvElection.class)))
								.thenReturn(finalCount);

		ApprovedFinalCountRef mock = mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS);
		when(mock.reportingUnitTypeId()).thenReturn(FYLKESVALGSTYRET);
		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock);

		assertThat(approvedFinalCount).isNotNull();
	}

	@Test
    public void findApprovedFinalCount_whenElectionEventAdminAndNoApprovedMunicipalityFinalCount_returnsNull() {
		when(userData.isElectionEventAdminUser()).thenReturn(true);
		when(userData.getOperatorAreaLevel()).thenReturn(ROOT);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(getInjectMock(FindCountService.class)
				.findApprovedMunicipalityFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class), any(MvElection.class)))
						.thenReturn(null);

		ApprovedFinalCountRef mock = mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS);
		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock);

		assertThat(approvedFinalCount).isNull();
	}

	@Test
    public void findApprovedFinalCount_whenCountyOperatorAndApprovedCountyFinalCount_returnsApprovedFinalCount() {
		when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(
				getInjectMock(FindCountService.class).findApprovedCountyFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class),
						any(MvElection.class)))
								.thenReturn(finalCount);

		ApprovedFinalCountRef mock = mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS);
		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock);

		assertThat(approvedFinalCount).isNotNull();
	}

	@Test
    public void findApprovedFinalCount_whenCountyOperatorAndNoApprovedCountyFinalCount_returnsNull() {
		when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(getInjectMock(FindCountService.class)
				.findApprovedCountyFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class), any(MvElection.class)))
						.thenReturn(null);

		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS));

		assertThat(approvedFinalCount).isNull();
	}

	@Test
    public void findApprovedFinalCount_whenMunicipalityOperatorAndApprovedMunicipalityFinalCount_returnsApprovedFinalCount() {
		when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(getInjectMock(FindCountService.class)
				.findApprovedMunicipalityFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class), any(MvElection.class)))
						.thenReturn(finalCount);

		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS));

		assertThat(approvedFinalCount).isNotNull();
	}

	@Test
    public void findApprovedFinalCount_whenMunicipalityOperatorAndNoApprovedMunicipalityFinalCount_returnsNull() {
		when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(getInjectMock(FindCountService.class)
				.findApprovedMunicipalityFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class), any(MvElection.class)))
						.thenReturn(null);

		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS));

		assertThat(approvedFinalCount).isNull();
	}

	@Test
    public void findApprovedFinalCount_whenRootOperatorAndApprovedMunicipalityFinalCount_returnsApprovedFinalCount() {
		when(userData.getOperatorAreaLevel()).thenReturn(ROOT);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		when(getInjectMock(FindCountService.class)
				.findApprovedMunicipalityFinalCount(any(AreaPath.class), any(CountContext.class), any(MvArea.class), any(MvElection.class)))
						.thenReturn(finalCount);

		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS));

		assertThat(approvedFinalCount).isNotNull();
	}

	@Test
    public void findApprovedFinalCount_whenRootOperatorAndSamiElection_returnsResultFromFindApprovedCountyFinalCount() {
		when(userData.isSamiElectionCountyUser()).thenReturn(true);
		MvElection fakeContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(fakeContestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);

		service.findApprovedFinalCount(userData, mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS));

		verify(getInjectMock(FindCountService.class), times(1)).findApprovedCountyFinalCount(any(AreaPath.class), any(CountContext.class),
				any(MvArea.class), any(MvElection.class));
	}

	@Test
    public void findApprovedFinalCount_whenPollingDistrictOperator_returnsNull() {
		when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.POLLING_DISTRICT);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(contestMvElection);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);

		FinalCount approvedFinalCount = service.findApprovedFinalCount(userData, mock(ApprovedFinalCountRef.class, RETURNS_DEEP_STUBS));

		assertThat(approvedFinalCount).isNull();
	}

	@Test
    public void updateFinalCountStatusToSettlement_whenCountyOperatorAndApprovedFinalCountAndCountRefWithReportingUnitTypeIdNull_countIsSetToSettlement() {
		final int[] invokeIndexHolder = new int[1];
		final CountStatus[] countStatusHolder = new CountStatus[1];
		final ReportingUnitTypeId[] reportingUnitTypeIdHolder = new ReportingUnitTypeId[1];
		service = new CountingApplicationService() {
			@Override
			public FinalCount findApprovedFinalCount(UserData userData, ApprovedFinalCountRef ref) {
				FinalCount fakeFinalCount = mock(FinalCount.class);
				when(fakeFinalCount.getReportingUnitTypeId()).thenReturn(FYLKESVALGSTYRET);
				return fakeFinalCount;
			}

			@Override
			FinalCount saveFinalCount(UserData userData, CountContext context, FinalCount finalCount, CountStatus status, CountValidator<FinalCount> validator,
					ReportingUnitTypeId reportingUnitTypeId) {
				int invokeIndex = invokeIndexHolder[0]++;
				countStatusHolder[invokeIndex] = status;
				reportingUnitTypeIdHolder[invokeIndex] = reportingUnitTypeId;
				return null;
			}
		};
		when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		ApprovedFinalCountRef fakeRef = mock(ApprovedFinalCountRef.class);
		when(fakeRef.reportingUnitTypeId()).thenReturn(null);

		service.updateFinalCountStatusToSettlement(userData, fakeRef);

		int firstInvokeIndex = 0;
		assertThat(countStatusHolder[firstInvokeIndex]).isEqualTo(CountStatus.TO_SETTLEMENT);
	}

	@Test(expectedExceptions = IllegalStateException.class)
    public void updateFinalCountStatusToSettlement_reportingUnitsDiffer_illegalStateExceptionIsThrown() {
		CountingApplicationService countingApplicationService = new CountingApplicationService() {
			@Override
			public FinalCount findApprovedFinalCount(UserData userData, ApprovedFinalCountRef ref) {
				FinalCount fakeFinalCount = mock(FinalCount.class);
				when(fakeFinalCount.getReportingUnitTypeId()).thenReturn(VALGSTYRET);
				return fakeFinalCount;
			}
		};

		CountContext aContext = new CountContext(new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST), CountCategory.VO);
		AreaPath anAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT);
		ApprovedFinalCountRef approvedFinalCountRef = new ApprovedFinalCountRef(ReportingUnitTypeId.FYLKESVALGSTYRET, aContext, anAreaPath);

		countingApplicationService.updateFinalCountStatusToSettlement(userData, approvedFinalCountRef);
	}
}
