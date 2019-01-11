package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.exception.ErrorCode;
import no.evote.security.UserData;
import no.evote.service.configuration.ProposerService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import org.primefaces.event.ReorderEvent;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ProposerControllerTest extends BaseFrontendTest {

	@Test
	public void onRowReorder_withFullReorder_fullSwap() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);
		ReorderEvent reorderEventStub = onRowReorderSetup(ctrl, 0, 4, 5);

		ctrl.onRowReorder(reorderEventStub);

		List<Proposer> result = ctrl.getProposerList();
		assertThat(result.size()).isEqualTo(5);
		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getFirstName()).isEqualTo("out" + i);
		}
	}

	@Test
	public void onRowReorder_withPartialReorder_partialSwap() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);
		ReorderEvent reorderEventStub = onRowReorderSetup(ctrl, 2, 3, 2);

		ctrl.onRowReorder(reorderEventStub);

		List<Proposer> result = ctrl.getProposerList();
		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getFirstName()).isEqualTo("in0");
		assertThat(result.get(1).getFirstName()).isEqualTo("in1");
		assertThat(result.get(2).getFirstName()).isEqualTo("out0");
		assertThat(result.get(3).getFirstName()).isEqualTo("out1");
		assertThat(result.get(4).getFirstName()).isEqualTo("in4");
	}

	@Test
	public void saveProposer_withInvalidProposer_verifyValidationMessages() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);
		Proposer proposer = stub_validate(false);
		mockFieldValue("proposerForEdit", proposer);

		ctrl.saveProposer();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "invalid");
	}

	@Test
	public void saveProposer_withUpdatedProposer_verifyMessageAndCloseDialog() throws Exception {
		ProposerController ctrl = initializeMocks(new ThisProposerController());
		Proposer proposer = stub_validate(true);
		when(proposer.isCreated()).thenReturn(true);
		mockFieldValue("proposerForEdit", proposer);
		mockFieldValue("proposerList", mockList(2, Proposer.class));

		ctrl.saveProposer();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@listProposal.candidate.updated, ProposerMock]");
		verify_closeAndUpdate(ctrl.getEditProposerDialog(),
				"editListProposalForm:msg", "editListProposalForm:tabs:proposerDataTable");
	}

	@Test
	public void saveProposer_withNewProposer_verifyMessageAndCloseDialog() throws Exception {
		ProposerController ctrl = initializeMocks(new ThisProposerController());
		Proposer proposer = stub_validate(true);
		when(proposer.isCreated()).thenReturn(false);
		when(proposer.isIdSet()).thenReturn(false);
		mockFieldValue("proposerForEdit", proposer);
		mockFieldValue("proposerList", mockList(2, Proposer.class));
		mock_setMockIdForEmptyId(proposer);

		ctrl.saveProposer();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@listProposal.candidate.created, ProposerMock]");
		verify_closeAndUpdate(ctrl.getEditProposerDialog(),
				"editListProposalForm:msg", "editListProposalForm:tabs:proposerDataTable");
	}

	@Test
	public void deleteProposer_withProposer_verifyBackendDeleteAndCloseDialog() throws Exception {
		ProposerController ctrl = initializeMocks(new ThisProposerController());
		Proposer proposer = stub_validate(true);
		mockFieldValue("proposerForEdit", proposer);

		ctrl.deleteProposer();

		verify(getInjectMock(ProposerService.class)).deleteAndReorder(eq(getUserDataMock()), any(Proposer.class), anyLong());
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.list.delete, 1, ProposerMock]");
		verify_closeAndUpdate(ctrl.getConfirmDeleteProposer(),
				"editListProposalForm:msg", "editListProposalForm:tabs:proposerDataTable");
	}

	@Test
	public void editProposer_withProposer_verifySearchModeAndOpenDialog() throws Exception {
		ProposerController ctrl = initializeMocks(new ThisProposerController());
		Proposer proposer = createMock(Proposer.class);

		ctrl.editProposer(proposer);

		assertThat(ctrl.getProposerForEdit()).isSameAs(proposer);
		assertThat(ctrl.isSearchMode()).isFalse();
		verify_open(ctrl.getEditProposerDialog());
	}

	@Test
	public void promptDeleteProposer_withProposer_verifySearchModeAndOpenDialog() throws Exception {
		ProposerController ctrl = initializeMocks(new ThisProposerController());
		Proposer proposer = createMock(Proposer.class);

		ctrl.promptDeleteProposer(proposer);

		assertThat(ctrl.getProposerForEdit()).isSameAs(proposer);
		verify_open(ctrl.getConfirmDeleteProposer());
	}

	@Test
	public void createNewProposer_withProposer_verifyDisplayOrderSetAndOpenDialog() throws Exception {
		ProposerController ctrl = initializeMocks(new ThisProposerController());
		Proposer proposer = createMock(Proposer.class);
		when(getInjectMock(ProposerService.class).createNewProposer(eq(getUserDataMock()), any(Ballot.class))).thenReturn(proposer);
		mockFieldValue("proposerList", new ArrayList<>());

		ctrl.createNewProposer();

		verify(proposer).setDisplayOrder(1);
		verify_open(ctrl.getEditProposerDialog());
	}

	@Test
	public void createProposerFromVoter_withVoter_convertsVoterToProposer() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);
		Voter voter = mockField("selectedVoterResult", Voter.class);

		ctrl.createProposerFromVoter();

		assertThat(ctrl.getSelectedVoterResult()).isNull();
		assertThat(ctrl.isSearchMode()).isFalse();
        verify(getInjectMock(ProposerService.class)).convertVoterToProposer(any(UserData.class), any(), eq(voter));
	}

	@Test
	public void searchForProposerInElectoralRoll_withProposer_verifyState() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);
		Proposer proposer = createMock(Proposer.class);
		stub_searchVoter(5);

		ctrl.searchForProposerInElectoralRoll(proposer);

		assertThat(ctrl.getProposerForEdit()).isSameAs(proposer);
		assertThat(ctrl.getVoterResult()).hasSize(5);
		assertThat(ctrl.isMoreHitsThanDisplayed()).isFalse();
		assertThat(ctrl.isSearchMode()).isTrue();
	}

	@Test
	public void cancelSearchForProposerInElectoralRoll_setsSearchModeToFalse() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);
		mockFieldValue("searchMode", true);

		ctrl.cancelSearchForProposerInElectoralRoll();

		assertThat(ctrl.isSearchMode()).isFalse();
	}

	@Test
	public void onError_withOptimisticLockError_verifyDataReloadAndReturnMessage() throws Exception {
		ProposerController ctrl = initializeMocks(ProposerController.class);

		String result = ctrl.onError(ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK);

		assertThat(result).isEqualTo("@listProposal.save.optimisticLockingException");
		verify_updateProposerListFromDb();
	}

    private ReorderEvent onRowReorderSetup(ProposerController ctrl, int from, int to, int out) {
		ReorderEvent reorderEventStub = mock(ReorderEvent.class);
		when(getInjectMock(RedigerListeforslagController.class).getAffiliation()).thenReturn(null);
		ctrl.setProposerList(getProposerList("in", 5));
		when(reorderEventStub.getFromIndex()).thenReturn(from);
		when(reorderEventStub.getToIndex()).thenReturn(to);
		when(getInjectMock(ProposerService.class).changeDisplayOrder(any(UserData.class), any(Proposer.class), anyInt(), anyInt())).thenReturn(
				getProposerList("out", out));
		return reorderEventStub;
	}

	private List<Proposer> getProposerList(String name, int num) {
		List<Proposer> result = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			result.add(new Proposer());
			result.get(i).setDisplayOrder(i + 1);
			result.get(i).setFirstName(name + i);
		}
		return result;
	}

	private void verify_updateProposerListFromDb() {
		verify(getInjectMock(ProposerService.class)).findByBallot(eq(getUserDataMock()), anyLong());
	}

	private Proposer stub_validate(boolean valid) {
		Proposer proposer = createMock(Proposer.class);
		when(proposer.isInvalid()).thenReturn(!valid);
		when(proposer.getValidationMessageList()).thenReturn(Collections.singletonList(new UserMessage("invalid")));
		when(proposer.getDisplayOrder()).thenReturn(1);
		when(proposer.toString()).thenReturn("ProposerMock");
		when(getInjectMock(ProposerService.class).validate(eq(getUserDataMock()), any(Proposer.class), anyLong())).thenReturn(proposer);
		return proposer;
	}

	private void mock_setMockIdForEmptyId(Proposer proposer) {
        when(getInjectMock(ProposerService.class).setMockIdForEmptyId(eq(getUserDataMock()), any(Proposer.class), anyLong(), any())).thenReturn(proposer);
	}

	private void stub_searchVoter(int size) {
        when(getInjectMock(ProposerService.class).searchVoter(eq(getUserDataMock()), any(Proposer.class), any(), anySet()))
				.thenReturn(mockList(size, Voter.class));
	}

	private class ThisProposerController extends ProposerController {

		private Dialog editProposerDialog;
		private Dialog confirmDeleteProposer;

		public ThisProposerController() {
			this.editProposerDialog = createMock(Dialog.class);
			this.confirmDeleteProposer = createMock(Dialog.class);
		}

		@Override
		public Dialog getEditProposerDialog() {
			return editProposerDialog;
		}

		@Override
		public Dialog getConfirmDeleteProposer() {
			return confirmDeleteProposer;
		}

		//
	}
}

