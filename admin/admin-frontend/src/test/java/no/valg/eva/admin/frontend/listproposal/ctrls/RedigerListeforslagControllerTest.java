package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.dto.ListProposalValidationData;
import no.evote.exception.ErrorCode;
import no.evote.service.configuration.AffiliationService;
import no.evote.service.configuration.BallotService;
import no.evote.service.configuration.LegacyListProposalService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedigerListeforslagControllerTest extends BaseFrontendTest {

	@Test
	public void saveColumn_verifyDialogCloseAndDOMUpdate() throws Exception {
		RedigerListeforslagController ctrl = initializeMocks(new ThisRedigerListeforslagController());

		ctrl.saveColumn();

		verify_closeAndUpdate(ctrl.getShowCandidateProfessionResidenceDialog(),
				"editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void setStatusApproved_withValidationApproved_verifyUpdateBallotStatusAndMessageAddedAndUpdated() throws Exception {
		RedigerListeforslagController ctrl = initializeMocks(new ThisRedigerListeforslagController());
		mockListProposalData(true);
		stub_validateNumberOfCandidatesAndProposers(true, 1, true);

		ctrl.setStatusApproved();

		verify(getInjectMock(BallotService.class)).updateBallotStatus(eq(getUserDataMock()), any(Affiliation.class), any(BallotStatus.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@listProposal.status.new.updated");
		verify(getRequestContextMock()).update(Arrays.asList("electionMeta", "editListProposalForm"));
	}

	@Test
	public void setStatusApproved_withValidationError_returnsErrorMessageAndUpdatesClientMsgComponent() throws Exception {
		RedigerListeforslagController ctrl = initializeMocks(new ThisRedigerListeforslagController());
		mockListProposalData(true);
		stub_validateNumberOfCandidatesAndProposers(false, 1, false);

		ctrl.setStatusApproved();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "invalid");
		verify(getRequestContextMock()).update("editListProposalForm:msg");
	}

	@Test
	public void setStatusApproved_withValidationErrorAndOverrideValidation_opensConfirmDialog() throws Exception {
		RedigerListeforslagController ctrl = initializeMocks(new ThisRedigerListeforslagController());
		mockListProposalData(true);
		stub_validateNumberOfCandidatesAndProposers(false, 2, true);

		ctrl.setStatusApproved();

		verify(getRequestContextMock()).execute("PF('confirmApproveListProposal').show()");
	}

	@Test
	public void setStatusApprovedFromDialog_verifyCloseDialogWithMessage() throws Exception {
		RedigerListeforslagController ctrl = initializeMocks(new ThisRedigerListeforslagController());
		mockListProposalData(true);

		ctrl.setStatusApprovedFromDialog();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@listProposal.status.new.updated");
		verify_closeAndUpdate(Dialogs.CONFIRM_APPROVE_LIST_PROPOSAL.getId(),
				"electionMeta", "editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void onError_withStaleObjectError_verifyMessageAndDataReload() throws Exception {
		RedigerListeforslagController ctrl = initializeMocks(new ThisRedigerListeforslagController());
		mockListProposalData(true);

		String result = ctrl.onError(ErrorCode.ERROR_CODE_0504_STALE_OBJECT);

		assertThat(result).isEqualTo("@listProposal.save.optimisticLockingException");
		verify_updateAffiliation();
		verify_updateProposalPersons();
	}

	private void verify_updateProposalPersons() {
		verify(getInjectMock(CandidateController.class)).updateCandidateListFromDb();
		verify(getInjectMock(ProposerController.class)).updateProposerListFromDb();
	}

	private void verify_updateAffiliation() {
		verify(getInjectMock(AffiliationService.class)).findByPk(eq(getUserDataMock()), anyLong());
	}

	private void mockListProposalData(boolean approved) throws Exception {
		Affiliation affiliation = createMock(Affiliation.class);
		mockFieldValue("affiliation", affiliation);
		when(affiliation.getParty().isApproved()).thenReturn(approved);
	}

	private void stub_validateNumberOfCandidatesAndProposers(boolean approved, int withSignOnlySize, boolean readyForApproval) {
		ListProposalValidationData data = createMock(ListProposalValidationData.class);
		when(data.isApproved()).thenReturn(approved);
		when(data.isSufficientNumberOfCandidates()).thenReturn(true);
		when(data.getProposerListMedKunUtfylteUnderskrifter().size()).thenReturn(withSignOnlySize);
		when(data.getAffiliation().getValidationMessageList()).thenReturn(Collections.singletonList(new String[] { "invalid" }));
		when(
				getInjectMock(LegacyListProposalService.class).validateCandidatesAndNumberOfCandidatesAndProposers(eq(getUserDataMock()),
						any(ListProposalValidationData.class)))
								.thenReturn(data);
		when(data.isPartyReadyForApproval()).thenReturn(readyForApproval);
	}

	private class ThisRedigerListeforslagController extends RedigerListeforslagController {

		private Dialog showCandidateProfessionResidenceDialog;

		public ThisRedigerListeforslagController() {
			showCandidateProfessionResidenceDialog = createMock(Dialog.class);
		}

		@Override
		public Dialog getShowCandidateProfessionResidenceDialog() {
			return showCandidateProfessionResidenceDialog;
		}
	}

}
