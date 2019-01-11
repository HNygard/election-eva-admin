package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.common.counting.model.modifiedballots.RegisterModifiedBallotCountStatus;
import no.valg.eva.admin.common.counting.service.CountingService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.Instance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ModifiedBallotsStatusControllerTest extends BaseCountControllerTest {

	private ModifiedBallotsStatusController controller;
	private FinalCount finalCountMock;

	@DataProvider(name = "getBreadCrumbText")
	public static Object[][] getBreadCrumbText() {
		return new Object[][] {
				{ ModifiedBallotsStatusController.Referrer.COUNTING, "@count.tab.type[E]" },
				{ ModifiedBallotsStatusController.Referrer.APPROVE_MANUAL_REJECTED_COUNT, "@menu.counting.approve_rejected.manual" },
				{ ModifiedBallotsStatusController.Referrer.APPROVE_SCANNED_REJECTED_COUNT, "@menu.counting.approve_rejected.scan" }
		};
	}

	@DataProvider(name = "getBreadCrumbAction")
	public static Object[][] getBreadCrumbAction() {
		return new Object[][] {
				{ ModifiedBallotsStatusController.Referrer.COUNTING, "/secure/counting/counting.xhtml?cid=1" },
				{ ModifiedBallotsStatusController.Referrer.APPROVE_MANUAL_REJECTED_COUNT,
						"/secure/counting/approveManualRejectedCount.xhtml?category=BF&contestPath=111111"
								+ ".22.33.444444&areaPath=1234" },
				{ ModifiedBallotsStatusController.Referrer.APPROVE_SCANNED_REJECTED_COUNT, "/secure/counting/approveScannedRejectedCount"
						+ ".xhtml?category=BF&contestPath=111111.22.33.444444&areaPath=1234" }
		};
	}

	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(ModifiedBallotsStatusController.class);
		finalCountMock = mock(FinalCount.class);
	}

	@Test
    public void getRegisterModifiedBallotStatus_always_returnsModifiedNumberOfModifiedBallots() {
		setupControllerToReturnModifiedBallots(buildModifiedBallotCountsWithUnfinishedWork());
		RegisterModifiedBallotCountStatus registerModifiedBallotCountStatus = controller.getRegisterModifiedBallotCountStatus();

		assertThat(registerModifiedBallotCountStatus.getModifiedBallotsStatusList().size()).isEqualTo(2);
		assertThat(registerModifiedBallotCountStatus.getModifiedBallotsStatusList().get(0).getTotal()).isEqualTo(0);
		assertThat(registerModifiedBallotCountStatus.getModifiedBallotsStatusList().get(1).getTotal()).isEqualTo(10);
	}

	@Test
    public void isRegistrationOfAllModifiedBallotsCompleted_whenAllModifiedBallotsAreHandled_returnsTrue() {
		setupControllerToReturnModifiedBallots(buildModifiedBallotCountsAllFinished());

		boolean result = controller.getRegisterModifiedBallotCountStatus().isRegistrationOfAllModifiedBallotsCompleted();

		assertThat(result).isEqualTo(true);
	}

	@Test
    public void isRegistrationOfAllModifiedBallotsCompleted_whenThereAreUnhandledBallots_returnsFalse() {
		setupControllerToReturnModifiedBallots(buildModifiedBallotCountsWithUnfinishedWork());

		boolean result = controller.getRegisterModifiedBallotCountStatus().isRegistrationOfAllModifiedBallotsCompleted();

		assertThat(result).isEqualTo(false);
	}

	@Test
    public void isRegistrationOfAllModifiedBallotsCompleted_whenNoStatusHasBeenRegistered_returnsTrue() {
        setupControllerToReturnModifiedBallots(Collections.emptyList());

		boolean result = controller.getRegisterModifiedBallotCountStatus().isRegistrationOfAllModifiedBallotsCompleted();

		assertThat(result).isEqualTo(true);
	}

	@Test
    public void canShowCreateBatchLink_whenThereAreUnfinishedModifiedBallots() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = buildModifiedBallotCountsWithUnfinishedWork();
		setupControllerToReturnModifiedBallots(modifiedBallotsStatusList);
		assertThat(controller.showCreateBatchLink(modifiedBallotsStatusList.get(1))).isTrue();
	}

	@Test
    public void canNotShowCreateBatchLink_whenThereAreUnfinishedModifiedBallotsAndAnOngoingBatch() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = buildModifiedBallotCountsWithUnfinishedWork("100_10_30");
		setupControllerToReturnModifiedBallots(modifiedBallotsStatusList);
		assertThat(controller.showCreateBatchLink(modifiedBallotsStatusList.get(1))).isFalse();
	}

	@Test
    public void canNotShowCreateBatchLink_whenThereAreActuallyNoModifiedBallots() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = buildModifiedBallotCountsWithUnfinishedWork();
		setupControllerToReturnModifiedBallots(modifiedBallotsStatusList);
		assertThat(controller.showCreateBatchLink(modifiedBallotsStatusList.get(0))).isFalse();
	}

	@Test
    public void canNotShowGotoReviewLink_whenThereAreUnfinishedModifiedBallots() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = buildModifiedBallotCountsWithUnfinishedWork();
		setupControllerToReturnModifiedBallots(modifiedBallotsStatusList);
		assertThat(controller.showGotoReviewLink(modifiedBallotsStatusList.get(0))).isFalse();
		assertThat(controller.showGotoReviewLink(modifiedBallotsStatusList.get(1))).isFalse();
	}

	@Test
    public void canNotShowCreateBatchLink_whenAllModifiedBallotsAreDone() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = buildModifiedBallotCountsAllFinished();
		setupControllerToReturnModifiedBallots(modifiedBallotsStatusList);
		assertThat(controller.showCreateBatchLink(modifiedBallotsStatusList.get(0))).isFalse();
		assertThat(controller.showCreateBatchLink(modifiedBallotsStatusList.get(1))).isFalse();
	}

	@Test
    public void canNotShowGotoReviewLink_whenAllModifiedBallotsAreDone() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = buildModifiedBallotCountsAllFinished();
		setupControllerToReturnModifiedBallots(modifiedBallotsStatusList);
		assertThat(controller.showGotoReviewLink(modifiedBallotsStatusList.get(0))).isTrue();
		assertThat(controller.showGotoReviewLink(modifiedBallotsStatusList.get(1))).isTrue();
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Registration of modified ballots is not completed!")
	public void registrationCompleted_withNotCompletedStatus_throwsIllegalStateException() throws Exception {
		RegisterModifiedBallotCountStatus mock = mockField("registerModifiedBallotCountStatus", RegisterModifiedBallotCountStatus.class);
		when(mock.isRegistrationOfAllModifiedBallotsCompleted()).thenReturn(false);

		controller.registrationCompleted();
	}

	@Test
	public void registrationCompleted_withCompletedStatus_verifyRedirect() throws Exception {
		RegisterModifiedBallotCountStatus registerModifiedBallotCountStatusMock = mockField("registerModifiedBallotCountStatus",
				RegisterModifiedBallotCountStatus.class);
		when(registerModifiedBallotCountStatusMock.isRegistrationOfAllModifiedBallotsCompleted()).thenReturn(true);
		mockFieldValue("referrer", ModifiedBallotsStatusController.Referrer.COUNTING);
		mockControllerInstances();
		FinalCount finalCountMock = mockFinalCount();

		String path = controller.registrationCompleted();

		assertThat(path).isEqualTo("/secure/counting/counting.xhtml?faces-redirect=true");
		verify(finalCountMock).setModifiedBallotsProcessed(true);
		verify(getInjectMock(CountingService.class)).saveCount(eq(getUserDataMock()), any(CountContext.class), eq(finalCountMock)
		);
	}

	@Test
	public void registrationCompleted_withCompletedStatusAndSsModifiedBallotsProcessed_verifyNoSaveAndRedirect() throws Exception {
		RegisterModifiedBallotCountStatus registerModifiedBallotCountStatusMock = mockField("registerModifiedBallotCountStatus",
			RegisterModifiedBallotCountStatus.class);
		when(registerModifiedBallotCountStatusMock.isRegistrationOfAllModifiedBallotsCompleted()).thenReturn(true);
		mockFieldValue("referrer", ModifiedBallotsStatusController.Referrer.COUNTING);
		mockControllerInstances();
		FinalCount finalCountMock = mockFinalCount();
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(true);

		String path = controller.registrationCompleted();

		assertThat(path).isEqualTo("/secure/counting/counting.xhtml?faces-redirect=true");
		verify(finalCountMock, never()).setModifiedBallotsProcessed(true);
		verify(getInjectMock(CountingService.class), never()).saveCount(eq(getUserDataMock()), any(CountContext.class), eq(finalCountMock)
		);
	}

	@Test(dataProvider = "getBreadCrumbText")
	public void getBreadCrumbText_withDataProvider_verifyExpected(ModifiedBallotsStatusController.Referrer referrer, String expected) throws Exception {
		mockFieldValue("referrer", referrer);

		assertThat(controller.getBreadCrumbText()).isEqualTo(expected);
	}

	@Test(dataProvider = "getBreadCrumbAction")
	public void getBreadCrumbAction_withDataProvider_verifyExpected(ModifiedBallotsStatusController.Referrer referrer, String expected) throws Exception {
		mockFieldValue("referrer", referrer);
		mockControllerInstances();
		mockCid();
		stubCountContext();
		FinalCount finalCountMock = mockFinalCount();
		when(finalCountMock.getAreaPath().toString()).thenReturn("1234");

		assertThat(controller.getBreadCrumbAction()).isEqualTo(expected);
	}

	@Test
	public void doRenderCountingBreadCrumb_withCountingReferrer_returnsTrue() throws Exception {
		mockFieldValue("referrer", ModifiedBallotsStatusController.Referrer.COUNTING);

		assertThat(controller.doRenderCountingBreadCrumb()).isTrue();
	}

	private List<ModifiedBallotsStatus> buildModifiedBallotCountsWithUnfinishedWork() {
		return buildModifiedBallotCountsWithUnfinishedWork(null);
	}

	private List<ModifiedBallotsStatus> buildModifiedBallotCountsWithUnfinishedWork(String incompleteBatchId) {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = new ArrayList<>();
		modifiedBallotsStatusList.add(buildModifiedBallotsStatus(0, 0, 0, incompleteBatchId));
		modifiedBallotsStatusList.add(buildModifiedBallotsStatus(10, 0, 0));
		return modifiedBallotsStatusList;
	}

	private List<ModifiedBallotsStatus> buildModifiedBallotCountsAllFinished() {
		List<ModifiedBallotsStatus> modifiedBallotsStatusList = new ArrayList<>();
		modifiedBallotsStatusList.add(buildModifiedBallotsStatus(40, 0, 40));
		modifiedBallotsStatusList.add(buildModifiedBallotsStatus(30, 0, 30));
		modifiedBallotsStatusList.add(buildModifiedBallotsStatus(20, 0, 20));
		modifiedBallotsStatusList.add(buildModifiedBallotsStatus(10, 0, 10));
		return modifiedBallotsStatusList;
	}

	private ModifiedBallotsStatus buildModifiedBallotsStatus(int total, int inProgress, int completed) {
		return buildModifiedBallotsStatus(total, inProgress, completed, null);
	}

	private ModifiedBallotsStatus buildModifiedBallotsStatus(int total, int inProgress, int completed, String incompleteBatchId) {
		return new ModifiedBallotsStatus(new BallotCount(), total, inProgress, completed, incompleteBatchId);
	}

    private void setupControllerToReturnModifiedBallots(List<ModifiedBallotsStatus> modifiedBallotsStatusList) {
        when(getModifiedBallotBatchServiceMock().buildModifiedBallotStatuses(any(UserData.class), eq(finalCountMock), any()))
				.thenReturn(
				modifiedBallotsStatusList);
		Instance<FinalCountController> instance = mockInstance("finalCountControllerInstance", FinalCountController.class);
		when(instance.get().getFinalCount()).thenReturn(finalCountMock);
		controller.preRender(null);
	}

	private void mockControllerInstances() {
		mockInstance("startCountingControllerInstance", StartCountingController.class);
		mockInstance("finalCountControllerInstance", FinalCountController.class);
		mockInstance("countyFinalCountControllerInstance", CountyFinalCountController.class);
		mockInstance("approveManualRejectedCountControllerInstance", ApproveManualRejectedCountController.class);
		mockInstance("approveScannedRejectedCountControllerInstance", ApproveScannedRejectedCountController.class);
	}

	private FinalCount mockFinalCount() throws Exception {
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(getMockedInstance("finalCountControllerInstance", FinalCountController.class).getFinalCount()).thenReturn(finalCountMock);
		when(getMockedInstance("countyFinalCountControllerInstance", CountyFinalCountController.class).getFinalCount()).thenReturn(finalCountMock);
		when(getMockedInstance("approveManualRejectedCountControllerInstance", ApproveManualRejectedCountController.class).getFinalCount()).thenReturn(
				finalCountMock);
		when(getMockedInstance("approveScannedRejectedCountControllerInstance", ApproveScannedRejectedCountController.class).getFinalCount()).thenReturn(
				finalCountMock);
		return finalCountMock;
	}

	private CountContext stubCountContext() throws Exception {
		CountContext countContext = new CountContext(ElectionPath.from("111111.22.33.444444"), CountCategory.BF);
		when(getMockedInstance("finalCountControllerInstance", FinalCountController.class).getContext()).thenReturn(countContext);
		when(getMockedInstance("countyFinalCountControllerInstance", CountyFinalCountController.class).getContext()).thenReturn(countContext);
		when(getMockedInstance("approveManualRejectedCountControllerInstance", ApproveManualRejectedCountController.class).getCountContext()).thenReturn(
				countContext);
		when(getMockedInstance("approveScannedRejectedCountControllerInstance", ApproveScannedRejectedCountController.class).getCountContext()).thenReturn(
				countContext);
		return countContext;
	}

	private String mockCid() {
		when(getInjectMock(Conversation.class).getId()).thenReturn("1");
		return "1";
	}
}

