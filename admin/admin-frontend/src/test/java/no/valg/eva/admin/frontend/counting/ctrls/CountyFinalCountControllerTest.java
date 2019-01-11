package no.valg.eva.admin.frontend.counting.ctrls;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.frontend.counting.view.Tab;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CountyFinalCountControllerTest extends BaseCountControllerTest {

	private CountyFinalCountController ctrl;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(CountyFinalCountController.class);
	}

	@Test
    public void getFinalCountIndex_checkIndex() {
		when(getCountsMock().getCountyFinalCountIndex()).thenReturn(1);

		assertThat(ctrl.getFinalCountIndex()).isEqualTo(1);
	}

	@Test
    public void updateCounts_withSameAsCurrentIndex_checkSet() {
		List<FinalCount> list = addFinalCounts(5);
		when(getCountsMock().getCountyFinalCountIndex()).thenReturn(1);
		FinalCount newCount = mock(FinalCount.class);

		ctrl.updateCounts(1, newCount);

		assertThat(list.get(1)).isSameAs(newCount);
	}

	@Test
    public void updateCounts_withOtherThanCurrentIndex_checkAdd() {
		List<FinalCount> list = addFinalCounts(5);
		when(getCountsMock().getCountyFinalCountIndex()).thenReturn(1);
		FinalCount newCount = mock(FinalCount.class);

		ctrl.updateCounts(5, newCount);

		assertThat(list.get(5)).isSameAs(newCount);
	}

	@Test
    public void getBlankBallotCountDifferenceFromPreviousCount_verifyCountsExecution() {
		addFinalCounts(1);
		ctrl.getBlankBallotCountDifferenceFromPreviousCount();

        verify(getCountsMock()).getCountyBlankBallotCountDifference(any());
	}

	@Test
    public void getOrdinaryBallotCountDifferenceFromPreviousCount_verifyCountsExecution() {
		addFinalCounts(1);
		ctrl.getOrdinaryBallotCountDifferenceFromPreviousCount();

        verify(getCountsMock()).getCountyOrdinaryBallotCountDifference(any());
	}

	@Test
    public void getQuestionableBallotCountDifferenceFromPreviousCount_verifyCountsExecution() {
		addFinalCounts(1);
		ctrl.getQuestionableBallotCountDifferenceFromPreviousCount();

        verify(getCountsMock()).getCountyQuestionableBallotCountDifference(any());
	}

	@Test
    public void getTotalBallotCountDifferenceFromPreviousCount_verifyCountsExecution() {
		addFinalCounts(1);
		ctrl.getTotalBallotCountDifferenceFromPreviousCount();

        verify(getCountsMock()).getCountyTotalBallotCountDifference(any());
	}

	@Test
    public void isApproved_verifyCountsExecution() {
		ctrl.isApproved();

		verify(getCountsMock()).hasApprovedCountyFinalCount();
	}

	@Test
    public void isCountEditable_givenNonEditableCounts_isNotEditable() {
		addFinalCounts(1, false);
		assertThat(ctrl.isCountEditable()).isFalse();
	}

	@Test
    public void isCountEditable_givenPreviousControlerNotInstanceofFinalCountController_isEditable() {
		addFinalCounts(1, true);
		assertThat(ctrl.isCountEditable()).isTrue();
	}

	@Test
    public void isCountEditable_givenProcessedRejectedBallots_isEditable() {
		addFinalCounts(1, true);
		setupTabs();
		assertThat(ctrl.isCountEditable()).isTrue();
	}

	private List<FinalCount> addFinalCounts(int count) {
		return addFinalCounts(count, true);
	}

	private List<FinalCount> addFinalCounts(int count, boolean isEditable) {
		FinalCount finalCountMock = mock(FinalCount.class, RETURNS_DEEP_STUBS);
		when(finalCountMock.isEditable()).thenReturn(isEditable);
		when(finalCountMock.isRejectedBallotsProcessed()).thenReturn(true);
		List<FinalCount> counts = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			counts.add(finalCountMock);
		}
		when(getCountsMock().getCountyFinalCounts()).thenReturn(counts);
		when(getModifiedBallotBatchServiceMock().hasModifiedBallotBatchForBallotCountPks(eq(getUserDataMock()), anyList())).thenReturn(true);
		when(getCountsMock().getCountyFinalCountIndex()).thenReturn(count - 1);
		return counts;
	}

	private void setupTabs() {
		List<Tab> tabs = new ArrayList<>();
		setupTab(tabs, true);
		setupTab(tabs, false);
		ctrl.setTabIndex(1);
		when(getStartCountingControllerMock().getTabs()).thenReturn(tabs);
	}

	private void setupTab(List<Tab> tabs, boolean approved) {
		Tab tab = mock(Tab.class, RETURNS_DEEP_STUBS);
		FinalCountController finalCountControllerMock = mock(FinalCountController.class, RETURNS_DEEP_STUBS);
		when(finalCountControllerMock.isApproved()).thenReturn(approved);
		when(tab.getController()).thenReturn(finalCountControllerMock);
		FinalCount finalCountMock = mock(FinalCount.class, RETURNS_DEEP_STUBS);
		when(finalCountMock.isRejectedBallotsProcessed()).thenReturn(true);
		when(finalCountControllerMock.getFinalCount()).thenReturn(finalCountMock);
		tabs.add(tab);
	}

}

