package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.AbstractCount;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.counting.view.CompareBallotCountView;
import no.valg.eva.admin.frontend.counting.view.CompareBallotCounts;
import no.valg.eva.admin.frontend.counting.view.Tab;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CompareCountsControllerTest extends BaseCountControllerTest {
	private static final String A_BOROUGH_PATH = "950004.47.03.0301.030010";
	private static final int ONE_HUNDRED = 100;
	private static final int ONE = 1;
	private static final int TEN = 10;
	private static final int EIGHT = 8;
	private static final int FIVE = 5;
	private static final int THREE = 3;
	private static final int ZERO = 0;
	private static final int TWO = 2;
	private static final int SEVEN = 7;
	private static final int FOUR = 4;
	private static final int SIX = 6;
	private static final int ELEVEN = 11;
	private static final int TWELVE = 12;
	private static final int NINE = 9;
	private static final int SIXTEEN = 16;
	private static final int TWENTY_EIGHT = 28;
	private static final int THIRTY_FIVE = 35;

	private CompareCountsController ctrl;
	private FinalCountController finalCountControllerMock;
	private CountyFinalCountController countyFinalCountControllerMock;

	@DataProvider(name = "getApproveButton")
	public static Object[][] getApproveButton() {
		return new Object[][] {
				{ false, false, true, false, false, true },
				{ true, false, true, false, false, true },
				{ true, true, true, false, false, true },
				{ true, true, false, false, false, true },
				{ true, true, false, true, true, false },
		};
	}

	@DataProvider(name = "setupDefaultCompare")
	public static Object[][] setupDefaultCompare() {
		return new Object[][] {
				{ THREE, true, true, false, false, "3", "" },
				{ THREE, false, false, false, false, "2", "3" },
				{ ONE, false, false, false, true, "1", "" }
		};
	}

	@DataProvider(name = "getRevokeButton")
	public static Object[][] getRevokeButton() {
		return new Object[][] {
				{ false, false, false, true },
				{ true, false, false, true },
				{ true, true, true, false }
		};
	}

	@DataProvider(name = "getNewFinalCountButton")
	public static Object[][] getNewFinalCountButton() {
		return new Object[][] {
				{ true, false, false, true, true },
				{ false, false, false, true, true },
				{ false, true, false, true, true },
				{ false, true, true, true, false }
		};
	}

	@DataProvider(name = "isCountEditable")
	public static Object[][] isCountEditable() {
		return new Object[][] {
				{ false, false, true, true, false },
				{ true, false, true, true, false },
				{ true, true, true, true, false },
				{ true, true, false, true, false },
				{ true, true, false, false, true }
		};
	}

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(CompareCountsController.class);

		finalCountControllerMock = mock(FinalCountController.class, RETURNS_DEEP_STUBS);
		countyFinalCountControllerMock = mock(CountyFinalCountController.class, RETURNS_DEEP_STUBS);
		ctrl.setTabIndex(TWO);
		PreliminaryCount preliminaryCountMock = mock(PreliminaryCount.class, RETURNS_DEEP_STUBS);

		when(getStartCountingControllerMock().getCountCategory()).thenReturn(CountCategory.VO);
		when(getStartCountingControllerMock().getContestPath()).thenReturn(new ElectionPath("730001.01.01.000001"));
		when(getStartCountingControllerMock().getAreaPath()).thenReturn(new AreaPath("123456.78.90.1234.567890.0000"));
		hasAccess();
		when(getCountsMock().getPreliminaryCount()).thenReturn(preliminaryCountMock);

		setupWithFinalCount();
	}

	@Test(dataProvider = "setupDefaultCompare")
	public void setupDefaultCompare_parameterized_thenCheckState(
            int count, boolean ctrlIsApproved, boolean countApproved,
            boolean isNoProcessed, boolean isOneProcessed, String firstId, String secondId) {
		List<FinalCount> finalCounts = setupFinalCounts(count);
		when(finalCountControllerMock.isApproved()).thenReturn(ctrlIsApproved);
		if (countApproved) {
			when(finalCounts.get(count - ONE).isApproved()).thenReturn(true);
		}

		ctrl.setupDefaultCompare();

		assertThat(ctrl.isNoProcessed()).isEqualTo(isNoProcessed);
		assertThat(ctrl.isOneProcessed()).isEqualTo(isOneProcessed);
		assertThat(ctrl.getFirstCountSelect().getId()).isEqualTo(firstId);
		assertThat(ctrl.getSecondCountSelect().getId()).isEqualTo(secondId);
	}

	@Test
    public void newFinalCount_withTabIndex2_checkPreviousTabSet() {
		ctrl.setTabIndex(TWO);

		ctrl.newFinalCount();

		verify(finalCountControllerMock).newFinalCount();
		verify(getStartCountingControllerMock()).setCurrentTab(ONE);
	}

	@Test
    public void saveComment_withValidateError_shouldDisplayErrorMessage() {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		validateException().when(finalCounts.get(ZERO)).validate();

		ctrl.saveComment(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void saveComment_withNoErrors_shouldDisplaySavedMessage() {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		when(getCountingServiceMock().saveCount(any(UserData.class), any(CountContext.class), any(FinalCount.class)))
				.thenReturn(finalCounts.get(ZERO));

		ctrl.saveComment(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@count.isSaved");

	}

	@Test
    public void approve_withValidateError_shouldDisplayErrorMessage() {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		validateException().when(finalCounts.get(ZERO)).validateForApproval();

		ctrl.approve(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void approve_withCommentError_shouldDisplayErrorMessage() {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		when(finalCounts.get(ZERO).hasComment()).thenReturn(false);
		when(getCountsMock().getTotalBallotCountDifferenceBetween(any(CountQualifier.class), any(CountQualifier.class), anyString())).thenReturn(ONE_HUNDRED);

		ctrl.approve(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@count.error.validation.missing_comment", null);
	}

	@Test
    public void approve_withNoError_shouldDisplaySavedMessage() {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		when(finalCounts.get(ZERO).hasComment()).thenReturn(true);
		when(getCountingServiceMock().approveCount(any(UserData.class), any(CountContext.class), any(FinalCount.class)))
				.thenReturn(finalCounts.get(ZERO));

		ctrl.approve(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@count.isApproved");
	}

	@Test
    public void revokeCount_withSaveError_shouldDisplayErrorMessage() {
		setupFinalCounts(ONE);
		validateException().when(getCountingServiceMock()).revokeCount(any(UserData.class), any(CountContext.class),
				any(FinalCount.class));

		ctrl.revoke(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void revokeCount_withNoError_shouldDisplaySavedMessage() {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		when(finalCounts.get(ZERO).hasComment()).thenReturn(true);
		when(getCountingServiceMock().revokeCount(any(UserData.class), any(CountContext.class), any(FinalCount.class)))
				.thenReturn(finalCounts.get(ZERO));

		ctrl.revoke(ctrl.getFirstCountSelect());

		verify(getMessageProviderMock()).get("@count.isNotApprovedAnymore");
	}

	@Test
    public void getApproveButton_withNoAccess_returnsNotRendered() {
		Button button = ctrl.getApproveButton(ctrl.getFirstCountSelect());

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test(dataProvider = "getApproveButton")
	public void getApproveButton_parameterized_checkRenderedDisabled(boolean editable, boolean processed, boolean approved, boolean previousApproved,
                                                                     boolean isRendered, boolean isDisabled) {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		when(finalCounts.get(ZERO).isEditable()).thenReturn(editable);
		when(finalCounts.get(ZERO).isModifiedBallotsProcessed()).thenReturn(processed);
		when(finalCountControllerMock.isApproved()).thenReturn(approved);
		when(finalCountControllerMock.isPreviousApproved()).thenReturn(previousApproved);

		Button button = ctrl.getApproveButton(ctrl.getFirstCountSelect());

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@Test
    public void getRevokeButton_withNoAccess_returnsNotRendered() {
		Button button = ctrl.getRevokeButton(ctrl.getFirstCountSelect());

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test(dataProvider = "getRevokeButton")
	public void getRevokeButton_parameterized_checkRenderedDisabled(
            boolean isApproved, boolean isPreviousApproved, boolean isRendered, boolean isDisabled) {
		List<FinalCount> finalCounts = setupFinalCounts(ONE);
		when(finalCounts.get(ZERO).isApproved()).thenReturn(isApproved);
		when(finalCountControllerMock.isPreviousApproved()).thenReturn(isPreviousApproved);

		Button button = ctrl.getRevokeButton(ctrl.getFirstCountSelect());

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@Test
    public void getNewFinalCountButton_withNoAccess_returnsNotRendered() {
		hasAccess(Opptelling_Forhånd_Rediger, false);
		hasAccess(Opptelling_Valgting_Rediger, false);

		Button button = ctrl.getNewFinalCountButton();

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test(dataProvider = "getNewFinalCountButton")
	public void getNewFinalCountButton_parameterized_checkRenderedDisabled(
            boolean isApproved, boolean isModifiedBallotsProcessed, boolean isPreviousApproved,
            boolean isRendered, boolean isDisabled) {

		setupFinalCounts(ONE);
		when(finalCountControllerMock.isApproved()).thenReturn(isApproved);

		when(finalCountControllerMock.getFinalCount().isModifiedBallotsProcessed()).thenReturn(isModifiedBallotsProcessed);
		when(finalCountControllerMock.isPreviousApproved()).thenReturn(isPreviousApproved);

		Button button = ctrl.getNewFinalCountButton();

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@Test
    public void getTotalBallotCountDifferenceFromPreviousCount_withCounty_checkCounts() {
		setupWithCountyFinalCount(true);
		List<FinalCount> counts = setupFinalCounts(ONE);
		when(getCountsMock().getCountyTotalBallotCountDifference("1")).thenReturn(TEN);

		int count = ctrl.getTotalBallotCountDifferenceFromPreviousCount(counts.get(ZERO));

		assertThat(count).isEqualTo(TEN);
	}

	@Test
    public void getBaseCount_withCounty_checkCount() {
		setupWithCountyFinalCount(true);
		setupFinalCounts(ONE);

		AbstractCount count = ctrl.getBaseCount();

		assertThat(count instanceof FinalCount).isTrue();
		assertThat(count.getId()).isEqualTo("1");
	}

	@Test
    public void getBaseCount_withNoCounty_checkCount() {
		setupFinalCounts(ONE);

		AbstractCount count = ctrl.getBaseCount();

		assertThat(count instanceof PreliminaryCount).isTrue();
	}

	@Test
    public void getBaseCountNameKey_withCounty_shouldReturnKE() {
		setupWithCountyFinalCount(true);

		assertThat(ctrl.getBaseCountNameKey()).isEqualTo("@count.tab.type[KE].approved");
	}

	@Test
    public void getBaseCountNameKey_withCountyButMunicipalityOnlyCountsPreliminary_shouldReturnF() {
		setupWithCountyFinalCount(false);

		assertThat(ctrl.getBaseCountNameKey()).isEqualTo("@count.tab.type[F].approved");
	}

	@Test
    public void getBaseCountNameKey_withNoCounty_shouldReturnF() {
		assertThat(ctrl.getBaseCountNameKey()).isEqualTo("@count.tab.type[F].approved");
	}

	@Test
    public void isApproved_isAlwaysFalse() {
		assertThat(ctrl.isApproved()).isFalse();
	}

	@Test(dataProvider = "isCountEditable")
	public void isCountEditable_parameterized_checkResult(
            boolean isEditable, boolean isPreviousApproved, boolean isNextApproved, boolean isApproved, boolean result) {
		FinalCount finalCountStub = mock(FinalCount.class, RETURNS_DEEP_STUBS);
		when(finalCountStub.isEditable()).thenReturn(isEditable);
		when(finalCountControllerMock.isPreviousApproved()).thenReturn(isPreviousApproved);
		when(finalCountControllerMock.isNextApproved()).thenReturn(isNextApproved);
		when(finalCountControllerMock.isApproved()).thenReturn(isApproved);

		assertThat(ctrl.isCountEditable(finalCountStub)).isEqualTo(result);
	}

	@Test
    public void getBallotCountViewsForBase_withFinalCount_checkResult() {
		CompareBallotCounts counts = ctrl.getBallotCountViewsForBase(getFinalCount(ONE), getFinalCount(TWO));

		assertThat(counts.size()).isEqualTo(NINE);
		assertView(counts.get(ZERO), "id1", "name1", FIVE, THREE, TWO);
		assertView(counts.get(ONE), "id2", "name2", SEVEN, FOUR, THREE);
		assertView(counts.get(TWO), null, "@count.label.ballot_total", TWELVE, null, null);
		assertView(counts.get(THREE), null, "@count.label.blancs", ELEVEN, null, null);
		assertView(counts.get(FOUR), null, "@count.ballot.approve.rejected.proposed", null, null, null);
		assertView(counts.get(FIVE), "id1", "name1", TWO, null, null);
		assertView(counts.get(SIX), "id2", "name2", THREE, null, null);
		assertView(counts.get(SEVEN), null, "@count.ballot.totalRejected", FIVE, null, null);
		assertView(counts.get(EIGHT), null, "@count.ballot.total", TWENTY_EIGHT, null, null);
	}

	@Test
    public void getBallotCountViewsForBase_withPreliminary_checkResult() {
		CompareBallotCounts counts = ctrl.getBallotCountViewsForBase(getPreliminaryCount(ONE), getFinalCount(TWO));

		assertThat(counts.size()).isEqualTo(NINE);
		assertView(counts.get(ZERO), "id1", "name1", FIVE, THREE, TWO);
		assertView(counts.get(ONE), "id2", "name2", SEVEN, FOUR, THREE);
		assertView(counts.get(TWO), null, "@count.label.ballot_total", TWELVE, null, null);
		assertView(counts.get(THREE), null, "@count.label.blancs", ELEVEN, null, null);
		assertView(counts.get(FOUR), null, "@count.ballot.approve.rejected.proposed", null, null, null);
		assertView(counts.get(FIVE), "id1", "name1", null, null, null);
		assertView(counts.get(SIX), "id2", "name2", null, null, null);
		assertView(counts.get(SEVEN), null, "@count.ballot.totalRejected", TWELVE, null, null);
		assertView(counts.get(EIGHT), null, "@count.ballot.total", THIRTY_FIVE, null, null);
	}

	@Test
    public void getBallotCountViewsFor_withFinalCountNull_checkResult() {
		when(finalCountControllerMock.getFinalCount()).thenReturn(getFinalCount(ONE));
		CompareBallotCounts counts = ctrl.getBallotCountViewsFor(getPreliminaryCount(ONE), null);

		assertThat(counts.size()).isEqualTo(EIGHT);
		for (int i = ZERO; i < EIGHT; i++) {
			assertView(counts.get(i), null, null, null, null, null, null);
		}
	}

	@Test
    public void getBallotCountViewsFor_withPreliminaryAndFinal_checkResult() {
		CompareBallotCounts counts = ctrl.getBallotCountViewsFor(getPreliminaryCount(ONE), getFinalCount(TWO));

		assertThat(counts.size()).isEqualTo(NINE);
		assertView(counts.get(ZERO), "id1", "name1", SEVEN, FOUR, THREE, TWO);
		assertView(counts.get(ONE), "id2", "name2", NINE, FIVE, FOUR, TWO);
		assertView(counts.get(TWO), null, null, SIXTEEN, null, null, FOUR);
		assertView(counts.get(THREE), null, null, TWELVE, null, null, ONE);
		assertView(counts.get(FOUR), null, null, null, null, null, null);
		assertView(counts.get(FIVE), "id1", "name1", THREE, null, null, null);
		assertView(counts.get(SIX), "id2", "name2", FOUR, null, null, null);
		assertView(counts.get(SEVEN), null, null, SEVEN, null, null, -FIVE);
		assertView(counts.get(EIGHT), null, "@count.ballot.total", THIRTY_FIVE, null, null, ZERO);
	}

	@Test
    public void getBallotCountViewsFor_withFinalAndFinal_checkResult() {
		CompareBallotCounts counts = ctrl.getBallotCountViewsFor(getFinalCount(ONE), getFinalCount(TWO));

		assertThat(counts.size()).isEqualTo(NINE);
		assertView(counts.get(ZERO), "id1", "name1", SEVEN, FOUR, THREE, TWO);
		assertView(counts.get(ONE), "id2", "name2", NINE, FIVE, FOUR, TWO);
		assertView(counts.get(TWO), null, null, SIXTEEN, null, null, FOUR);
		assertView(counts.get(THREE), null, null, TWELVE, null, null, ONE);
		assertView(counts.get(FOUR), null, null, null, null, null, null);
		assertView(counts.get(FIVE), "id1", "name1", THREE, null, null, ONE);
		assertView(counts.get(SIX), "id2", "name2", FOUR, null, null, ONE);
		assertView(counts.get(SEVEN), null, null, SEVEN, null, null, TWO);
		assertView(counts.get(EIGHT), null, "@count.ballot.total", THIRTY_FIVE, null, null, SEVEN);
	}

	@Test
	public void getProcessedFinalCounts_withIsRejectedBallotsProcessed_returnsOneFinalCount() throws Exception {
		FinalCountController finalCountController = mockField("finalCountController", FinalCountController.class);
		List<FinalCount> finalCounts = new ArrayList<>();
		finalCounts.add(createMock(FinalCount.class));
		when(finalCounts.get(0).isModifiedBallotsProcessed()).thenReturn(true);
		when(finalCounts.get(0).isRejectedBallotsProcessed()).thenReturn(false);
		finalCounts.add(createMock(FinalCount.class));
		when(finalCounts.get(1).isModifiedBallotsProcessed()).thenReturn(false);
		when(finalCounts.get(1).isRejectedBallotsProcessed()).thenReturn(true);
		finalCounts.add(createMock(FinalCount.class));
		when(finalCounts.get(2).isModifiedBallotsProcessed()).thenReturn(false);
		when(finalCounts.get(2).isRejectedBallotsProcessed()).thenReturn(false);
		when(finalCountController.getFinalCounts()).thenReturn(finalCounts);

		List<FinalCount> filtered = ctrl.getProcessedFinalCounts();

		assertThat(filtered).hasSize(2);
	}

	private void assertView(CompareBallotCountView view, String id, String nameKey, Integer count, Integer modified, Integer unmodified) {
		assertView(view, id, nameKey, count, modified, unmodified, null);
	}

	private void assertView(CompareBallotCountView view, String id, String nameKey, Integer count, Integer modified, Integer unmodified, Integer diff) {
		assertThat(view.getId()).isEqualTo(id);
		assertThat(view.getNameKey()).isEqualTo(nameKey);
		assertThat(view.getCount()).isEqualTo(count);
		assertThat(view.getModifiedCount()).isEqualTo(modified);
		assertThat(view.getUnmodifiedCount()).isEqualTo(unmodified);
		assertThat(view.getDiff()).isEqualTo(diff);
	}

	private void setupWithFinalCount() {
		when(getUserDataMock().getOperatorAreaPath().getLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		Tab tabMock = mock(Tab.class, RETURNS_DEEP_STUBS);
		when(getStartCountingControllerMock().getTabs().get(anyInt())).thenReturn(tabMock);
		when(tabMock.getController()).thenReturn(finalCountControllerMock);
		ctrl.setFinalCountController(finalCountControllerMock);
		ctrl.initCountController();
	}

	private void setupWithCountyFinalCount(boolean municipalityOnlyCountsPreliminary) {
		when(getUserDataMock().getOperatorAreaPath().isCountyLevel()).thenReturn(true);
		Tab tabMock = mock(Tab.class, RETURNS_DEEP_STUBS);
		when(getStartCountingControllerMock().getTabs().get(anyInt())).thenReturn(tabMock);
		when(getStartCountingControllerMock().isUserOnCountyLevel()).thenReturn(true);
		when(tabMock.getController()).thenReturn(countyFinalCountControllerMock);
		when(getStartCountingControllerMock().getCounts().municipalityCountsFinal()).thenReturn(municipalityOnlyCountsPreliminary);
		ctrl.setFinalCountController(countyFinalCountControllerMock);
		ctrl.initCountController();
	}

	private List<FinalCount> setupFinalCounts(int count) {
		return setupFinalCounts(count, true);
	}

	private List<FinalCount> setupFinalCounts(int count, boolean processed) {
		List<FinalCount> result = new ArrayList<>();
		for (int i = ZERO; i < count; i++) {
			FinalCount stub = mock(FinalCount.class, RETURNS_DEEP_STUBS);
			when(stub.isModifiedBallotsProcessed()).thenReturn(processed);
			when(stub.getId()).thenReturn(String.valueOf(i + ONE));
			when(stub.getIndex()).thenReturn(i + ONE);
			result.add(stub);
		}
		when(getCountsMock().getFinalCountIndex()).thenReturn(count - ONE);
		when(getCountsMock().getFinalCounts()).thenReturn(result);
		when(finalCountControllerMock.getFinalCount()).thenReturn(result.get(count - ONE));
		when(finalCountControllerMock.getFinalCounts()).thenReturn(result);
		when(countyFinalCountControllerMock.getFinalCount()).thenReturn(result.get(count - ONE));
		when(countyFinalCountControllerMock.getFinalCounts()).thenReturn(result);
		ctrl.initCountController();
		return result;
	}

	private PreliminaryCount getPreliminaryCount(int countBaseline) {
		PreliminaryCount result = new PreliminaryCount("", AreaPath.from(A_BOROUGH_PATH), CountCategory.VO, "", "", false);
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(new BallotCount("id1", "name1", countBaseline + ONE, countBaseline + TWO));
		ballotCounts.add(new BallotCount("id2", "name2", countBaseline + TWO, countBaseline + THREE));
		result.setBallotCounts(ballotCounts);
		result.setBlankBallotCount(countBaseline + TEN);
		result.setQuestionableBallotCount(countBaseline + ELEVEN);
		return result;
	}

	private FinalCount getFinalCount(int countBaseline) {
		FinalCount result = new FinalCount("", AreaPath.from(A_BOROUGH_PATH), CountCategory.VO, "", ReportingUnitTypeId.VALGSTYRET, "", false);
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(new BallotCount("id1", "name1", countBaseline + ONE, countBaseline + TWO));
		ballotCounts.add(new BallotCount("id2", "name2", countBaseline + TWO, countBaseline + THREE));
		result.setBallotCounts(ballotCounts);
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		rejectedBallotCounts.add(new RejectedBallotCount("id1", "name1", countBaseline + ONE));
		rejectedBallotCounts.add(new RejectedBallotCount("id2", "name2", countBaseline + TWO));
		result.setRejectedBallotCounts(rejectedBallotCounts);
		result.setBlankBallotCount(countBaseline + TEN);
		return result;
	}

}
