package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.enterprise.context.Conversation;
import javax.faces.application.FacesMessage;

import no.evote.exception.ModifiedBallotBatchCreationFailed;
import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Ballot;
import no.valg.eva.admin.common.counting.model.modifiedballots.BallotId;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class CreateModifiedBallotBatchControllerTest extends BaseCountControllerTest {

	public static final int MAX_NO_OF_WRITE_INS = 4;
	public static final String CID = "1";

	private CreateModifiedBallotBatchController controller;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(CreateModifiedBallotBatchController.class);
		when(getInjectMock(Conversation.class).getId()).thenReturn(CID);
	}

	@Test
	public void showCreateModifiedBallotsBatchDialog_always_showsTheDialog() throws Exception {
		BallotCount ballotCount = createBallotCount();

		controller.showModifiedBallotBatchDialog(ballotCount);

		assertThat(controller.getModifiedBallotBatchSize()).isNull();
		verify_open(Dialogs.CREATE_MODIFIED_BALLOT_BATCH.getId());
		verify(getRequestContextMock()).execute("PF('createModifiedBallotBatchDialog').setTitle('@party[ballotCountId].name')");
	}

	private BallotCount createBallotCount() {
		return new BallotCount("ballotCountId", "ballotCountName", 1, 2);
	}

	@Test
	public void initiateModifiedBallotBatch_whenBatchSizeIsAcceptable_newModifiedBallotBatchIsCreatedAndUserIsForwardedToModifiedBallotRegistrationPage()
			throws Exception {
		int noOfModifiedBallotsInBatch = 20;
		BatchId modifiedBallotBatchId = new BatchId("123");
		String expectedRedirectUrl = buildRedirectUrl(modifiedBallotBatchId);
		Ballot ballot = new Ballot(new BallotId("1"), createModifiedBallotConfiguration());
		when(getInjectMock(ModifiedBallotsStatusController.class).getProcess()).thenReturn(MODIFIED_BALLOTS_PROCESS);
		when(getModifiedBallotBatchServiceMock().createModifiedBallotBatch(getUserDataMock(), null, noOfModifiedBallotsInBatch, MODIFIED_BALLOTS_PROCESS))
				.thenReturn(new ModifiedBallotBatch(modifiedBallotBatchId, Collections.<ModifiedBallot> emptyList(), ballot));
		controller.setModifiedBallotBatchSize(noOfModifiedBallotsInBatch);

		controller.initiateModifiedBallotBatch();

		verify(getFacesContextMock().getExternalContext()).redirect(expectedRedirectUrl);
	}

    private ModifiedBallotConfiguration createModifiedBallotConfiguration() {
        return new ModifiedBallotConfiguration(true, true, true, true, true, 1, 1);
    }

    private String buildRedirectUrl(BatchId modifiedBallotBatchId) {
		return ModifiedBallotUrlBuilder.from(modifiedBallotBatchId).with(getInjectMock(Conversation.class)).buildRegisterModifiedBallotBatchUrl();
	}

	@Test
	public void initiateModifiedBallotBatch_whenBatchSizeIsLargerThanTheNumberOfAvailableModifiedBallots_errorMessageIsDisplayed()
			throws Exception {
		int noOfModifiedBallotsInBatch = 20;
		when(getInjectMock(ModifiedBallotsStatusController.class).getProcess()).thenReturn(MODIFIED_BALLOTS_PROCESS);
		when(getModifiedBallotBatchServiceMock().createModifiedBallotBatch(getUserDataMock(), null, noOfModifiedBallotsInBatch, MODIFIED_BALLOTS_PROCESS))
				.thenThrow(new ModifiedBallotBatchCreationFailed(noOfModifiedBallotsInBatch, 10));

		controller.setModifiedBallotBatchSize(noOfModifiedBallotsInBatch);
		controller.initiateModifiedBallotBatch();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@count.votes.cast.batch.size.invalid, 20, 10]");
	}
}

