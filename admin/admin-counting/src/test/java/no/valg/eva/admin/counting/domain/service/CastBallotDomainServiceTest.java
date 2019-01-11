package no.valg.eva.admin.counting.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.CastBallotId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.RejectedBallot;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CastBallotDomainServiceTest extends MockUtilsTestCase {
	private static final String B1 = "B1";
	private static final String B2 = "B2";
	private static final String BR1 = "BR1";
	private static final String BR2 = "BR2";
	private static final String CB1 = "CB1";
	private static final String CB2 = "CB2";
	private static final String CB3 = "CB3";
	
	private static final AreaPath A_ROOT_PATH = AreaPath.from("150001");
	private static final AreaPath A_COUNTY_PATH = AreaPath.from("150001.47.01");
	private static final AreaPath A_MUNICIPALITY_PATH = AreaPath.from("150001.47.01.0101");

	private CastBallotDomainService service;
	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(CastBallotDomainService.class);
		userData = mock(UserData.class, RETURNS_DEEP_STUBS);
	}

	@Test
    public void processRejectedBallots_givenMunicipalityUserAndChangedRejectedBallot_changesRejectedBallotCounts() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallot(BR1, BR2));
		Map<String, BallotCount> ballotCountMap = defaultBallotCountMap();
		CastBallot rejectedCastBallot = castBallot(ballotCountMap, BR1, CB1);

		setUpMunicipalityUserData();
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(ballotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(ballotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(BR2).getUnmodifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(BR2).getCastBallots()).containsExactly(rejectedCastBallot);
		assertThat(rejectedCastBallot.getBallotCount()).isEqualTo(ballotCountMap.get(BR2));
	}

	private Object setUpMunicipalityUserData() {
		when(userData.getOperatorAreaPath()).thenReturn(A_MUNICIPALITY_PATH);
		return when(userData.getOperatorAreaLevel()).thenReturn(MUNICIPALITY);
	}

	@Test
    public void processRejectedBallots_givenElectionEventAdminAndChangedRejectedBallot_changesMunicipalityRejectedBallotCounts() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallot(BR1, BR2));
		Map<String, BallotCount> ballotCountMap = defaultBallotCountMap();
		CastBallot rejectedCastBallot = castBallot(ballotCountMap, BR1, CB1);

		when(userData.isElectionEventAdminUser()).thenReturn(true);
		when(userData.getOperatorAreaPath()).thenReturn(A_ROOT_PATH);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_ROOT_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(ballotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(ballotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(BR2).getUnmodifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(BR2).getCastBallots()).containsExactly(rejectedCastBallot);
		assertThat(rejectedCastBallot.getBallotCount()).isEqualTo(ballotCountMap.get(BR2));
	}

	@Test
    public void processRejectedBallots_givenElectionEventAdminAndFylkesvalgstyretAndChangedRejectedBallot_changesCountyRejectedBallotCounts() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(FYLKESVALGSTYRET);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallot(BR1, BR2));
		Map<String, BallotCount> ballotCountMap = defaultBallotCountMap();
		CastBallot rejectedCastBallot = castBallot(ballotCountMap, BR1, CB1);

		when(userData.isElectionEventAdminUser()).thenReturn(true);
		when(userData.getOperatorAreaPath()).thenReturn(A_ROOT_PATH);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(FYLKESVALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_ROOT_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(ballotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(ballotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(BR2).getUnmodifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(BR2).getCastBallots()).containsExactly(rejectedCastBallot);
		assertThat(rejectedCastBallot.getBallotCount()).isEqualTo(ballotCountMap.get(BR2));
	}

	@Test
    public void processRejectedBallots_givenCountyUserAndChangedRejectedBallot_changesCountyRejectedBallotCounts() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallot(BR1, BR2));
		Map<String, BallotCount> ballotCountMap = defaultBallotCountMap();
		CastBallot rejectedCastBallot = castBallot(ballotCountMap, BR1, CB1);

		when(userData.getOperatorAreaLevel()).thenReturn(COUNTY);
		when(userData.getOperatorAreaPath()).thenReturn(A_COUNTY_PATH);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(FYLKESVALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_COUNTY_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(ballotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(ballotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(BR2).getUnmodifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(BR2).getCastBallots()).containsExactly(rejectedCastBallot);
		assertThat(rejectedCastBallot.getBallotCount()).isEqualTo(ballotCountMap.get(BR2));
	}

	@Test
    public void processRejectedBallots_givenMunicipalityUserAndNewModifiedBallot_removesRejectedBallotAndAddsModifiedBallot() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallotChangedToModifiedBallot(CB1, BR1, B1));
		CastBallot castBallot = castBallot(CB1, REJECTED);
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot));
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B1, 0, 0));

		setUpMunicipalityUserData();
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(rejectedBallotCountMap);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getBallotCountMap())
				.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(rejectedBallotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(rejectedBallotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(B1).getModifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(B1).getCastBallots()).containsExactly(castBallot);
		assertThat(castBallot.getBallotCount()).isEqualTo(ballotCountMap.get(B1));
	}

	@Test
    public void processRejectedBallots_givenCountyUserAndNewModifiedBallot_removesCountyRejectedBallotAndAddsCountyModifiedBallot() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallotChangedToModifiedBallot(CB1, BR1, B1));
		CastBallot castBallot = castBallot(CB1, REJECTED);
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot));
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B1, 0, 0));

		when(userData.getOperatorAreaLevel()).thenReturn(COUNTY);
		when(userData.getOperatorAreaPath()).thenReturn(A_COUNTY_PATH);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(FYLKESVALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_COUNTY_PATH)
				.getRejectedBallotCountMap())
						.thenReturn(rejectedBallotCountMap);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(FYLKESVALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(),
						A_COUNTY_PATH)
				.getBallotCountMap())
						.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(rejectedBallotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(rejectedBallotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(B1).getModifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(B1).getCastBallots()).containsExactly(castBallot);
		assertThat(castBallot.getBallotCount()).isEqualTo(ballotCountMap.get(B1));
	}

	@Test
    public void processRejectedBallots_givenMunicipalityUserAndNewUnmodifiedBallot_removesRejectedBallotAndAddsUnmodifiedBallot() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallotChangedToUnmodifiedBallot(CB1, BR1, B1));
		CastBallot castBallot = castBallot(CB1, REJECTED);
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot));
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B1, 0, 0));

		setUpMunicipalityUserData();
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(rejectedBallotCountMap);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getBallotCountMap())
				.thenReturn(ballotCountMap);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(rejectedBallotCountMap.get(BR1).getUnmodifiedBallots()).isZero();
		assertThat(rejectedBallotCountMap.get(BR1).getCastBallots()).isEmpty();
		assertThat(ballotCountMap.get(B1).getUnmodifiedBallots()).isEqualTo(1);
		assertThat(ballotCountMap.get(B1).getCastBallots()).containsExactly(castBallot);
		assertThat(castBallot.getBallotCount()).isEqualTo(ballotCountMap.get(B1));
	}

	@SuppressWarnings("unchecked")
	@Test
    public void processRejectedBallots_givenMunicipalityUserAndTwoNewUnknownUnmodifiedBallot_addNewBallotCountWithTwoUnmodifiedBallots() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots =
				rejectedBallots(rejectedBallotChangedToUnmodifiedBallot(CB1, BR1, B1), rejectedBallotChangedToUnmodifiedBallot(CB2, BR1, B1));
		CastBallot castBallot1 = castBallot(CB1, REJECTED);
		CastBallot castBallot2 = castBallot(CB2, REJECTED);
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot1, castBallot2));
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B2, 0, 0));
		BallotCount approvedBallotCount1 = approvedBallotCount(B1, 0, 0);
		BallotCount approvedBallotCountThatShouldNotBeUsed = approvedBallotCount(B1, 0, 0);
		Ballot ballot1 = approvedBallotCount1.getBallot();
		Map<String, BallotCount> ballotCountMapWithApprovedBallotCount1 = ballotCountMap(approvedBallotCount1, approvedBallotCount(B2, 0, 0));

		setUpMunicipalityUserData();
		VoteCount approvedFinalVoteCount = getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH);
		when(approvedFinalVoteCount.getRejectedBallotCountMap()).thenReturn(rejectedBallotCountMap);
		when(approvedFinalVoteCount.getBallotCountMap()).thenReturn(ballotCountMap, ballotCountMapWithApprovedBallotCount1);
		when(approvedFinalVoteCount.getContestReport().getContest().getPk()).thenReturn(1L);
		when(getInjectMock(BallotRepository.class).findByContestAndId(1L, B1)).thenReturn(ballot1);
		when(approvedFinalVoteCount.addNewBallotCount(ballot1, 0, 0)).thenReturn(approvedBallotCount1, approvedBallotCountThatShouldNotBeUsed);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		assertThat(approvedBallotCount1.getUnmodifiedBallots()).isEqualTo(2);
		assertThat(approvedBallotCountThatShouldNotBeUsed.getUnmodifiedBallots()).isZero();
	}

	@Test
    public void processRejectedBallots_givenMunicipalityUserAndNewApprovedBallot_updatesVoteCount() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallotChangedToModifiedBallot(CB1, BR1, B1));
		CastBallot castBallot = castBallot(CB1, REJECTED);
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot));
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B1, 0, 1));

		setUpMunicipalityUserData();
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getRejectedBallotCountMap())
						.thenReturn(rejectedBallotCountMap);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getBallotCountMap())
						.thenReturn(ballotCountMap);
		when(getInjectMock(VoteCountService.class)
						.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getRejectedBallots())
						.thenReturn(1);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		verify(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH))
								.setApprovedBallots(1);
		verify(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH))
								.setRejectedBallots(0);
	}

	@Test
    public void processRejectedBallots_givenMunicipalityUserAndNewModifiedBallotAndLastReportingUnit_createsNewModifiedBallotBatch() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots = rejectedBallots(rejectedBallotChangedToModifiedBallot(CB1, BR1, B1));
		CastBallot castBallot = castBallot(CB1, REJECTED);
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot));
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B1, 0, 1));

		initVoteCountService(approvedFinalCountRef, rejectedBallotCountMap, ballotCountMap, true);
		when(userData.getOperatorAreaPath()).thenReturn(A_MUNICIPALITY_PATH);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		ArgumentCaptor<ModifiedBallotBatch> modifiedBallotBatchArgumentCaptor = ArgumentCaptor.forClass(ModifiedBallotBatch.class);
		verify(getInjectMock(ModifiedBallotBatchRepository.class)).createModifiedBallotBatch(eq(userData), modifiedBallotBatchArgumentCaptor.capture());
		ModifiedBallotBatch modifiedBallotBatch = modifiedBallotBatchArgumentCaptor.getValue();
		assertThat(modifiedBallotBatch.getBallotCount()).isEqualTo(ballotCountMap.get(B1));
		assertThat(modifiedBallotBatch.getBatchMembers()).hasSize(1);
		assertThat(modifiedBallotBatch.getBatchMembers().iterator().next().getCastBallot()).isEqualTo(castBallot);
	}

	@Test
    public void processRejectedBallots_givenMunicipalityUserAndNewUnmodifiedAndModifiedBallotsAndLastReportingUnit_createsCompletedModifiedBallotBatch() {
		ApprovedFinalCountRef approvedFinalCountRef = approvedFinalCountRef(null);
		List<RejectedBallot> rejectedBallots =
				rejectedBallots(rejectedBallotChangedToUnmodifiedBallot(CB2, BR1, B1), rejectedBallotChangedToModifiedBallot(CB3, BR1, B2));
		Map<String, BallotCount> rejectedBallotCountMap = ballotCountMap(rejectedBallotCount(BR1, castBallot(CB2, REJECTED), castBallot(CB3, REJECTED)));
		CastBallot modifiedCastBallot = castBallot(CB1, MODIFIED);
		Map<String, BallotCount> ballotCountMap = ballotCountMap(approvedBallotCount(B1, 1, 1, modifiedCastBallot), approvedBallotCount(B2, 0, 0));

		initVoteCountService(approvedFinalCountRef, rejectedBallotCountMap, ballotCountMap, true);
		when(userData.getOperatorAreaPath()).thenReturn(A_MUNICIPALITY_PATH);

		service.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);

		ArgumentCaptor<ModifiedBallotBatch> modifiedBallotBatchArgumentCaptor = ArgumentCaptor.forClass(ModifiedBallotBatch.class);
		verify(getInjectMock(ModifiedBallotBatchRepository.class), times(2)).createModifiedBallotBatch(eq(userData), modifiedBallotBatchArgumentCaptor.capture());
		ModifiedBallotBatch modifiedBallotBatch = modifiedBallotBatchArgumentCaptor.getAllValues().get(1);
		assertThat(modifiedBallotBatch.getBallotCount()).isEqualTo(ballotCountMap.get(B1));
		assertThat(modifiedBallotBatch.getBatchMembers()).hasSize(1);
		ModifiedBallotBatchMember modifiedBallotBatchMember = modifiedBallotBatch.getBatchMembers().iterator().next();
		assertThat(modifiedBallotBatchMember.isDone()).isTrue();
		assertThat(modifiedBallotBatchMember.getCastBallot()).isEqualTo(modifiedCastBallot);
	}

	private void initVoteCountService(ApprovedFinalCountRef approvedFinalCountRef, Map<String, BallotCount> rejectedBallotCountMap,
									  Map<String, BallotCount> ballotCountMap, boolean lastReportingUnitForContest) {
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getRejectedBallotCountMap())
				.thenReturn(rejectedBallotCountMap);
		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath(), A_MUNICIPALITY_PATH)
				.getBallotCountMap())
				.thenReturn(ballotCountMap);
		when(getInjectMock(VoteCountService.class)
				.isLastReportingUnitForContest(VALGSTYRET, approvedFinalCountRef.countContext(), approvedFinalCountRef.countingAreaPath()))
				.thenReturn(lastReportingUnitForContest);
	}

	private ApprovedFinalCountRef approvedFinalCountRef(ReportingUnitTypeId overrideReportingUnitTypeId) {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountContext countContext = new CountContext(contestPath, VO);
		AreaPath countingAreaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		return new ApprovedFinalCountRef(overrideReportingUnitTypeId, countContext, countingAreaPath);
	}

	private List<RejectedBallot> rejectedBallots(RejectedBallot... rejectedBallots) {
		return asList(rejectedBallots);
	}

	private RejectedBallot rejectedBallot(String initialBallotRejectionId, String selectedBallotRejectionId) {
		RejectedBallot rejectedBallot = new RejectedBallot(CB1, initialBallotRejectionId);
		rejectedBallot.setSelectedBallotRejectionId(selectedBallotRejectionId);
		return rejectedBallot;
	}

	private RejectedBallot rejectedBallotChangedToModifiedBallot(String castBallotId, String initialBallotRejectionId, String selectedBallotId) {
		return rejectedBallot(castBallotId, initialBallotRejectionId, true, selectedBallotId);
	}

	private RejectedBallot rejectedBallotChangedToUnmodifiedBallot(String cb2, String br1, String b1) {
		return rejectedBallot(cb2, br1, false, b1);
	}

	private RejectedBallot rejectedBallot(String castBallotId, String initialBallotRejectionId, boolean modified, String selectedBallotId) {
		RejectedBallot rejectedBallot = new RejectedBallot(castBallotId, initialBallotRejectionId);
		if (modified) {
			rejectedBallot.setState(RejectedBallot.State.MODIFIED);
		} else {
			rejectedBallot.setState(RejectedBallot.State.UNMODIFIED);
		}
		rejectedBallot.setSelectedBallotId(selectedBallotId);
		return rejectedBallot;
	}

	private CastBallotId castBallotId(String castBallotId) {
		return new CastBallotId(castBallotId);
	}

	private CastBallot castBallot(String id, CastBallot.Type type) {
		CastBallot castBallot = new CastBallot();
		castBallot.setPk(new Random().nextLong());
		castBallot.setId(id);
		castBallot.setType(type);
		return castBallot;
	}

	private CastBallot castBallot(Map<String, BallotCount> ballotCountMap, String id, String castBallotId) {
		return ballotCountMap.get(id).getCastBallot(castBallotId(castBallotId));
	}

	private Map<String, BallotCount> defaultBallotCountMap() {
		CastBallot rejectedCastBallot = castBallot(CB1, REJECTED);
		return ballotCountMap(rejectedBallotCount(BR1, rejectedCastBallot), rejectedBallotCount(BR2));
	}

	private Map<String, BallotCount> ballotCountMap(BallotCount... ballotCounts) {
		Map<String, BallotCount> ballotCountMap = new LinkedHashMap<>();
		for (BallotCount ballotCount : ballotCounts) {
			String ballotId = ballotCount.getBallotId();
			if (ballotId != null) {
				ballotCountMap.put(ballotId, ballotCount);
			} else {
				ballotCountMap.put(ballotCount.getBallotRejectionId(), ballotCount);
			}
		}
		return ballotCountMap;
	}

	private BallotCount approvedBallotCount(String ballotId, int modifiedBallots, int unmodifiedBallots, CastBallot... castBallots) {
		return ballotCount(ballot(ballotId), null, modifiedBallots, unmodifiedBallots, castBallots);
	}

	private BallotCount rejectedBallotCount(String ballotRejectionId, CastBallot... castBallots) {
		return ballotCount(null, ballotRejection(ballotRejectionId), 0, castBallots.length, castBallots);
	}

	private BallotCount ballotCount(Ballot ballot, BallotRejection ballotRejection, int modifiedBallots, int unmodifiedBallots, CastBallot... castBallots) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(new Random().nextLong());
		if (ballot != null) {
			ballotCount.setBallot(ballot);
		} else {
			ballotCount.setBallotRejection(ballotRejection);
		}
		ballotCount.setModifiedBallots(modifiedBallots);
		ballotCount.setUnmodifiedBallots(unmodifiedBallots);
		for (CastBallot castBallot : castBallots) {
			castBallot.setBallotCount(ballotCount);
			ballotCount.getCastBallots().add(castBallot);
		}
		return ballotCount;
	}

	private Ballot ballot(String id) {
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		ballot.setId(id);
		return ballot;
	}

	private BallotRejection ballotRejection(String ballotRejectionId) {
		BallotRejection ballotRejection = new BallotRejection();
		ballotRejection.setPk(new Random().nextLong());
		ballotRejection.setId(ballotRejectionId);
		return ballotRejection;
	}
}
