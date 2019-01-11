package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class RegisterModifiedBallotBatchControllerTest extends RegisterModifiedBallotsControllerTest {
    private static final BatchId BATCH_ID = new BatchId("100_1_3");
	private RegisterModifiedBallotBatchController registerModifiedBallotBatchController;

	@Override
	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch(BATCH_ID, modifiedBallots.getModifiedBallots(), ballot);
		when(getInjectMock(ModifiedBallotBatchService.class).findActiveBatchByBatchId(any(UserData.class), any(BatchId.class))).thenReturn(modifiedBallotBatch);
	}

	@Override
	protected void initializeMocksForController() throws Exception {
		super.initializeMocksForController();
		registerModifiedBallotBatchController = initializeMocks(RegisterModifiedBallotBatchController.class);
	}

	@Override
	protected ModifiedBallotsNavigationController getControllerUnderTest() {
		return registerModifiedBallotBatchController;
	}

	@Test
    public void gotoNextBallot_enablesAndDisablesButtonsCorrectly() {
		start();
		checkPreviousButtonIsDisabled();
		checkNextButtonIsEnabled();
		checkFinishedButtonIsDisabled();

		gotoNext();
		checkPreviousButtonIsEnabled();
		checkNextButtonIsEnabled();
		checkFinishedButtonIsDisabled();

		gotoNext();
		checkPreviousButtonIsEnabled();
		checkNextButtonIsDisabled();
		checkFinishedButtonIsEnabled();
	}

	@Test
    public void gotoPreviousBallot_enablesAndDisablesButtonsCorrectly() {
		start();
		gotoLastModifiedBallot();

		simulateDoneWith(modifiedBallot3);
		simulateDoneWith(modifiedBallot2);
		simulateDoneWith(modifiedBallot1);

		checkPreviousButtonIsEnabled();
		checkNextButtonIsDisabled();
		checkFinishedButtonIsEnabled();

		gotoPrevious();
		checkPreviousButtonIsEnabled();
		checkNextButtonIsEnabled();
		checkFinishedButtonIsEnabled();

		gotoPrevious();
		checkPreviousButtonIsDisabled();
		checkNextButtonIsEnabled();
		checkFinishedButtonIsEnabled();
	}

	private void simulateDoneWith(ModifiedBallot modifiedBallot) {
		modifiedBallot.setDone(true);
	}
}

