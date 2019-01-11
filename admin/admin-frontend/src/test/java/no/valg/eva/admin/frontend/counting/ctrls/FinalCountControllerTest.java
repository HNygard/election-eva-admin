package no.valg.eva.admin.frontend.counting.ctrls;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.counting.view.Tab;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Rettelser_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class FinalCountControllerTest extends BaseCountControllerTest {

	private FinalCountController ctrl;

	@DataProvider(name = "buttonRegisterCorrections")
	public static Object[][] buttonRegisterCorrections() {
		return new Object[][] {
				{ false, false, true, true, false, true, true, false, false, true },
				{ true, false, true, true, false, true, true, false, false, true },
				{ true, true, true, true, false, true, true, false, false, true },
				{ true, true, false, true, false, true, true, false, false, true },
				{ true, true, false, true, true, true, true, false, false, true },
				{ true, true, false, true, true, false, true, false, false, true },
				{ true, true, false, true, true, false, false, false, false, true },
				{ true, true, false, true, true, false, false, true, true, false }
		};
	}

	@DataProvider(name = "buttonModifiedBallotProcessed")
	public static Object[][] buttonModifiedBallotProcessed() {
		return new Object[][] {
				{ false, true, false, true, false, true, true, false, false, true },
				{ true, true, false, true, false, true, true, false, false, true },
				{ true, false, false, true, false, true, true, false, false, true },
				{ true, false, false, true, true, true, true, false, false, true },
				{ true, false, false, true, true, false, true, false, false, true },
				{ true, false, false, true, true, false, false, false, false, true },
				{ true, false, false, true, true, false, false, true, true, false }
		};
	}

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(FinalCountController.class);

		when(getUserDataMock().getOperatorAreaPath().isMunicipalityLevel()).thenReturn(true);
	}

	@Test
	public void getFinalCountIndex_checkIndex() {
		when(getCountsMock().getFinalCountIndex()).thenReturn(1);

		assertThat(ctrl.getFinalCountIndex()).isEqualTo(1);
	}

	@Test
	public void updateCounts_withSameAsCurrentIndex_checkSet() {
		List<FinalCount> list = addFinalCounts(5, true, 0, true, true, false);
		when(getCountsMock().getFinalCountIndex()).thenReturn(1);
		FinalCount newCount = mock(FinalCount.class);

		ctrl.updateCounts(1, newCount);

		assertThat(list.get(1)).isSameAs(newCount);
	}

	@Test
	public void updateCounts_withOtherThanCurrentIndex_checkAdd() {
		List<FinalCount> list = addFinalCounts(5, true, 0, true, true, false);
		when(getCountsMock().getFinalCountIndex()).thenReturn(1);
		FinalCount newCount = mock(FinalCount.class);

		ctrl.updateCounts(5, newCount);

		assertThat(list.get(5)).isSameAs(newCount);
	}

	@Test
	public void isApproved_verifyCountsExecution() {
		ctrl.isApproved();

		verify(getCountsMock()).hasApprovedFinalCount();
	}

	@Test
	public void isCountEditable_withUserOnCountyLevel_returnsFalse() {
		when(getStartCountingControllerMock().isUserOnCountyLevel()).thenReturn(true);

		assertThat(ctrl.isCountEditable()).isFalse();
	}

	@Test
	public void isCountEditable_withUserOnMunicipalityLevel_returnsFalse() {
		addFinalCounts(5, true, 0, true, false, false);
		when(getUserDataMock().getOperatorAreaPath().isMunicipalityLevel()).thenReturn(true);

		assertThat(ctrl.isCountEditable()).isFalse();
	}

	@Test
	public void button_withUserOnCountyLevel_returnsNotRendered() {
		when(getStartCountingControllerMock().isUserOnCountyLevel()).thenReturn(true);

		Button button = ctrl.button(ButtonType.REGISTER_CORRECTIONS);

		assertThat(button.isRendered()).isFalse();
		assertThat(button.isDisabled()).isTrue();
	}

	@Test(dataProvider = "buttonRegisterCorrections")
	public void buttonRegisterCorrections_parameterized_checkRenderedDisabled(
			boolean hasAccess, boolean hasCorrections, boolean isContestOnCountyLevel,
			boolean isManualCount, boolean isEditable, boolean isProcessed, boolean isApproved, boolean isPreviousApproved,
			boolean isRendered, boolean isDisabled) {

		addFinalCounts(5, true, hasCorrections ? 1 : 0, isManualCount, isEditable, isProcessed);

		hasAccess(Opptelling_Rettelser_Rediger);
		whenIsContestOnCountyLevel(isContestOnCountyLevel);
		whenIsApproved(isApproved);
		whenIsPreviousApproved(isPreviousApproved);

		Button button = ctrl.button(ButtonType.REGISTER_CORRECTIONS);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@Test(dataProvider = "buttonModifiedBallotProcessed")
	public void buttonModifiedBallotProcessed_parameterized_checkRenderedDisabled(
			boolean hasAccess, boolean hasCorrections, boolean isContestOnCountyLevel,
			boolean isManualCount, boolean isEditable, boolean isProcessed, boolean isApproved, boolean isPreviousApproved,
			boolean isRendered, boolean isDisabled) {

		addFinalCounts(5, true, hasCorrections ? 1 : 0, isManualCount, isEditable, isProcessed);

		hasAccess(Opptelling_Forhånd_Rediger);
		whenIsContestOnCountyLevel(isContestOnCountyLevel);
		whenIsApproved(isApproved);
		whenIsPreviousApproved(isPreviousApproved);

		/*
		if (!hasWriteAccess() || (hasCorrections() && !startCountingController.isContestOnCountyLevel())) {
				return notRendered();
			}
			if (count.isEditable() && !count.isModifiedBallotsProcessed() && !isApproved() && isPreviousApproved()) {
				return rendered(true);
			}
			return notRendered();
		 */

		Button button = ctrl.button(ButtonType.MODIFIED_BALLOT_PROCESSED);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	private void whenIsPreviousApproved(boolean isPreviousApproved) {
		setupTabs(5, 2, isPreviousApproved);
	}

	private void whenIsContestOnCountyLevel(boolean isContestOnCountyLevel) {
		when(getStartCountingControllerMock().isContestOnCountyLevel()).thenReturn(isContestOnCountyLevel);
	}

	private void whenIsApproved(boolean isApproved) {
		when(getCountsMock().hasApprovedFinalCount()).thenReturn(isApproved);
	}

	private void setupTabs(int count, int tabIndex, boolean approved) {
		List<Tab> tabs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Tab tab = mock(Tab.class, RETURNS_DEEP_STUBS);
			CountController ctrl = mock(CountController.class, RETURNS_DEEP_STUBS);
			when(ctrl.isApproved()).thenReturn(approved);
			when(tab.getController()).thenReturn(ctrl);
			tabs.add(tab);
		}
		ctrl.setTabIndex(tabIndex);
		when(getStartCountingControllerMock().getTabs()).thenReturn(tabs);
	}

	private List<FinalCount> addFinalCounts(
			int count, boolean hasModifiedBallotBatchForBallotCountPks, int modifiedCount, boolean isManualCount, boolean isEditable,
			boolean isModifiedBallotsProcessed) {
		List<FinalCount> counts = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			FinalCount finalCount = mock(FinalCount.class, RETURNS_DEEP_STUBS);
			BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
			when(ballotCount.getModifiedCount()).thenReturn(modifiedCount);
			when(finalCount.getBallotCounts()).thenReturn(Collections.singletonList(ballotCount));
			when(finalCount.isManualCount()).thenReturn(isManualCount);
			when(finalCount.isEditable()).thenReturn(isEditable);
			when(finalCount.isModifiedBallotsProcessed()).thenReturn(isModifiedBallotsProcessed);
			counts.add(finalCount);
		}
		when(getCountsMock().getFinalCounts()).thenReturn(counts);
		when(getModifiedBallotBatchServiceMock().hasModifiedBallotBatchForBallotCountPks(eq(getUserDataMock()), anyList()))
			.thenReturn(hasModifiedBallotBatchForBallotCountPks);
		when(getCountsMock().getFinalCountIndex()).thenReturn(count - 1);
		return counts;
	}

}

