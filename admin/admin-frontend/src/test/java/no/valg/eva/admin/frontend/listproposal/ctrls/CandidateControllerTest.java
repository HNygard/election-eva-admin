package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.security.UserData;
import no.evote.service.configuration.CandidateService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.application.ResponsibilityValidationService;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import org.joda.time.LocalDate;
import org.primefaces.event.ReorderEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CandidateControllerTest extends BaseFrontendTest {

	private CandidateController ctrl;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(new ThisCandidateController());
		ctrl.init();
		when(getInjectMock(RedigerListeforslagController.class).getContest().getElection().getMaxCandidateNameLength()).thenReturn(100);
	}

	@Test
	public void onRowReorder_withFullReorder_fullSwap() {
		ReorderEvent reorderEventStub = onRowReorder_setup(ctrl, 0, 4, 5);
		ctrl.onRowReorder(reorderEventStub);

		List<Candidate> result = ctrl.getCandidateList();
		assertThat(result.size()).isEqualTo(5);
		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getFirstName()).isEqualTo("out" + i);
		}

	}

	@Test
	public void onRowReorder_withPartialReorder_partialSwap() {
		ReorderEvent reorderEventStub = onRowReorder_setup(ctrl, 2, 3, 2);
		ctrl.onRowReorder(reorderEventStub);

		List<Candidate> result = ctrl.getCandidateList();
		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getFirstName()).isEqualTo("in0");
		assertThat(result.get(1).getFirstName()).isEqualTo("in1");
		assertThat(result.get(2).getFirstName()).isEqualTo("out0");
		assertThat(result.get(3).getFirstName()).isEqualTo("out1");
		assertThat(result.get(4).getFirstName()).isEqualTo("in4");
	}

	@Test
	public void onRowReorder_withEvoteException_verifyDataReloadAndErrorMessage() {
		ReorderEvent reorderEventStub = onRowReorder_setup(ctrl, 2, 3, 2);
		when(getInjectMock(RedigerListeforslagController.class).getAffiliation()).thenReturn(createMock(Affiliation.class));
		optimisticLockExceptionWhen(CandidateService.class).changeDisplayOrder(eq(getUserDataMock()), any(Candidate.class), anyInt(),
				anyInt());

		ctrl.onRowReorder(reorderEventStub);

		verify_optimisticLockExceptionHandling();
	}

	@Test
	public void validateRoleConflicts_withNoConflicts_shouldAddInfoMessage() {
		setCandidateForEdit(100L, "Per", "", "Pettersen");

		when(getInjectMock(ResponsibilityValidationService.class)
				.checkIfCandidateHasBoardMemberOrRoleConflict(any(UserData.class), any(Candidate.class), any(Affiliation.class)))
				.thenReturn(emptyList());

		ctrl.validateRoleConflicts();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@listProposal.candidate.updated, Per Pettersen]");
		verify_closeAndUpdate(ctrl.getEditCandidateDialog(), "editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void validateRoleConflicts_withConflicts_opensWarningDialog() {
		setCandidateForEdit(100L, "Per", "", "Pettersen");
		
		when(getInjectMock(ResponsibilityValidationService.class)
                .checkIfCandidateHasBoardMemberOrRoleConflict(any(UserData.class), any(), any(Affiliation.class)))
				.thenReturn(singletonList(new ResponsibilityConflict(ResponsibilityConflictType.ROLE_POLLING_DISTRICT, "Role name", "Polling district name", "Municipality name")));

		ctrl.validateRoleConflicts();

		assertThat(ctrl.getConflicts()).isNotEmpty();

	}

	@Test
	public void saveCandidate_withExistingCandidate_shouldAddInfoMessage() {
		setCandidateForEdit(100L, "Per", "", "Pettersen");

		ctrl.saveCandidate();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@listProposal.candidate.updated, Per Pettersen]");
		verify_closeAndUpdate(ctrl.getEditCandidateDialog(), "editListProposalForm:msg",
				"editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void saveCandidate_withNewCandidate_shouldAddInfoMessage() throws Exception {
		Candidate candidate = setCandidateForEdit(null, "Per", "", "Pettersen");
        when(getInjectMock(CandidateService.class).setMockIdForEmptyId(any(UserData.class), any(Candidate.class), anyLong(), any())).thenReturn(candidate);
		mockFieldValue("currentBaseLineCount", 10);

		ctrl.saveCandidate();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@listProposal.candidate.created, Per Pettersen]");
		verify_closeAndUpdate(ctrl.getEditCandidateDialog(), "editListProposalForm:msg",
				"editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void saveCandidate_withEvoteException_shouldAddErrorMessage() {
		setCandidateForEdit(100L, "Per", "", "Pettersen");
		optimisticLockExceptionWhen(CandidateService.class).update(any(UserData.class), any(Candidate.class));

		ctrl.saveCandidate();

		verify_optimisticLockExceptionHandling();
	}

	@Test
	public void deleteCandidate_withCandidate_shouldAddInfoMessage() {
		Candidate candidate = setCandidateForEdit(100L, "Per", "", "Pettersen");

		ctrl.deleteCandidate();

		verify(getInjectMock(CandidateService.class)).deleteAndReorder(any(UserData.class), eq(candidate), anyLong());
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.list.delete, 1, Per Pettersen]");
		verify_closeAndUpdate(ctrl.getConfirmDeleteCandidateDialog(),
				"editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void deleteCandidate_withEvoteException_reloadsDataAndReturnsErrorMessage() {
		Candidate candidate = setCandidateForEdit(100L, "Per", "", "Pettersen");
		optimisticLockExceptionWhen(CandidateService.class)
				.deleteAndReorder(eq(getUserDataMock()), eq(candidate), anyLong());

		ctrl.deleteCandidate();

		verify_optimisticLockExceptionHandling();
	}

	@Test
	public void deleteAllCandidates_withCandidates_shouldAddInfoMessage() {
		setCandidateForEdit(100L, "Per", "", "Pettersen");

		ctrl.deleteAllCandidates();

		verify(getInjectMock(CandidateService.class)).deleteAll(any(UserData.class), anyList());
		verify_findByAffiliation();
		verify_closeAndUpdate(ctrl.getConfirmDeleteAllCandidatesDialog(),
				"editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
	}

	@Test
	public void deleteAllCandidates_withEvoteException_verifyDataReloadAndErrorMessage() {
		setCandidateForEdit(100L, "Per", "", "Pettersen");
		optimisticLockExceptionWhen(CandidateService.class).deleteAll(eq(getUserDataMock()), anyList());

		ctrl.deleteAllCandidates();

		verify_optimisticLockExceptionHandling();
	}

	@Test
	public void searchForCandidateInElectoralRoll_withCandidate_returnsTrueForSearchMode() {
		Candidate candidate = createMock(Candidate.class);
		stub_searchVoter();

		ctrl.searchForCandidateInElectoralRoll(candidate);

		assertThat(ctrl.isSearchMode()).isTrue();
	}

	@Test
	public void cancelSearchForCandidateInElectoralRoll_returnsFalseForSearchMode() throws Exception {
		mockFieldValue("searchMode", true);

		ctrl.cancelSearchForCandidateInElectoralRoll();

		assertThat(ctrl.isSearchMode()).isFalse();
	}

	@Test
	public void createCandidateFromVoter_withVoter_verifyConvertSearchModeAndDataReload() throws Exception {
		Voter voter = mockField("selectedVoterResult", Voter.class);

		ctrl.createCandidateFromVoter();

        verify(getInjectMock(CandidateService.class)).convertVoterToCandidate(eq(getUserDataMock()), any(), eq(voter));
		verify_findByAffiliation();
		assertThat(ctrl.isSearchMode()).isFalse();
	}

	@Test
	public void viewCandidateAudit_verifyDialogDataRetrivalAndOpenDialog() {
		ctrl.viewCandidateAudit();

		verify(getInjectMock(CandidateService.class)).getCandidateAuditByBallot(eq(getUserDataMock()), anyLong());
		verify_open(ctrl.getAuditCandidateDialog());
	}

	@Test
	public void editCandidate_withCandidate_verifyOpenDialogNotInSearchMode() {
		Candidate candidate = createMock(Candidate.class);

		ctrl.editCandidate(candidate);

		assertThat(ctrl.getCandidateForEdit()).isSameAs(candidate);
		assertThat(ctrl.isSearchMode()).isFalse();
		verify_open(ctrl.getEditCandidateDialog());
	}

	@Test
	public void promptDeleteCandidate_withCandidate_verifyOpenDialog() {
		Candidate candidate = createMock(Candidate.class);

		ctrl.promptDeleteCandidate(candidate);

		assertThat(ctrl.getCandidateForEdit()).isSameAs(candidate);
		verify_open(ctrl.getConfirmDeleteCandidateDialog());
	}

	@Test
	public void showCreateCandidateDialog_withNoCandidates_verifyDisplayOrderAndOpenDialog() throws Exception {
		Candidate candidate = createMock(Candidate.class);
		when(getInjectMock(CandidateService.class).createNewCandidate(eq(getUserDataMock()), any(Affiliation.class))).thenReturn(candidate);
		mockFieldValue("candidateList", new ArrayList<Candidate>());

		ctrl.showCreateCandidateDialog();

		assertThat(ctrl.isSearchMode()).isFalse();
		verify(candidate).setDisplayOrder(1);
		verify_open(ctrl.getEditCandidateDialog());
	}

	@Test
	public void promptDeleteAllCandidate_verifyOpenDialog() {
		ctrl.promptDeleteAllCandidate();

		verify_open(ctrl.getConfirmDeleteAllCandidatesDialog());
	}

	@Test
	public void showUploadCandidatesDialog_verifyOpenDialog() {
		ctrl.showUploadCandidatesDialog();

		verify_open(ctrl.getUploadCandidatesDialog());
	}

	@Test
	public void getFemalePercentage_withFemalesAndMales_returnsCorrectPercentage() {
		stub_findByAffiliation(getGenderCandidateList(0));

		assertThat(ctrl.getFemalePercentage()).isEqualTo(43);
		assertThat(ctrl.getMalePercentage()).isEqualTo(57);
	}

	@Test
	public void getFemalePercentage_withFemalesAndMalesAndNoId_returnsCorrectPercentage() {
		stub_findByAffiliation(getGenderCandidateList(1));

		// The remaining 12% is the one candidate without social sec num.
		assertThat(ctrl.getFemalePercentage()).isEqualTo(38);
		assertThat(ctrl.getMalePercentage()).isEqualTo(50);
	}

	private Candidate setCandidateForEdit(Long pk, String firstName, String middleName, String lastName) {
		Candidate result = new Candidate();
		result.setPk(pk);
		result.setFirstName(firstName);
		result.setMiddleName(middleName);
		result.setLastName(lastName);
		result.setDateOfBirth(LocalDate.now());
		result.setDisplayOrder(1);
		ctrl.setCandidateForEdit(result);
		List<Candidate> list = new ArrayList<>();
		list.add(ctrl.getCandidateForEdit());
		ctrl.setCandidateList(list);
		return result;
	}

	private ReorderEvent onRowReorder_setup(CandidateController candidateController, int from, int to, int out) {
		ReorderEvent reorderEventStub = mock(ReorderEvent.class);
		when(getInjectMock(RedigerListeforslagController.class).getAffiliation()).thenReturn(null);
		candidateController.setCandidateList(getCandidateList("in", 5));
		when(reorderEventStub.getFromIndex()).thenReturn(from);
		when(reorderEventStub.getToIndex()).thenReturn(to);
		when(getInjectMock(CandidateService.class).changeDisplayOrder(any(UserData.class), any(Candidate.class), anyInt(), anyInt())).thenReturn(
				getCandidateList("out", out));
		return reorderEventStub;
	}

	private List<Candidate> getCandidateList(String name, int num) {
		List<Candidate> result = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			result.add(new Candidate());
			result.get(i).setDisplayOrder(i + 1);
			result.get(i).setFirstName(name + i);
		}
		return result;
	}

	private List<Candidate> getGenderCandidateList(int noId) {
		List<Candidate> result = new ArrayList<>();

		result.add(candidate("12037649749")); // Mann
		result.add(candidate("12037649587")); // Mann
		result.add(candidate("12037649315")); // Mann

		result.add(candidate("12037649315")); // Kvinne
		result.add(candidate("12037649234")); // Kvinne
		result.add(candidate("12037649072")); // Kvinne
		result.add(candidate("12037648874")); // Kvinne
		for (int i = 0; i < noId; i++) {
			result.add(candidate(""));
		}
		return result;
	}

	private Candidate candidate(String id) {
		Candidate result = new Candidate();
		result.setId(id);
		return result;
	}

	private void verify_findByAffiliation() {
		verify(getInjectMock(CandidateService.class)).findByAffiliation(eq(getUserDataMock()), anyLong());
	}

	private void verify_optimisticLockExceptionHandling() {
		verify_findByAffiliation();
		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@listProposal.save.optimisticLockingException");
	}

	private void stub_searchVoter() {
		when(getInjectMock(CandidateService.class).searchVoter(eq(getUserDataMock()), any(Candidate.class), anyString(), anySet())).thenReturn(mockList(1, Voter.class));
	}

	private void stub_findByAffiliation(List<Candidate> candidates) {
		when(getInjectMock(CandidateService.class).findByAffiliation(eq(getUserDataMock()), anyLong())).thenReturn(candidates);
	}

	private class ThisCandidateController extends CandidateController {

		private Dialog editCandidateDialog;
		private Dialog confirmDeleteCandidateDialog;
		private Dialog confirmDeleteAllCandidatesDialog;
		private Dialog auditCandidateDialog;
		private Dialog uploadCandidatesDialog;

		private ThisCandidateController() {
			this.editCandidateDialog = createMock(Dialog.class);
			this.confirmDeleteCandidateDialog = createMock(Dialog.class);
			this.confirmDeleteAllCandidatesDialog = createMock(Dialog.class);
			this.auditCandidateDialog = createMock(Dialog.class);
			this.uploadCandidatesDialog = createMock(Dialog.class);
		}

		@Override
		public Dialog getEditCandidateDialog() {
			return editCandidateDialog;
		}

		@Override
		public Dialog getConfirmDeleteCandidateDialog() {
			return confirmDeleteCandidateDialog;
		}

		@Override
		public Dialog getConfirmDeleteAllCandidatesDialog() {
			return confirmDeleteAllCandidatesDialog;
		}

		@Override
		public Dialog getAuditCandidateDialog() {
			return auditCandidateDialog;
		}

		@Override
		public Dialog getUploadCandidatesDialog() {
			return uploadCandidatesDialog;
		}
	}
}

