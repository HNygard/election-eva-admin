package no.valg.eva.admin.counting.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.AbstractCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.mockups.BoroughMockups;
import no.valg.eva.admin.common.mockups.ElectionEventMockups;
import no.valg.eva.admin.common.mockups.ElectionMockups;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.common.mockups.PollingDistrictMockups;
import no.valg.eva.admin.common.mockups.ReportingUnitMockups;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.FinalCountMockups;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.domain.updater.BallotUpdater;
import no.valg.eva.admin.counting.domain.updater.CountUpdater;
import no.valg.eva.admin.counting.domain.updater.FinalBallotUpdater;
import no.valg.eva.admin.counting.domain.updater.FinalCountUpdater;
import no.valg.eva.admin.counting.domain.updater.PreliminaryBallotUpdater;
import no.valg.eva.admin.counting.domain.updater.PreliminaryCountUpdater;
import no.valg.eva.admin.counting.domain.updater.ProtocolBallotUpdater;
import no.valg.eva.admin.counting.domain.updater.ProtocolCountUpdater;
import no.valg.eva.admin.counting.domain.validation.CountValidator;
import no.valg.eva.admin.counting.domain.validation.FinalCountValidator;
import no.valg.eva.admin.counting.domain.validation.PreliminaryCountValidator;
import no.valg.eva.admin.counting.domain.validation.ProtocolCountValidator;
import no.valg.eva.admin.counting.mockup.ReportCountCategoryMockups;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.LocalDate;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.EvoteConstants.BALLOT_BLANK;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class VoteCountServiceTest extends MockUtilsTestCase {

	private static final String PROTOCOL_COUNT_ID = "PVO1";
	private static final String PRELIMINARY_COUNT_ID = "FVO1";
	private static final String FINAL_COUNT_ID = "EVO1";

	private static final int SPECIAL_COVERS = 100;
	private static final int FOREIGN_SPECIAL_COVERS = 101;
	private static final int EMERGENCY_SPECIAL_COVERS = 102;
	private static final int ORDINARY_BALLOT_COUNT = 103;
	private static final int QUESTIONABLE_BALLOT_COUNT = 104;
	private static final int BLANK_BALLOT_COUNT = 105;
	private static final int PARTI_BALLOT_COUNT = 106;
	private static final int REJECTED_COUNT = 107;
	private static final int BALLOTS_FOR_OTHER_CONTESTS_COUNT = 108;

	private static final String PARTI_ID = "Kystpartiet";
	private static final String REJECTED_ID = "rejected";
	private static final Long DIFFERENT_PK = 2001L;
	private static final String FIRST_TECHNICAL_POLLING_DISTRICT_ID = "0001";
	private static final AreaPath AN_AREA_PATH = AreaPath.from("150001.47.01");

	private VoteCountService voteCountService;

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
	private MvElectionRepository mvElectionRepository;
	@Mock
	private MvAreaRepository mvAreaRepository;
	@Mock
	private BallotRepository ballotRepository;
	@Mock
	private BallotRejectionRepository ballotRejectionRepository;
	@Mock
	private ContestReportRepository contestReportRepository;
	@Mock
	private VoteCountRepository voteCountRepository;
	@Mock
	private ReportingUnitDomainService reportingUnitDomainService;
	@Mock
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	@Mock
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;

	private AreaPath areaPath;
	private ElectionPath contestPath;
	private CountCategory category;
	private UserData userData;
	private CountContext context;
	private Municipality municipality;
	private MvArea pollingDistrict;
	private MvElection contest;
	private ReportingUnit reportingUnit;
	private VoteCount voteCount;
	private ContestReport contestReport;
	private AreaPath operatorAreaPath;
	private AreaPath operatorAreaPathInOtherPollingDistrict;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void setUp() {
		voteCountService = new VoteCountService(
				reportingUnitRepository,
				reportCountCategoryRepository,
				votingRepository,
				manualContestVotingRepository,
				countingCodeValueRepository,
				mvElectionRepository,
				mvAreaRepository,
				ballotRepository,
				ballotRejectionRepository,
				contestReportRepository,
				voteCountRepository,
				reportingUnitDomainService,
				antallStemmesedlerLagtTilSideDomainService,
				voteCountStatusendringTrigger);

		areaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT);
		operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		operatorAreaPathInOtherPollingDistrict = new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT_OTHER);
		contestPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		category = VO;
		userData = new UserData();
		context = new CountContext(contestPath, category);
		municipality = MunicipalityMockups.municipality(true);
		pollingDistrict = MvAreaMockups.pollingDistrictMvArea(municipality);
		Borough borough = new Borough();
		borough.getPollingDistricts().add(pollingDistrict.getPollingDistrict());
		municipality.getBoroughs().add(borough);
		contest = createMock(MvElection.class);
		reportingUnit = ReportingUnitMockups.reportingUnit(pollingDistrict);

		contestReport = new ContestReport();
		contestReport.setReportingUnit(reportingUnit);
		voteCount = voteCount();
	}

	@Test
	public void findVoteCountFindsCount() {
		no.valg.eva.admin.common.counting.model.CountQualifier qualifier = no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
		voteCount.setCountQualifier(countQualifier(qualifier));
		contestReport.add(voteCount);
		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		assertThat(voteCountService.findVoteCount(reportingUnit, context, pollingDistrict, contest, qualifier)).isEqualTo(voteCount);
	}

	@Test
	public void findVoteCountDoesNotFindCount() {
		no.valg.eva.admin.common.counting.model.CountQualifier qualifier = no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
		voteCount.setCountQualifier(countQualifier(qualifier));
		assertThat(voteCountService.findVoteCount(reportingUnit, context, pollingDistrict, contest, qualifier)).isNull();
	}

	@Test
	public void findVoteCountDoesNotFindContestReport() {
		ReportingUnit otherReportingUnit = ReportingUnitMockups.reportingUnit();
		otherReportingUnit.setPk(DIFFERENT_PK);
		assertThat(voteCountService.findVoteCount(otherReportingUnit, context, pollingDistrict, contest, null)).isNull();
	}

	@Test
	public void findFinalCountsFindsNothing() {
		assertThat(voteCountService.findFinalVoteCounts(reportingUnit, contest, pollingDistrict, category)).isEmpty();
	}

	@Test
	public void findFinalCountsDoesNotFindContestReport() {
		ReportingUnit otherReportingUnit = ReportingUnitMockups.reportingUnit();
		otherReportingUnit.setPk(DIFFERENT_PK);
		assertThat(voteCountService.findFinalVoteCounts(otherReportingUnit, contest, pollingDistrict, category)).isEmpty();
	}

	@Test
	public void pollingDistrictsForProtocolCount_whenCentralCount_areFoundFromMunicipality() {
		Municipality municipality = mock(Municipality.class);
		when(municipality.isRequiredProtocolCount()).thenReturn(true);
		MvArea pollingDistrictMvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		pollingDistrictMvArea.getPollingDistrict().setMunicipality(true);
		MvArea municipalityArea = makeMunicipalityMvAreaWhichIsA0000PollingDistrict(municipality, pollingDistrictMvArea.getPollingDistrict());
		Collection<PollingDistrict> pollingDistricts = makePollingDistricts(pollingDistrictMvArea);
		when(municipality.regularPollingDistricts(true, true)).thenReturn(pollingDistricts);
		CountCategory category = CountCategory.VO;
		CountContext context = new CountContext(contestPath, category);
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), eq(category)))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());
		AreaPath operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);

		assertThat(voteCountService.pollingDistrictsForProtocolCount(context, municipalityArea, fakeMvElectionContest, operatorAreaPath))
				.containsAll(pollingDistricts);
	}

	private MvArea makeMunicipalityMvAreaWhichIsA0000PollingDistrict(Municipality municipality, PollingDistrict pollingDistrict) {
		MvArea mvArea = new MvArea();
		mvArea.setMunicipality(municipality);
		mvArea.setPollingDistrict(pollingDistrict);
		return mvArea;
	}

	private Collection<PollingDistrict> makePollingDistricts(MvArea pollingDistrictMvArea) {
		Collection<PollingDistrict> pollingDistricts = new ArrayList<>();
		pollingDistricts.add(pollingDistrictMvArea.getPollingDistrict());
		PollingDistrict anotherPollingDistrict = PollingDistrictMockups.pollingDistrict();
		pollingDistricts.add(anotherPollingDistrict);
		return pollingDistricts;
	}

	@Test
	public void pollingDistrictsForProtocolCountsAreFoundFromChildrenWhenNotCentralCountButParentPollingDistrict() {
		Set<PollingDistrict> pollingDistricts = new HashSet<>();
		pollingDistricts.add(PollingDistrictMockups.child1PollingDistrict());
		pollingDistricts.add(PollingDistrictMockups.child2PollingDistrict());

		pollingDistrict.setParentPollingDistrict(true);
		pollingDistrict.getPollingDistrict().setChildPollingDistricts(pollingDistricts);
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);

		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), eq(category)))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVo());

		assertThat(voteCountService.pollingDistrictsForProtocolCount(context, pollingDistrict, fakeMvElectionContest, operatorAreaPath))
				.containsAll(pollingDistricts);
	}

	@Test
	public void pollingDistrictsForProtocolCountsContainsOnlySameWhenNotCentralCountAndNotParentPollingDistrict() {
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), eq(category)))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVo());
		assertThat(voteCountService.pollingDistrictsForProtocolCount(context, pollingDistrict, fakeMvElectionContest, operatorAreaPath)).containsOnly(
				pollingDistrict.getPollingDistrict());
	}

	@Test
	public void noPollingDistrictsForProtocolCountsAreReturnedIfCountCategoryDoesNotHaveProtocolCounts() {
		context = new CountContext(contestPath, CountCategory.FO);
		assertThat(voteCountService.pollingDistrictsForProtocolCount(context, pollingDistrict, contest, operatorAreaPath)).isEmpty();
	}

	@Test
	public void onlyPollingDistrictsWithinOperatorsAreaPathAreReturned() {
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		municipality = mock(Municipality.class);
		pollingDistrict.setMunicipality(municipality);
		Collection<PollingDistrict> pollingDistricts = makePollingDistricts(pollingDistrict);
		when(municipality.regularPollingDistricts(true, true)).thenReturn(pollingDistricts);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), eq(category)))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());

		assertThat(voteCountService.pollingDistrictsForProtocolCount(context, pollingDistrict, fakeMvElectionContest, operatorAreaPathInOtherPollingDistrict))
				.isEmpty();
	}

	@Test
	public void useSpecialCoversWhenReportCountCategoryIndicatesSpecialCovers() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), CountCategory.VF))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVf(true));

		assertThat(voteCountService.useForeignSpecialCovers(contest, municipality)).isTrue();
	}

	@Test
	public void doNotUseSpecialCoversWhenReportCountCategoryIndicatesOtherwise() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), CountCategory.VF))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVf(false));

		assertThat(voteCountService.useForeignSpecialCovers(contest, municipality)).isFalse();
	}

	@Test
	public void doNotUseSpecialCoversWhenReportCountCategoryIsNull() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), CountCategory.VF))
				.thenReturn(null);

		assertThat(voteCountService.useForeignSpecialCovers(contest, municipality)).isFalse();
	}

	@Test
	public void updateVoteCountWithProtocolValues() {
		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL));
		contestReport.add(voteCount);

		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(reportCountCategory);

		ProtocolCount count = protocolCount();
		ProtocolCountUpdater countUpdater = new ProtocolCountUpdater();
		ProtocolBallotUpdater ballotUpdater = new ProtocolBallotUpdater();
		ProtocolCountValidator protocolCountValidator = new ProtocolCountValidator();
		VoteCount updatedCount = voteCountService.updateVoteCount(
				userData,
				context,
				reportingUnit,
				count,
				pollingDistrict,
				contest,
				countUpdater,
				ballotUpdater,
				protocolCountValidator);

		assertThat(updatedCount.getId()).isEqualTo(count.getId());
		assertThat(updatedCount.getSpecialCovers()).isEqualTo(SPECIAL_COVERS);
		assertThat(updatedCount.getForeignSpecialCovers()).isEqualTo(FOREIGN_SPECIAL_COVERS);
		assertThat(updatedCount.getEmergencySpecialCovers()).isNull();
		assertThat(updatedCount.getApprovedBallots()).isEqualTo(ORDINARY_BALLOT_COUNT + BLANK_BALLOT_COUNT);
		assertThat(updatedCount.getRejectedBallots()).isEqualTo(QUESTIONABLE_BALLOT_COUNT);
		BallotCount ballotCount = updatedCount.getBallotCountSet().iterator().next();
		assertThat(ballotCount.getBallotId()).isEqualTo(BALLOT_BLANK);
		assertThat(ballotCount.getUnmodifiedBallots()).isEqualTo(BLANK_BALLOT_COUNT);
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@database.error.stale_object")
	public void updateVoteCount_givenStaleCount_ThrowException() {
		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL));
		voteCount.setAuditOplock(2);
		contestReport.add(voteCount);

		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(reportCountCategory);

		ProtocolCount count = protocolCount();
		count.setVersion(1);
		ProtocolCountUpdater countUpdater = new ProtocolCountUpdater();
		ProtocolBallotUpdater ballotUpdater = new ProtocolBallotUpdater();
		ProtocolCountValidator protocolCountValidator = new ProtocolCountValidator();
		voteCountService.updateVoteCount(
				userData,
				context,
				reportingUnit,
				count,
				pollingDistrict,
				contest,
				countUpdater,
				ballotUpdater,
				protocolCountValidator);
	}

	@Test
	public void updateVoteCount_whenBoroughContest_updatesBallotsForOtherContests() {
		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL));
		contestReport.add(voteCount);

		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(reportCountCategory);

		ProtocolCount protocolCount = protocolCount();
		DailyMarkOffCount markOffCount = new DailyMarkOffCount(new LocalDate(2014, 1, 1));
		markOffCount.setMarkOffCount(BALLOTS_FOR_OTHER_CONTESTS_COUNT);
		protocolCount.setDailyMarkOffCountsForOtherContests(new DailyMarkOffCounts(singletonList(markOffCount)));
		protocolCount.setBallotCountForOtherContests(BALLOTS_FOR_OTHER_CONTESTS_COUNT);

		VoteCount updatedCount = voteCountService.updateVoteCount(
				userData,
				context,
				reportingUnit,
				protocolCount,
				pollingDistrict,
				contest,
				new ProtocolCountUpdater(),
				new ProtocolBallotUpdater(),
				new ProtocolCountValidator());

		assertThat(updatedCount.getBallotsForOtherContests()).isEqualTo(BALLOTS_FOR_OTHER_CONTESTS_COUNT);
	}

	@Test
	public void updateVoteCount_approvedCount_updatesWithPreliminaryValues() {
		voteCount.setCountQualifier(countQualifier(PRELIMINARY));
		contestReport.add(voteCount);

		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(reportCountCategory);

		AbstractCount count = preliminaryCount();
		count.setStatus(APPROVED);
		CountUpdater countUpdater = new PreliminaryCountUpdater();
		CountValidator countValidator = new PreliminaryCountValidator();
		BallotUpdater ballotUpdater = new PreliminaryBallotUpdater();

		VoteCount updatedCount = voteCountService.updateVoteCount(
				userData, context, reportingUnit, count, pollingDistrict, contest, countUpdater, ballotUpdater, countValidator);

		assertThat(updatedCount.getId()).isEqualTo(count.getId());
		assertThat(updatedCount.getApprovedBallots()).isEqualTo(PARTI_BALLOT_COUNT + BLANK_BALLOT_COUNT);
		assertThat(updatedCount.getRejectedBallots()).isEqualTo(QUESTIONABLE_BALLOT_COUNT);
	}

	@Test
	public void updateVoteCountWithFinalValues() {

		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.FINAL));
		contestReport.add(voteCount);

		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(reportCountCategory);

		FinalCount count = finalCount();
		FinalCountUpdater countUpdater = new FinalCountUpdater();
		FinalCountValidator countValidator = new FinalCountValidator();
		FinalBallotUpdater ballotUpdater = new FinalBallotUpdater();

		VoteCount updatedCount = voteCountService.updateVoteCount(
				userData,
				context,
				reportingUnit,
				count,
				pollingDistrict,
				contest,
				countUpdater,
				ballotUpdater,
				countValidator);

		assertThat(updatedCount.getId()).isEqualTo(count.getId());
		assertThat(updatedCount.getApprovedBallots()).isEqualTo(PARTI_BALLOT_COUNT);
		assertThat(updatedCount.getRejectedBallots()).isEqualTo(REJECTED_COUNT);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void updateVoteCount_withFoAndIngenStemmesedlerLagtTilSide_throwsEvoteException() {
		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY));
		contestReport.add(voteCount);

		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReport);
		ReportCountCategory reportCountCategory = mock(ReportCountCategory.class);
		when(reportCountCategory.getCountingMode()).thenReturn(CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(reportCountCategory);

		PreliminaryCount count = (PreliminaryCount) preliminaryCount();
		PreliminaryCountUpdater countUpdater = new PreliminaryCountUpdater();
		PreliminaryBallotUpdater ballotUpdater = new PreliminaryBallotUpdater();
		PreliminaryCountValidator protocolCountValidator = new PreliminaryCountValidator();

		context = new CountContext(new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST), CountCategory.FO);

		voteCountService.updateVoteCount(userData, context, reportingUnit, count, pollingDistrict, contest, countUpdater, ballotUpdater,
				protocolCountValidator);
	}

	@Test
	public void reportingUnitTypeForPreliminaryCountIsValgstyretWhenCountModeIsNotByPollingDistrict() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), category))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());
		assertThat(voteCountService.reportingUnitTypeForPreliminaryCount(context, municipality, contest)).isEqualTo(ReportingUnitTypeId.VALGSTYRET);
	}

	@Test
	public void reportingUnitTypeForPreliminaryCountIsStemmestyretWhenCountModeIsByPollingDistrict() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), category))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoByPollingDistrict());

		assertThat(voteCountService.reportingUnitTypeForPreliminaryCount(context, municipality, contest)).isEqualTo(ReportingUnitTypeId.STEMMESTYRET);
	}

	@Test
	public void doNotUseCombinedProtocolAndPreliminaryCountWhenCountModeIsNotByPollingDistrict() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), category))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());
		assertThat(voteCountService.useCombinedProtocolAndPreliminaryCount(context, pollingDistrict, contest)).isFalse();
	}

	@Test
	public void useCombinedProtocolAndPreliminaryCountWhenCountModeIsByPollingDistrictAndNoParentPollingDistricts() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), category))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoByPollingDistrict());
		assertThat(voteCountService.useCombinedProtocolAndPreliminaryCount(context, pollingDistrict, contest)).isTrue();
	}

	@Test
	public void doNotUseCombinedProtocolAndPreliminaryCountWhenCountModeIsByPollingDistrictAndPollingDistrictIsParentPollingDistrict() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), category))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoByPollingDistrict());
		pollingDistrict.setParentPollingDistrict(true);
		assertThat(voteCountService.useCombinedProtocolAndPreliminaryCount(context, pollingDistrict, contest)).isFalse();
	}

	@Test
	public void doNotUseCombinedProtocolAndPreliminaryCountWhenCountModeIsByPollingDistrictAndPollingDistrictIsChildPollingDistrict() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(), category))
				.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoByPollingDistrict());
		pollingDistrict.setPollingDistrictByParentPollingDistrictPk(new PollingDistrict());
		assertThat(voteCountService.useCombinedProtocolAndPreliminaryCount(context, pollingDistrict, contest)).isFalse();
	}

	@Test
	public void useCombinedProtocolAndPreliminaryCount_whenBydelsvalg_returnsFalse() {
		MvArea boroughMvArea = MvAreaMockups.boroughMvArea(BoroughMockups.borough(MunicipalityMockups.municipality(true)));
		MvElection boroughElection = MvElectionMockups.contestMvElection();
		ContestArea contestArea = new ContestArea();
		contestArea.setMvArea(boroughMvArea);
		boroughElection.getContest().getContestAreaSet().add(contestArea);
		assertThat(voteCountService.useCombinedProtocolAndPreliminaryCount(context, boroughMvArea, boroughElection)).isFalse();
	}

	@Test
	public void useCombinedProtocolAndPreliminaryCount_withNotIsRequiredProtocolCount_returnsFalse() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		CountContext context = createMock(CountContext.class);
		MvArea countArea = createMock(MvArea.class);
		MvElection contest = createMock(MvElection.class);
		when(context.getCategory()).thenReturn(CountCategory.VO);
		when(countArea.getMunicipality().isRequiredProtocolCount()).thenReturn(false);

		assertThat(service.useCombinedProtocolAndPreliminaryCount(context, countArea, contest)).isFalse();
	}

	@Test
	public void userCanNotAccessReportingUnitForPreliminaryCountWhenCentralCountAndUserAreaIsPollingDistrict() {
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(),
				category)).thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());
		assertThat(
				voteCountService.userCanAccessReportingUnitForPreliminaryCount(context, new AreaPath(pollingDistrict.getAreaPath()), contest, pollingDistrict))
						.isFalse();
	}

	@Test
	public void userCanNotAccessReportingUnitForPreliminaryCountWhenPollingDistrictCountAndUserAreaIsAMunicipalityThatDoesNotContainPollingDistrict() {
		reset(reportingUnitRepository);

		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(),
				category)).thenReturn(ReportCountCategoryMockups.reportCountCategoryVoByPollingDistrict());
		assertThat(voteCountService.userCanAccessReportingUnitForPreliminaryCount(context,
				new AreaPath(MvAreaMockups.MV_AREA_PATH_OSLO_MUNICIPALITY), contest, pollingDistrict)).isFalse();
	}

	@Test
	public void userCanAccessReportingUnitForPreliminaryCount_pollingDistrictCountAndUserAreaIsAMunicipalityThatContainsPollingDistrict() {
		AreaPath operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);

		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(),
				category)).thenReturn(ReportCountCategoryMockups.reportCountCategoryVoByPollingDistrict());

		voteCountService.userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contest, pollingDistrict);
        verify(reportingUnitRepository, times(1)).existsFor(any(), eq(STEMMESTYRET));
	}

	@Test
	public void userCanAccessReportingUnitForPreliminaryCount_centralCountAndUserAreaIsAMunicipality_returnsExistsFor() {
		reset(reportingUnitRepository);
		pollingDistrict.getPollingDistrict().setMunicipality(true);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, contest.getElectionGroup(),
				category)).thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());
		AreaPath operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);

		voteCountService.userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contest, pollingDistrict);
        verify(reportingUnitRepository, times(1)).existsFor(any(), eq(VALGSTYRET));
	}

	@Test
	public void userCanAccessReportingUnitForPreliminaryCount_whenCountingAreaIsChildPollingDistrict_returnsFalse() {
		MvArea countingArea = MvAreaMockups.pollingDistrictMvArea();
		countingArea.setPollingDistrictByParentPollingDistrictPk(PollingDistrictMockups.pollingDistrict());
		assertThat(voteCountService.userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contest, countingArea)).isFalse();
	}

	@Test
	public void countExists() {
		AbstractCount count = protocolCount();
		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL));
		contestReport.add(voteCount);
		when(contestReportRepository
				.findByReportingUnitContest(anyLong(), anyLong()))
						.thenReturn(contestReport);
		assertThat(voteCountService.countExists(count, contest, pollingDistrict, reportingUnit)).isTrue();
	}

	@Test
	public void contestReportExistsButCountDoesNot() {
		AbstractCount count = protocolCount();
		String id = "finnesIkke";
		count.setId(id);
		voteCount.setCountQualifier(countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL));
		contestReport.add(voteCount);
		when(contestReportRepository
				.findByReportingUnitContest(anyLong(), anyLong()))
						.thenReturn(contestReport);
		assertThat(voteCountService.countExists(count, contest, pollingDistrict, reportingUnit)).isFalse();
	}

	@Test
	public void contestReportDoesNotExist() {
		AbstractCount count = protocolCount();
		assertThat(voteCountService.countExists(count, contest, pollingDistrict, new ReportingUnit())).isFalse();
	}

	@Test
	public void markOffCountForPreliminaryCount_whenFs_returnMarkOffCount() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountCategory countCategory = FS;
		CountContext context = new CountContext(contestPath, countCategory);
		MvElection stubMvElection = stub(MvElection.class);
		ElectionGroup stubElectionGroup = stub(ElectionGroup.class);
		Contest stubContest = stub(Contest.class);
		MvArea stubMvArea = stub(MvArea.class);
		Municipality stubMunicipality = stub(Municipality.class);
		Borough stubBorough = stub(Borough.class);
		PollingDistrict stubFirstTechnicalPollingDistrict = stub(PollingDistrict.class);
		VotingCategory[] votingCategories = VotingCategory.from(FS);
		ReportCountCategory stubReportCountCategory = mock(ReportCountCategory.class);

		when(stubMunicipality.isElectronicMarkoffs()).thenReturn(true);
		when(stubMvElection.getElectionGroup()).thenReturn(stubElectionGroup);
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubMvArea.getMunicipality()).thenReturn(stubMunicipality);
		when(stubMvArea.getBorough()).thenReturn(stubBorough);
		when(stubMvArea.getPollingDistrict()).thenReturn(stubFirstTechnicalPollingDistrict);
		when(votingRepository.findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation(stubMunicipality, votingCategories, true)).thenReturn(3L);
		when(stubReportCountCategory.getCountingMode()).thenReturn(CountingMode.CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(stubReportCountCategory);

		Long markOffCount = voteCountService.markOffCountForPreliminaryCount(context, stubMvElection, stubMvArea, countCategory);

		assertThat(markOffCount).isEqualTo(3);
	}

	@Test
	public void markOffCountForPreliminaryCount_whenFoAndFirstTechnicalPollingDistrict_returnMarkOffCount() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountCategory countCategory = FO;
		CountContext context = new CountContext(contestPath, countCategory);
		MvElection stubMvElection = stub(MvElection.class);
		ElectionGroup stubElectionGroup = stub(ElectionGroup.class);
		Contest stubContest = stub(Contest.class);
		MvArea stubMvArea = stub(MvArea.class);
		Municipality stubMunicipality = stub(Municipality.class);
		Borough stubBorough = stub(Borough.class);
		PollingDistrict stubFirstTechnicalPollingDistrict = stub(PollingDistrict.class);
		VotingCategory[] votingCategories = VotingCategory.from(FO);
		ReportCountCategory stubReportCountCategory = mock(ReportCountCategory.class);

		when(stubMvElection.getElectionGroup()).thenReturn(stubElectionGroup);
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubMvArea.getMunicipality()).thenReturn(stubMunicipality);
		when(stubMvArea.getBorough()).thenReturn(stubBorough);
		when(stubMvArea.getPollingDistrict()).thenReturn(stubFirstTechnicalPollingDistrict);
		when(stubBorough.findFirstTechnicalPollingDistrict()).thenReturn(stubFirstTechnicalPollingDistrict);
		when(stubFirstTechnicalPollingDistrict.getId()).thenReturn(FIRST_TECHNICAL_POLLING_DISTRICT_ID);
		when(votingRepository.findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation(stubMunicipality, votingCategories, false)).thenReturn(3L);
		when(stubReportCountCategory.getCountingMode()).thenReturn(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(stubReportCountCategory);

		Long markOffCount = voteCountService.markOffCountForPreliminaryCount(context, stubMvElection, stubMvArea, countCategory);

		assertThat(markOffCount).isEqualTo(3);
	}

	@Test
	public void markOffCountForPreliminaryCount_whenFoAndNotFirstTechnicalPollingDistrict_returnNullMarkOffCount() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountCategory countCategory = FO;
		CountContext context = new CountContext(contestPath, countCategory);
		MvElection stubMvElection = stub(MvElection.class);
		ElectionGroup stubElectionGroup = stub(ElectionGroup.class);
		Contest stubContest = stub(Contest.class);
		MvArea stubMvArea = stub(MvArea.class);
		Municipality stubMunicipality = stub(Municipality.class);
		Borough stubBorough = stub(Borough.class);
		PollingDistrict stubPollingDistrict = stub(PollingDistrict.class);
		PollingDistrict stubFirstTechnicalPollingDistrict = stub(PollingDistrict.class);
		ReportCountCategory stubReportCountCategory = mock(ReportCountCategory.class);

		when(stubMvElection.getElectionGroup()).thenReturn(stubElectionGroup);
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubMvArea.getMunicipality()).thenReturn(stubMunicipality);
		when(stubMvArea.getBorough()).thenReturn(stubBorough);
		when(stubMvArea.getPollingDistrict()).thenReturn(stubPollingDistrict);
		when(stubBorough.findFirstTechnicalPollingDistrict()).thenReturn(stubFirstTechnicalPollingDistrict);
		when(stubFirstTechnicalPollingDistrict.getId()).thenReturn(FIRST_TECHNICAL_POLLING_DISTRICT_ID);
		when(stubReportCountCategory.getCountingMode()).thenReturn(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(stubReportCountCategory);

		Long markOffCount = voteCountService.markOffCountForPreliminaryCount(context, stubMvElection, stubMvArea, countCategory);

		assertThat(markOffCount).isNull();
	}

	@Test
	public void markOffCountForPreliminaryCount_whenCategoryIsFoAndBoroughArea_thenReturnBoroughMarkOffs() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountCategory countCategory = FO;
		CountContext context = new CountContext(contestPath, countCategory);
		MvElection stubMvElection = stub(MvElection.class);
		Contest stubContest = stub(Contest.class);
		MvArea stubMvArea = stub(MvArea.class);
		Municipality stubMunicipality = stub(Municipality.class);
		Borough stubBorough = stub(Borough.class);
		VotingCategory[] votingCategories = VotingCategory.from(FO);

		when(stubMvElection.getAreaLevel()).thenReturn(AreaLevelEnum.BOROUGH.getLevel());
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubContest.isOnBoroughLevel()).thenReturn(true);
		when(stubMvArea.getMunicipality()).thenReturn(stubMunicipality);
		when(stubMvArea.getBorough()).thenReturn(stubBorough);

		voteCountService.markOffCountForPreliminaryCount(context, stubMvElection, stubMvArea, countCategory);

		verify(votingRepository).findApprovedVotingCountByBoroughAndCategoriesAndLateValidation(stubBorough, votingCategories, false);
	}

	@Test
	public void markOffCountForPreliminaryCount_whenCategoryIsFoAndSamlekommune_thenReturnSamiMarkOffs() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountCategory countCategory = FO;
		CountContext context = new CountContext(contestPath, countCategory);
		MvElection stubMvElection = stub(MvElection.class);
		when(stubMvElection.getElectionGroup()).thenReturn(mock(ElectionGroup.class));
		Contest stubContest = stub(Contest.class);
		MvArea stubMvArea = stub(MvArea.class);
		Municipality stubMunicipality = stub(Municipality.class);
		ReportCountCategory stubReportCountCategory = mock(ReportCountCategory.class);
		when(stubReportCountCategory.getCountingMode()).thenReturn(CountingMode.CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(stubReportCountCategory);
		when(stubMvElection.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY.getLevel());
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubContest.isOnBoroughLevel()).thenReturn(false);
		when(stubMvArea.getMunicipality()).thenReturn(stubMunicipality);
		when(stubMunicipality.isSamlekommune()).thenReturn(true);

		voteCountService.markOffCountForPreliminaryCount(context, stubMvElection, stubMvArea, countCategory);

		verify(votingRepository).findMarkOffForSamlekommuneInContest(stubMvElection, false);
	}

	@Test
	public void markOffCountForPreliminaryCount_whenCategoryIsFoAndNotSamlekommune_thenReturnApprovedVotingCount() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountCategory countCategory = FO;
		CountContext context = new CountContext(contestPath, countCategory);
		MvElection stubMvElection = stub(MvElection.class);
		when(stubMvElection.getElectionGroup()).thenReturn(mock(ElectionGroup.class));
		Contest stubContest = stub(Contest.class);
		MvArea stubMvArea = stub(MvArea.class);
		Municipality stubMunicipality = stub(Municipality.class);
		ReportCountCategory stubReportCountCategory = mock(ReportCountCategory.class);
		when(stubReportCountCategory.getCountingMode()).thenReturn(CountingMode.CENTRAL);
		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any())).thenReturn(stubReportCountCategory);
		when(stubMvElection.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY.getLevel());
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubContest.isOnBoroughLevel()).thenReturn(false);
		when(stubMvArea.getMunicipality()).thenReturn(stubMunicipality);
		when(stubMunicipality.isSamlekommune()).thenReturn(false);

		voteCountService.markOffCountForPreliminaryCount(context, stubMvElection, stubMvArea, countCategory);

		verify(votingRepository).findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation(stubMunicipality, VotingCategory.from(FO), false);
	}

	@Test
	public void markOffCountForPreliminaryCount_whenCategoryIsBF_returnsMarkOffCountForBF() {
		MvElection mvElectionContest = MvElectionMockups.contestMvElection();
		mvElectionContest.setElection(ElectionMockups.defaultElection(ElectionEventMockups.electionEvent()));
		Borough borough = BoroughMockups.borough(MunicipalityMockups.municipality(true));
		MvArea countingArea = MvAreaMockups.boroughMvArea(borough);
		countingArea.setMunicipality(countingArea.getBorough().getMunicipality());
		CountCategory countCategory = CountCategory.BF;
		Long expectedCount = 1L;
		when(votingRepository.findMarkOffInOtherBoroughs(borough.getPk())).thenReturn(expectedCount);

		assertThat(voteCountService.markOffCountForPreliminaryCount(context, mvElectionContest, countingArea, countCategory)).isEqualTo(expectedCount);
	}

	@Test
	public void markOffCountForPreliminaryCount_whenCategoryIsVFAndNotXiM_returnsDefaultMarkOffCountForVFWithoutXiM() {
		MvElection fakeMvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		boolean electronicMarkOffs = false;
		Municipality municipality = MunicipalityMockups.municipality(electronicMarkOffs);
		Borough borough = BoroughMockups.borough(municipality);
		MvArea countingArea = MvAreaMockups.boroughMvArea(borough);
		countingArea.setMunicipality(countingArea.getBorough().getMunicipality());
		CountCategory countCategory = CountCategory.VF;

		when(reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, fakeMvElectionContest.getElectionGroup(),
				category))
						.thenReturn(ReportCountCategoryMockups.reportCountCategoryVoCentral());

		Long expectedCount = 1L;
		when(votingRepository.findNotRejectedVotingCountByMunicipalityAndCategoriesAndLateValidation(eq(municipality),
				any(no.valg.eva.admin.common.voting.VotingCategory[].class), anyBoolean())).thenReturn(expectedCount);

		assertThat(voteCountService.markOffCountForPreliminaryCount(context, fakeMvElectionContest, countingArea, countCategory)).isEqualTo(expectedCount);
	}

	private VoteCount voteCount() {
		VoteCount voteCount = new VoteCount();
		voteCount.setMvArea(pollingDistrict);
		voteCount.setVoteCountCategory(voteCountCategory());
		voteCount.setBallotCountSet(ballotCounts());
		voteCount.setVoteCountStatus(voteCountStatus());
		return voteCount;
	}

	private VoteCountStatus voteCountStatus() {
		return new VoteCountStatus();
	}

	private Set<BallotCount> ballotCounts() {
		Set<BallotCount> ballotCounts = new HashSet<>();
		ballotCounts.add(ballotCount());
		return ballotCounts;
	}

	private BallotCount ballotCount() {
		BallotCount ballotCount = new BallotCount();
		Ballot ballot = new Ballot();
		ballot.setId(BALLOT_BLANK);
		ballotCount.setBallot(ballot);
		return ballotCount;
	}

	private VoteCountCategory voteCountCategory() {
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(category.getId());
		return voteCountCategory;
	}

	private CountQualifier countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier qualifierId) {
		CountQualifier qualifier = new CountQualifier();
		qualifier.setId(qualifierId.getId());
		return qualifier;
	}

	private ProtocolCount protocolCount() {
		ProtocolCount count = new ProtocolCount(PROTOCOL_COUNT_ID, areaPath, "", "", true);
		count.setId(PROTOCOL_COUNT_ID);
		count.setSpecialCovers(SPECIAL_COVERS);
		count.setForeignSpecialCovers(FOREIGN_SPECIAL_COVERS);
		count.setEmergencySpecialCovers(EMERGENCY_SPECIAL_COVERS);
		count.setOrdinaryBallotCount(ORDINARY_BALLOT_COUNT);
		count.setQuestionableBallotCount(QUESTIONABLE_BALLOT_COUNT);
		count.setBlankBallotCount(BLANK_BALLOT_COUNT);
		return count;
	}

	public AbstractCount preliminaryCount() {
		PreliminaryCount count = new PreliminaryCount("FVO1", areaPath, VO, "", "", false);
		count.setId(PRELIMINARY_COUNT_ID);
		count.setBlankBallotCount(BLANK_BALLOT_COUNT);
		count.setQuestionableBallotCount(QUESTIONABLE_BALLOT_COUNT);
		count.setBallotCounts(partiBallotCounts());
		return count;
	}

	private FinalCount finalCount() {
		FinalCount count = new FinalCount("FVO1", areaPath, VO, "", ReportingUnitTypeId.FYLKESVALGSTYRET, "", false);
		count.setId(FINAL_COUNT_ID);
		count.setBallotCounts(partiBallotCounts());
		count.setRejectedBallotCounts(rejectedBallotCounts());
		return count;
	}

	private List<RejectedBallotCount> rejectedBallotCounts() {
		List<RejectedBallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(rejectedBallotCount());
		return ballotCounts;
	}

	private RejectedBallotCount rejectedBallotCount() {
		RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();
		rejectedBallotCount.setId(REJECTED_ID);
		rejectedBallotCount.setCount(REJECTED_COUNT);
		return rejectedBallotCount;
	}

	private List<no.valg.eva.admin.common.counting.model.BallotCount> partiBallotCounts() {
		List<no.valg.eva.admin.common.counting.model.BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(partiBallotCount());
		return ballotCounts;
	}

	private no.valg.eva.admin.common.counting.model.BallotCount partiBallotCount() {
		no.valg.eva.admin.common.counting.model.BallotCount ballotCount = new no.valg.eva.admin.common.counting.model.BallotCount();
		ballotCount.setId(PARTI_ID);
		ballotCount.setUnmodifiedCount(PARTI_BALLOT_COUNT);
		return ballotCount;
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected <CONTEST> election path, but got <ELECTION>")
	public void findPreliminaryVoteCountsByReportingUnitContestPathAndCategory_givenNotContestPath_throwsException() {
		ElectionPath electionPath = ElectionPath.from("111111.11.11");
		voteCountService.findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(reportingUnit, electionPath, VO);
	}

	@Test
	public void findPreliminaryVoteCountsByReportingUnitContestPathAndCategory_givenContestWithNoContestReport_returnsEmptyList() {
		MvElection stubContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);

		when(mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(stubContestMvElection);
		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(null);

		List<VoteCount> voteCounts = voteCountService.findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(reportingUnit, contestPath, VO);

		assertThat(voteCounts).isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findPreliminaryVoteCountsByReportingUnitContestPathAndCategory_givenContestWithContestReport_returnsVoteCounts() {
		MvElection stubContestMvElection = stub(MvElection.class, RETURNS_DEEP_STUBS);
		ContestReport stubContestReport = stub(ContestReport.class, RETURNS_DEEP_STUBS);
		List<VoteCount> stubVoteCounts = stub(List.class);

		when(mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(stubContestMvElection);
		when(stubContestReport.findVoteCountsByCategoryAndQualifier(VO, PRELIMINARY)).thenReturn(stubVoteCounts);
		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(stubContestReport);

		List<VoteCount> voteCounts = voteCountService.findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(reportingUnit, contestPath, VO);

		assertThat(voteCounts).isSameAs(stubVoteCounts);
	}

	@Test
	public void findVoteCountsFor_withContestLevel_verifyfindVoteCountsByCountQualifierAndStatusWithFinalAndNew() {
		ReportingUnit reportingUnitStub = createMock(ReportingUnit.class);
		ElectionPath electionPath = ElectionPath.from("111111.11.11.111111");
		MvElection mvElectionStub = createMock(MvElection.class);
		when(mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(mvElectionStub);
		ContestReport contestReportMock = createMock(ContestReport.class);
		when(contestReportRepository.findByReportingUnitContest(anyLong(), anyLong())).thenReturn(contestReportMock);

		voteCountService.findVoteCountsFor(reportingUnitStub, electionPath, CountStatus.NEW, FINAL);

		verify(contestReportMock).findVoteCountsByCountQualifierAndStatus(FINAL, CountStatus.NEW);
	}

	@Test
	public void isLastReportingUnitForContest_givenReportingUnitOnSameLevelAsContest_returnTrue() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountContext countContext = new CountContext(contestPath, VO);
		AreaPath countingAreaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		AreaPath countyPath = countingAreaPath.toCountyPath();

		ReportingUnit fakeReportingUnit = mock(ReportingUnit.class);
		when(fakeReportingUnit.getActualAreaLevel()).thenReturn(COUNTY);
		when(reportingUnitRepository.findByAreaPathAndType(countyPath, FYLKESVALGSTYRET)).thenReturn(fakeReportingUnit);
		MvElection fakeMvElection = mock(MvElection.class);
		MvArea fakeMvArea = mock(MvArea.class);
		when(fakeMvElection.contestMvArea()).thenReturn(fakeMvArea);
		when(fakeMvArea.getActualAreaLevel()).thenReturn(COUNTY);
		when(mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(fakeMvElection);

		assertThat(voteCountService.isLastReportingUnitForContest(FYLKESVALGSTYRET, countContext, countingAreaPath)).isTrue();
	}

	@Test
	public void isLastReportingUnitForContest_givenReportingUnitOnDifferentLevelAsContest_returnTrue() throws Exception {
		VoteCountService voteCountService = initializeMocks(VoteCountService.class);
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountContext countContext = new CountContext(contestPath, VO);
		AreaPath countingAreaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		AreaPath municipalityPath = countingAreaPath.toMunicipalityPath();

		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(municipalityPath, VALGSTYRET).getActualAreaLevel()).thenReturn(MUNICIPALITY);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(contestPath.tilValghierarkiSti()).contestMvArea().getActualAreaLevel()).thenReturn(COUNTY);

		assertThat(voteCountService.isLastReportingUnitForContest(VALGSTYRET, countContext, countingAreaPath)).isFalse();
	}

	@Test
	public void pollingDistrictsForProtocolCount_withVOAndNotRequiredProtocolCount_returnsEmptyList() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		CountContext countContext = createMock(CountContext.class);
		MvArea mvArea = createMock(MvArea.class);
		when(countContext.getCategory()).thenReturn(CountCategory.VO);
		when(mvArea.getMunicipality().isRequiredProtocolCount()).thenReturn(false);

		assertThat(service.pollingDistrictsForProtocolCount(countContext, mvArea,
				createMock(MvElection.class), createMock(AreaPath.class))).isEmpty();
	}

	@Test
	public void includeMunicipalityFinalCounts_penultimateRecountIsFalse_returnsFalse() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		assertThat(service.includeMunicipalityFinalCounts(AN_AREA_PATH, false)).isFalse();
	}

	@Test
	public void includeMunicipalityFinalCounts_penultimateRecountIsTrueAndUserOnRootLevel_returnsTrue() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		AreaPath areaPathOnRootLevel = AreaPath.from("150001");
		assertThat(service.includeMunicipalityFinalCounts(areaPathOnRootLevel, true)).isTrue();
	}

	@Test
	public void userCanAccessReportingUnitForFinalCount_userOnRootLevel_returnsTrue() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		AreaPath areaPathOnRootLevel = AreaPath.from("150001");
		assertThat(service.userCanAccessReportingUnitForFinalCount(areaPathOnRootLevel)).isTrue();
	}

	@Test
	public void userCanAccessReportingUnitForFinalCount_userOnCountyLevel_returnsTrue() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		AreaPath areaPathOnCountyLevel = AreaPath.from("150001.47.01");
		assertThat(service.userCanAccessReportingUnitForFinalCount(areaPathOnCountyLevel)).isTrue();
	}

	@Test
	public void userCanAccessReportingUnitForFinalCount_userOnMunicipalityLevelButReportingUnitExists_returnsTrue() {
		AreaPath municipalityAreaPath = AreaPath.from("150001.47.01.0101");
		when(reportingUnitRepository.existsFor(municipalityAreaPath, VALGSTYRET)).thenReturn(true);
		assertThat(voteCountService.userCanAccessReportingUnitForFinalCount(municipalityAreaPath)).isTrue();
	}

	@Test
	public void userCanAccessReportingUnitForFinalCount_userOnMunicipalityLevelAndReportingUnitDoesNotExist_returnsFalse() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		AreaPath municipalityAreaPath = AreaPath.from("150001.47.01.0101");
		when(getInjectMock(ReportingUnitRepository.class).existsFor(municipalityAreaPath, VALGSTYRET)).thenReturn(false);
		assertThat(service.userCanAccessReportingUnitForFinalCount(municipalityAreaPath)).isFalse();
	}

	@Test
	public void isFinalCountReadyForSettlement_whenRejectedBallotsNotProcessed_returnsFalse() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		FinalCount aFinalCount = FinalCountMockups.finalCount(APPROVED, 0, true, "", areaPath, "", "", "FVO1", OPPTELLINGSVALGSTYRET);
		aFinalCount.setRejectedBallotsProcessed(false);
		ElectionPath electionPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);

		assertThat(service.isFinalCountReadyForSettlement(electionPath, aFinalCount)).isFalse();
	}

	@Test
	public void isFinalCountReadyForSettlement_whenRejectedBallotsProcessedAndOpptellingsvalgstyret_returnsTrue() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		FinalCount aFinalCount = FinalCountMockups.finalCount(APPROVED, 0, true, "", areaPath, "", "", "FVO1", OPPTELLINGSVALGSTYRET);
		aFinalCount.setRejectedBallotsProcessed(true);
		ElectionPath electionPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);

		assertThat(service.isFinalCountReadyForSettlement(electionPath, aFinalCount)).isTrue();
	}

	@Test
	public void isFinalCountReadyForSettlement_whenRejectedBallotsProcessedAndValgstyretAndContestOnMunicipality_returnsTrue() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		FinalCount aFinalCount = FinalCountMockups.finalCount(APPROVED, 0, true, "", areaPath, "", "", "FVO1", VALGSTYRET);
		aFinalCount.setRejectedBallotsProcessed(true);
		ElectionPath electionPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		MvElection fakeMvElection = createMock(MvElection.class);
		when(fakeMvElection.getContest().isOnMunicipalityLevel()).thenReturn(true);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(fakeMvElection);

		assertThat(service.isFinalCountReadyForSettlement(electionPath, aFinalCount)).isTrue();
	}

	@Test
	public void isFinalCountReadyForSettlement_whenRejectedBallotsProcessedAndFylkesvalgstyretAndContestOnCounty_returnsTrue() throws Exception {
		VoteCountService service = initializeMocks(VoteCountService.class);
		FinalCount aFinalCount = FinalCountMockups.finalCount(APPROVED, 0, true, "", areaPath, "", "", "FVO1", FYLKESVALGSTYRET);
		aFinalCount.setRejectedBallotsProcessed(true);
		ElectionPath electionPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		MvElection fakeMvElection = createMock(MvElection.class);
		when(fakeMvElection.getContest().isOnCountyLevel()).thenReturn(true);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(fakeMvElection);

		assertThat(service.isFinalCountReadyForSettlement(electionPath, aFinalCount)).isTrue();
	}

	@Test
	public void countingMode_countCategoryNotConfiguredForMunicipality_returnsNull() throws Exception {
		VoteCountService voteCountService = initializeMocks(VoteCountService.class);

		when(getInjectMock(ReportCountCategoryRepository.class).findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any()))
				.thenReturn(null);
		CountingMode countingMode = testCountingMode(voteCountService, false, VO);
		assertThat(countingMode).isNull();
	}

	@Test
	public void countingMode_countCategoryConfiguredForMunicipality_returnsCountCategory() throws Exception {
		VoteCountService voteCountService = initializeMocks(VoteCountService.class);

		ReportCountCategory fakeReportCountCategory = mock(ReportCountCategory.class);
		when(fakeReportCountCategory.getCountingMode()).thenReturn(BY_POLLING_DISTRICT);
		when(getInjectMock(ReportCountCategoryRepository.class).findByMunicipalityElectionGroupAndVoteCountCategory(any(), any(), any()))
				.thenReturn(fakeReportCountCategory);

		CountingMode countingMode = testCountingMode(voteCountService, false, VO);
		assertThat(countingMode).isEqualTo(BY_POLLING_DISTRICT);
	}

	@Test
	public void countingMode_boroughElectionAndVo_returnsCentralAndByPollingDistrict() throws Exception {
		VoteCountService voteCountService = initializeMocks(VoteCountService.class);

		CountingMode countingMode = testCountingMode(voteCountService, true, VO);

		assertThat(countingMode).isEqualTo(BY_POLLING_DISTRICT);
	}

	@Test
	public void countingMode_boroughElectionAndNotVo_returnsCentral() throws Exception {
		VoteCountService voteCountService = initializeMocks(VoteCountService.class);

		CountingMode countingMode = testCountingMode(voteCountService, true, FO);

		assertThat(countingMode).isEqualTo(CENTRAL);
	}

	private CountingMode testCountingMode(VoteCountService voteCountService, boolean boroughElection, CountCategory countCategory) {
		Municipality fakeSamiParentMunicipality = mock(Municipality.class);
		MvElection fakeMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(fakeMvElection.getContest().isOnBoroughLevel()).thenReturn(boroughElection);

		return voteCountService.countingMode(countCategory, fakeSamiParentMunicipality, fakeMvElection);
	}

}

