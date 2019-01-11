package no.valg.eva.admin.settlement.application;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.dto.CandidateVoteCountDto;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.rbac.UserDataMockups;
import no.valg.eva.admin.common.settlement.model.AffiliationVoteCount;
import no.valg.eva.admin.common.settlement.model.BallotCountSummary;
import no.valg.eva.admin.common.settlement.model.BallotInfo;
import no.valg.eva.admin.common.settlement.model.CandidateSeat;
import no.valg.eva.admin.common.settlement.model.SettlementStatus;
import no.valg.eva.admin.common.settlement.model.SettlementSummary;
import no.valg.eva.admin.common.settlement.model.SimpleBallotCount;
import no.valg.eva.admin.common.settlement.model.SplitBallotCount;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.builder.FinalCountMockups;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.settlement.CountCategoryDomainService;
import no.valg.eva.admin.counting.domain.service.settlement.CountingAreaDomainService;
import no.valg.eva.admin.counting.domain.service.votecount.FindCountService;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.settlement.application.mapper.AffiliationVoteCountMapper;
import no.valg.eva.admin.settlement.application.mapper.CandidateSeatMapper;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static no.valg.eva.admin.common.mockups.BoroughMockups.boroughGamleOslo;
import static no.valg.eva.admin.common.mockups.ContestMockups.contest;
import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.ElectionMockups.defaultElection;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.MV_AREA_PATH_MUNICIPALITY;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.MV_AREA_PATH_OSLO_MUNICIPALITY;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.boroughMvArea;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.municipalityMvArea;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.pollingDistrictMvArea;
import static no.valg.eva.admin.common.mockups.MvElectionMockups.contestMvElection;
import static no.valg.eva.admin.common.mockups.OperatorMockups.defaultOperator;
import static no.valg.eva.admin.common.mockups.OperatorRoleMockups.operatorRole;
import static no.valg.eva.admin.common.mockups.RoleMockups.defaultRole;
import static no.valg.eva.admin.counting.builder.AreaPathMockups.AREA_PATH_BOROUGH_GAMLE_OSLO;
import static no.valg.eva.admin.counting.builder.AreaPathMockups.areaPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SettlementApplicationServiceTest extends MockUtilsTestCase {

	private static final String ERROR_MSG_USER_NOT_ACCESS_TO_BOROUGH =
			"Operator with area path 730001.47.01.0101 has not access to contest area with path 730001.47.03.0301.030101.";
	private static final String MV_ELECTION_PATH_GAMLE_OSLO = "730001.01.03.030101";
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private SettlementApplicationService settlementApplicationService;

	@BeforeMethod
	public void setUp() throws Exception {
		settlementApplicationService = initializeMocks(SettlementApplicationService.class);
	}

	@Test
	public void settlementStatusMap_whenUserCanAccessReportingUnitForBoroughElection_statusForSixBoroughCountCategories() {
		UserData userData = mockMunicipalityUser(MV_AREA_PATH_OSLO_MUNICIPALITY);
		ElectionPath boroughPath = new ElectionPath(MV_ELECTION_PATH_GAMLE_OSLO);
		initServices();

		Map<CountCategory, SettlementStatus> settlementStatusMap = settlementApplicationService.settlementStatusMap(userData, boroughPath);

		assertThat(settlementStatusMap.size()).isEqualTo(6);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ERROR_MSG_USER_NOT_ACCESS_TO_BOROUGH)
	public void settlementStatusMap_whenUserCannotAccessReportingUnitForBoroughElection_throwsIllegalArgumentException() {
		UserData userData = mockMunicipalityUser(MV_AREA_PATH_MUNICIPALITY);
		ElectionPath boroughPath = new ElectionPath(MV_ELECTION_PATH_GAMLE_OSLO);
		initServices();

		settlementApplicationService.settlementStatusMap(userData, boroughPath);
	}

	@Test
	public void settlementStatusMap_whenBoroughHasThreePollingDistricts_countCategoryVOHasThreeCountingAreasAllOthersHaveOne() {
		UserData userData = mockMunicipalityUser(MV_AREA_PATH_OSLO_MUNICIPALITY);
		ElectionPath boroughPath = new ElectionPath(MV_ELECTION_PATH_GAMLE_OSLO);
		initServices();

		Map<CountCategory, SettlementStatus> settlementStatusMap = settlementApplicationService.settlementStatusMap(userData, boroughPath);

		assertThat(settlementStatusMap.get(VO).getCountingAreaList()).hasSize(3);
		assertThat(settlementStatusMap.get(VB).getCountingAreaList()).hasSize(1);
		assertThat(settlementStatusMap.get(VS).getCountingAreaList()).hasSize(1);
		assertThat(settlementStatusMap.get(BF).getCountingAreaList()).hasSize(1);
		assertThat(settlementStatusMap.get(FO).getCountingAreaList()).hasSize(1);
		assertThat(settlementStatusMap.get(FS).getCountingAreaList()).hasSize(1);
	}

	@Test
	public void settlementStatusMap_whenMunicipalityHasPollingDistricts_allCategoriesHaveCorrectCountingMode() {
		UserData userData = mockMunicipalityUser(MV_AREA_PATH_OSLO_MUNICIPALITY);
		ElectionPath boroughPath = new ElectionPath(MV_ELECTION_PATH_GAMLE_OSLO);
		initServices();
		
		Map<CountCategory, SettlementStatus> settlementStatusMap = settlementApplicationService.settlementStatusMap(userData, boroughPath);
		
		assertThat(settlementStatusMap.get(VO).getCountingMode()).isEqualTo(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		assertThat(settlementStatusMap.get(VB).getCountingMode()).isEqualTo(CountingMode.CENTRAL);
		assertThat(settlementStatusMap.get(VS).getCountingMode()).isEqualTo(CountingMode.CENTRAL);
		assertThat(settlementStatusMap.get(BF).getCountingMode()).isEqualTo(CountingMode.CENTRAL);
		assertThat(settlementStatusMap.get(FO).getCountingMode()).isEqualTo(CountingMode.CENTRAL);
		assertThat(settlementStatusMap.get(FS).getCountingMode()).isEqualTo(CountingMode.CENTRAL);
	}

	private void initServices() {
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(mockMvElection());
		List finalCounts = mockFinalBoroughCounts();
		when(getInjectMock(FindCountService.class)
				.findMunicipalityFinalCounts(any(AreaPath.class), any(MvElection.class), any(MvArea.class), any(CountCategory.class)))
				.thenReturn(finalCounts);
		when(getInjectMock(CountCategoryDomainService.class).countCategories(any(Contest.class))).thenReturn(asList(VO, VB, VS, BF, FO, FS));
		when(getInjectMock(VoteCountService.class).countingMode(eq(VO), any(Municipality.class), any(MvElection.class)))
				.thenReturn(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		when(getInjectMock(VoteCountService.class).countingMode(not(eq(VO)), any(Municipality.class), any(MvElection.class)))
				.thenReturn(CountingMode.CENTRAL);
		List<MvArea> mvAreaList = mockThreePollingDistrictsInOsloMvAreas();
		when(getInjectMock(CountingAreaDomainService.class).countingMvAreas(any(MvElection.class), eq(VO))).thenReturn(mvAreaList);
		when(getInjectMock(CountingAreaDomainService.class).countingMvAreas(any(MvElection.class), not(eq(VO)))).thenReturn(singletonList(mock(MvArea.class)));
	}

	private MvElection mockMvElection() {
		MvElection mvElection = contestMvElection();
		Election election = defaultElection(electionEvent());
		Contest boroughContest = contest(election);
		Set<ContestArea> contestAreaSet = new HashSet<>();
		ContestArea boroughOsloContestArea = new ContestArea();
		Borough boroughInOslo = boroughGamleOslo();
		boroughOsloContestArea.setMvArea(boroughMvArea(boroughInOslo));
		contestAreaSet.add(boroughOsloContestArea);
		boroughContest.setContestAreaSet(contestAreaSet);
		mvElection.setContest(boroughContest);
		mvElection.setElection(election);
		return mvElection;
	}

	private List<FinalCount> mockFinalBoroughCounts() {
		List<FinalCount> finalCounts = new ArrayList<>();
		finalCounts.add(FinalCountMockups.finalCount(APPROVED, areaPath(AREA_PATH_BOROUGH_GAMLE_OSLO)));
		return finalCounts;
	}

	private List<MvArea> mockThreePollingDistrictsInOsloMvAreas() {
		List<MvArea> mockAreas = new ArrayList<>();
		MvArea mockMvAreaPollingDistrict1GamleOslo = pollingDistrictMvArea();
		MvArea mockMvAreaPollingDistrict2GamleOslo = pollingDistrictMvArea();
		MvArea mockMvAreaPollingDistrict3GamleOslo = pollingDistrictMvArea();
		mockAreas.add(mockMvAreaPollingDistrict1GamleOslo);
		mockAreas.add(mockMvAreaPollingDistrict2GamleOslo);
		mockAreas.add(mockMvAreaPollingDistrict3GamleOslo);
		return mockAreas;
	}

	private UserData mockMunicipalityUser(String areaPath) {
		MvArea municipality = municipalityMvArea();
		municipality.setMunicipality(new Municipality());
		municipality.setAreaPath(areaPath);
		municipality.setAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());
		Operator op = defaultOperator();
		OperatorRole opr = operatorRole(defaultRole(), op, contestMvElection(), municipality);
		return UserDataMockups.userData(opr);
	}

	@Test
	public void settlementSummary_whenNoFinalCountsToSettlement_returnsEmptySettlementSummary() {
		UserData userData = mockMunicipalityUser(MV_AREA_PATH_OSLO_MUNICIPALITY);
		ElectionPath boroughContestPath = ElectionPath.from("111111.11.11.111111");

		when(getInjectMock(CountCategoryDomainService.class).countCategories(any(Contest.class))).thenReturn(asList(VO, VS, VB, FO, FS, BF));
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(mockMvElection());

		SettlementSummary actual = settlementApplicationService.settlementSummary(userData, boroughContestPath);

		assertThat(actual).isEqualTo(blankSettlementSummary());
	}

	@Test
	public void settlementSummary_whenFinalCountsToSettlement_returnsSettlementSummary() {
		UserData userData = mockMunicipalityUser(MV_AREA_PATH_OSLO_MUNICIPALITY);
		ElectionPath boroughContestPath = ElectionPath.from("111111.11.11.111111");

		when(getInjectMock(CountCategoryDomainService.class).countCategories(any(Contest.class))).thenReturn(asList(VO, VS, VB, FO, FS, BF));
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class))).thenReturn(mockMvElection());
		List list = asList(finalCount(VO), finalCount(VO), finalCount(VO), finalCount(VS), finalCount(VB), finalCount(FO), finalCount(FS), finalCount(BF));
		when(
				getInjectMock(FindCountService.class).findMunicipalityCountsByStatus(any(AreaPath.class), any(ElectionPath.class),
						any(CountStatus.class)))
				.thenReturn(
						list);

		SettlementSummary actual = settlementApplicationService.settlementSummary(userData, boroughContestPath);

		SettlementSummary expected = expectedSettlementSummary();
		assertThat(actual).isEqualTo(expected);
	}

	private SettlementSummary expectedSettlementSummary() {
		return new SettlementSummary(countCategories(), ordinaryBallotCountSummaries(), blankBallotCountSummary(), rejectedBallotCountSummaries());
	}

	private List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries() {
		List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries = new ArrayList<>();
		rejectedBallotCountSummaries.add(rejectedBallotCountSummary1());
		rejectedBallotCountSummaries.add(rejectedBallotCountSummary2());
		rejectedBallotCountSummaries.add(rejectedBallotCountSummary3());
		return rejectedBallotCountSummaries;
	}

	private BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary1() {
		List<SimpleBallotCount> rejectedBallotCounts = new ArrayList<>();
		addAll(
				rejectedBallotCounts,
				simpleBallotCount(VO, 15),
				simpleBallotCount(VS, 5),
				simpleBallotCount(VB, 5),
				simpleBallotCount(FO, 5),
				simpleBallotCount(FS, 5),
				simpleBallotCount(BF, 5));
		return ballotCountSummary(rejectedBallotInfo1(), rejectedBallotCounts);
	}

	private BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary2() {
		List<SimpleBallotCount> rejectedBallotCounts = new ArrayList<>();
		addAll(
				rejectedBallotCounts,
				simpleBallotCount(VO, 15),
				simpleBallotCount(VS, 5),
				simpleBallotCount(VB, 5),
				simpleBallotCount(FO, 5),
				simpleBallotCount(FS, 5),
				simpleBallotCount(BF, 5));
		return ballotCountSummary(rejectedBallotInfo2(), rejectedBallotCounts);
	}

	private BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary3() {
		List<SimpleBallotCount> rejectedBallotCounts = new ArrayList<>();
		addAll(
				rejectedBallotCounts,
				simpleBallotCount(VO, 15),
				simpleBallotCount(VS, 5),
				simpleBallotCount(VB, 5),
				simpleBallotCount(FO, 5),
				simpleBallotCount(FS, 5),
				simpleBallotCount(BF, 5));
		return ballotCountSummary(rejectedBallotInfo3(), rejectedBallotCounts);
	}

	private List<CountCategory> countCategories() {
		return asList(VO, VS, VB, FO, FS, BF);
	}

	private List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries() {
		List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries = new ArrayList<>();
		ordinaryBallotCountSummaries.add(ballotCountSummary1());
		ordinaryBallotCountSummaries.add(ballotCountSummary2());
		ordinaryBallotCountSummaries.add(ballotCountSummary3());
		return ordinaryBallotCountSummaries;
	}

	private SplitBallotCount splitBallotCount(CountCategory countCategory) {
		return splitBallotCount(countCategory, 5, 5);
	}

	private SplitBallotCount splitBallotCount(CountCategory countCategory, int modifiedBallotCount, int unmodifiedBallotCount) {
		return new SplitBallotCount(countCategory, modifiedBallotCount, unmodifiedBallotCount);
	}

	private SimpleBallotCount simpleBallotCount(CountCategory countCategory) {
		return simpleBallotCount(countCategory, 10);
	}

	private SimpleBallotCount simpleBallotCount(CountCategory countCategory, int ballotCount) {
		return new SimpleBallotCount(countCategory, ballotCount);
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary1() {
		List<SplitBallotCount> splitBallotCounts = new ArrayList<>();
		addAll(splitBallotCounts, splitBallotCount(VO, 15, 15), splitBallotCount(VS), splitBallotCount(VB), splitBallotCount(FO), splitBallotCount(FS),
				splitBallotCount(BF));
		return ballotCountSummary(ballotInfo1(), splitBallotCounts);
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary2() {
		List<SplitBallotCount> splitBallotCounts = new ArrayList<>();
		addAll(splitBallotCounts, splitBallotCount(VO, 15, 15), splitBallotCount(VS), splitBallotCount(VB), splitBallotCount(FO), splitBallotCount(FS),
				splitBallotCount(BF));
		return ballotCountSummary(ballotInfo2(), splitBallotCounts);
	}

	private BallotCountSummary<SplitBallotCount> ballotCountSummary3() {
		List<SplitBallotCount> splitBallotCounts = new ArrayList<>();
		addAll(splitBallotCounts, splitBallotCount(VO, 15, 15), splitBallotCount(VS), splitBallotCount(VB), splitBallotCount(FO), splitBallotCount(FS),
				splitBallotCount(BF));
		return ballotCountSummary(ballotInfo3(), splitBallotCounts);
	}

	private BallotCountSummary<SimpleBallotCount> blankBallotCountSummary() {
		List<SimpleBallotCount> blankBallotCounts = new ArrayList<>();
		addAll(blankBallotCounts, simpleBallotCount(VO, 30), simpleBallotCount(VS), simpleBallotCount(VB), simpleBallotCount(FO), simpleBallotCount(FS),
				simpleBallotCount(BF));
		return ballotCountSummary(blankBallotInfo(), blankBallotCounts);
	}

	private <T extends no.valg.eva.admin.common.settlement.model.BallotCount> BallotCountSummary<T> ballotCountSummary(BallotInfo ballotInfo,
			List<T> ballotCounts) {
		return new BallotCountSummary<>(ballotInfo, ballotCounts);
	}

	private FinalCount finalCount(CountCategory category) {
		FinalCount finalCount = new FinalCount("ID", AreaPath.from("111111.11.11.1111"), category, "areaName", "reportingUnitAreaName", false, 10);
		finalCount.setStatus(TO_SETTLEMENT);
		finalCount.setBallotCounts(asList(ballotCount1(), ballotCount2(), ballotCount3()));
		finalCount.setRejectedBallotCounts(asList(rejectedBallotCount1(), rejectedBallotCount2(), rejectedBallotCount3()));
		return finalCount;
	}

	private RejectedBallotCount rejectedBallotCount1() {
		return rejectedBallotCount("rejectedBallotId1", "rejectedBallotName1", 5);
	}

	private RejectedBallotCount rejectedBallotCount2() {
		return rejectedBallotCount("rejectedBallotId2", "rejectedBallotName2", 5);
	}

	private RejectedBallotCount rejectedBallotCount3() {
		return rejectedBallotCount("rejectedBallotId3", "rejectedBallotName3", 5);
	}

	private RejectedBallotCount rejectedBallotCount(String id, String name, int count) {
		return new RejectedBallotCount(id, name, count);
	}

	private BallotInfo ballotInfo(String id, String name) {
		return new BallotInfo(id, name);
	}

	private BallotInfo ballotInfo1() {
		return ballotInfo("ballotId1", "ballotName1");
	}

	private BallotInfo ballotInfo2() {
		return ballotInfo("ballotId2", "ballotName2");
	}

	private BallotInfo ballotInfo3() {
		return ballotInfo("ballotId3", "ballotName3");
	}

	private BallotInfo blankBallotInfo() {
		return ballotInfo(EvoteConstants.BALLOT_BLANK, "@party[BLANK].name");
	}

	private BallotInfo rejectedBallotInfo1() {
		return ballotInfo("rejectedBallotId1", "rejectedBallotName1");
	}

	private BallotInfo rejectedBallotInfo2() {
		return ballotInfo("rejectedBallotId2", "rejectedBallotName2");
	}

	private BallotInfo rejectedBallotInfo3() {
		return ballotInfo("rejectedBallotId3", "rejectedBallotName3");
	}

	private BallotCount ballotCount(String id, String name, int unmodifiedCount, int modifiedCount) {
		return new BallotCount(id, name, unmodifiedCount, modifiedCount);
	}

	private BallotCount ballotCount1() {
		return ballotCount("ballotId1", "ballotName1", 5, 5);
	}

	private BallotCount ballotCount2() {
		return ballotCount("ballotId2", "ballotName2", 5, 5);
	}

	private BallotCount ballotCount3() {
		return ballotCount("ballotId3", "ballotName3", 5, 5);
	}

	private SettlementSummary blankSettlementSummary() {
		List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries = new ArrayList<>();
		BallotCountSummary<SimpleBallotCount> blankBallotCountSummary = new BallotCountSummary<>(new BallotInfo(EvoteConstants.BALLOT_BLANK, "@party[BLANK].name"));
		List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries = new ArrayList<>();
		return new SettlementSummary(countCategories(), ordinaryBallotCountSummaries, blankBallotCountSummary, rejectedBallotCountSummaries);
	}

	@Test
	public void findAffiliationVoteCountsBySettlement_givenUserDataAndContestPath_returnAffiliationVoteCounts() throws Exception {
		SettlementApplicationService settlementApplicationService = initializeMocks(SettlementApplicationService.class);
		SettlementRepository settlementRepository = getInjectMock(SettlementRepository.class);
		Contest contest = createMock(Contest.class);
		Settlement settlement = createMock(Settlement.class);
		List<no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount> affiliationVoteCountEntities = createListMock();
		List<AffiliationVoteCount> affiliationVoteCountDtos = createListMock();

		when(getInjectMock(ContestRepository.class).findSingleByPath(CONTEST_PATH)).thenReturn(contest);
		when(settlementRepository.findSettlementByContest(contest)).thenReturn(settlement);
		when(settlementRepository.findAffiliationVoteCountsBySettlement(settlement)).thenReturn(affiliationVoteCountEntities);
		when(getInjectMock(AffiliationVoteCountMapper.class).affiliationVoteCounts(affiliationVoteCountEntities)).thenReturn(affiliationVoteCountDtos);

		List<AffiliationVoteCount> affiliationVoteCounts = settlementApplicationService.findAffiliationVoteCountsBySettlement(userData(), CONTEST_PATH);

		assertThat(affiliationVoteCounts).isEqualTo(affiliationVoteCountDtos);
	}

	@Test
	public void findMandatesBySettlement_givenUserDataAndContestPath_returnsMandates() throws Exception {
		SettlementApplicationService settlementApplicationService = initializeMocks(SettlementApplicationService.class);
		SettlementRepository settlementRepository = getInjectMock(SettlementRepository.class);
		Contest contest = createMock(Contest.class);
		Settlement settlement = createMock(Settlement.class);
		List<Integer> mandatesFromSettlementRepository = createListMock();

		when(getInjectMock(ContestRepository.class).findSingleByPath(CONTEST_PATH)).thenReturn(contest);
		when(settlementRepository.findSettlementByContest(contest)).thenReturn(settlement);
		when(settlementRepository.findMandatesBySettlement(settlement)).thenReturn(mandatesFromSettlementRepository);

		List<Integer> mandates = settlementApplicationService.findMandatesBySettlement(userData(), CONTEST_PATH);

		assertThat(mandates).isEqualTo(mandatesFromSettlementRepository);
	}

	@Test
	public void findCandidateVoteCountsBySettlement_givenUserDataAndContestPath_returnsCandidateVoteCounts() throws Exception {
		SettlementApplicationService settlementApplicationService = initializeMocks(SettlementApplicationService.class);
		SettlementRepository settlementRepository = getInjectMock(SettlementRepository.class);
		Contest contest = createMock(Contest.class);
		Settlement settlement = createMock(Settlement.class);
		Map<Long, List<CandidateVoteCountDto>> expectedCandidateVoteCounts = createMapMock();

		when(getInjectMock(ContestRepository.class).findSingleByPath(CONTEST_PATH)).thenReturn(contest);
		when(settlementRepository.findSettlementByContest(contest)).thenReturn(settlement);
		when(settlementRepository.findCandidateVoteCountsBySettlement(settlement)).thenReturn(expectedCandidateVoteCounts);

		Map<Long, List<CandidateVoteCountDto>> candidateVoteCounts = settlementApplicationService.findCandidateVoteCountsBySettlement(userData(), CONTEST_PATH);

		assertThat(candidateVoteCounts).isEqualTo(expectedCandidateVoteCounts);
	}

	@Test
	public void findAffiliationCandidateSeatsBySettlement_givenUserDataAndContestPath_returnsCandidateSeats() throws Exception {
		SettlementApplicationService settlementApplicationService = initializeMocks(SettlementApplicationService.class);
		SettlementRepository settlementRepository = getInjectMock(SettlementRepository.class);
		Contest contest = createMock(Contest.class);
		Settlement settlement = createMock(Settlement.class);
		List<no.valg.eva.admin.settlement.domain.model.CandidateSeat> candidateSeatEntities = createListMock();
		List<CandidateSeat> candidateSeatDtos = createListMock();

		when(getInjectMock(ContestRepository.class).findSingleByPath(CONTEST_PATH)).thenReturn(contest);
		when(settlementRepository.findSettlementByContest(contest)).thenReturn(settlement);
		when(settlementRepository.findAffiliationCandidateSeatsBySettlement(settlement)).thenReturn(candidateSeatEntities);
		when(getInjectMock(CandidateSeatMapper.class).candidateSeats(candidateSeatEntities)).thenReturn(candidateSeatDtos);

		List<CandidateSeat> candidateSeats = settlementApplicationService.findAffiliationCandidateSeatsBySettlement(userData(), CONTEST_PATH);

		assertThat(candidateSeats).isEqualTo(candidateSeatDtos);
	}

	private UserData userData() {
		return createMock(UserData.class);
	}
}

