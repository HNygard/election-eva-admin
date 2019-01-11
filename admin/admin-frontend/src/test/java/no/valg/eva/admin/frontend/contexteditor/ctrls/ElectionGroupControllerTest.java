package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectionGroupControllerTest extends BaseFrontendTest {

	@Test
	public void prepareForCreate_withMvElection_verifyState() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);
		MvElection mvElection = createMock(MvElection.class);
		when(mvElection.getElectionEvent().electionPath()).thenReturn(ELECTION_PATH_ELECTION_EVENT);
		when(mvElection.getElectionEventName()).thenReturn("Event");

		ctrl.prepareForCreate(mvElection);

		assertThat(ctrl.getCurrentElectionGroup()).isNotNull();
		assertThat(ctrl.getCurrentElectionGroup().getParentElectionPath()).isEqualTo(ELECTION_PATH_ELECTION_EVENT);
		assertThat(ctrl.getCurrentElectionGroup().getElectionEventName()).isEqualTo("Event");
		assertThat(ctrl.getCurrentElectionGroup().getId()).isEmpty();
		assertThat(ctrl.getCurrentElectionGroup().getName()).isEmpty();
		assertThat(ctrl.getCurrentElectionGroup().isElectronicMarkoffs()).isTrue();
		assertThat(ctrl.getCurrentElectionGroup().isAdvanceVoteInBallotBox()).isTrue();
	}

	@Test
	public void doCreateElectionGroup_withDuplicateIdResponse_returnsErrorMessage() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);
		ElectionGroup electionGroup = electionGroup();
		stub_create(saveElectionResponse(true, electionGroup));

		ctrl.doCreateElectionGroup(electionGroup);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, @election_level[1].name, 99, ElectionEvent]");
	}

	@Test
	public void doCreateElectionGroup_withNewGroup_savesGroup() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);
		ElectionGroup electionGroup = electionGroup();
		stub_create(saveElectionResponse(false, electionGroup));

		ctrl.doCreateElectionGroup(electionGroup);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_create.successful, Name 99, ElectionEvent]");
		assertThat(ctrl.getCurrentElectionGroup()).isNull();
		verify(getInjectMock(ElectionGroupService.class)).save(getUserDataMock(), electionGroup);
		verify(getInjectMock(MvElectionPickerController.class)).update(ElectionLevelEnum.ELECTION_GROUP.getLevel(),
				electionGroup.getElectionGroupPath().path());
		verify_closeDialogAndUpdateHierarchyEditor("createElectionLevel1Widget");
	}

	@Test
	public void prepareForUpdate_withElection_verifyState() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);

		ctrl.prepareForUpdate(createMock(MvElectionMinimal.class));

		assertThat(ctrl.getCurrentElectionGroup()).isNotNull();
	}

	@Test
	public void doUpdateElectionGroup_withValidGroup_updatesGroup() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);
		ElectionGroup electionGroup = electionGroup();
		stub_create(saveElectionResponse(false, electionGroup));

		ctrl.doUpdateElectionGroup(electionGroup);

		assertThat(ctrl.getCurrentElectionGroup()).isNull();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.update.successful");
		verify(getInjectMock(MvElectionPickerController.class)).update(ElectionLevelEnum.ELECTION_GROUP.getLevel(), null);
		verify(getInjectMock(ElectionGroupService.class)).save(getUserDataMock(), electionGroup);
		verify_closeDialogAndUpdateHierarchyEditor("editElectionLevel1Widget");
	}

	@Test
	public void doDeleteElectionGroup_withisCurrentRemovableFalse_doesNotDeleteGroup() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);
		ElectionGroup electionGroup = electionGroup();
		stub_isCurrentRemovable(false);

		ctrl.doDeleteElectionGroup(electionGroup);

		verify(getInjectMock(ElectionGroupService.class), never()).delete(eq(getUserDataMock()), any(ElectionPath.class));
	}

	@Test
	public void doDeleteElectionGroup_withisCurrentRemovable_deletesGroup() throws Exception {
		ElectionGroupController ctrl = initializeMocks(ElectionGroupController.class);
		ElectionGroup electionGroup = electionGroup();
		stub_isCurrentRemovable(true);

		ctrl.doDeleteElectionGroup(electionGroup);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_delete.successful, Name 99, ElectionEvent]");
		verify(getInjectMock(ElectionGroupService.class), times(1)).delete(eq(getUserDataMock()), any(ElectionPath.class));
		verify(getInjectMock(MvElectionPickerController.class), times(1)).update(ElectionLevelEnum.ELECTION_GROUP.getLevel(), null);
	}

	private void verify_closeDialogAndUpdateHierarchyEditor(String id) {
		verify(getRequestContextMock()).execute("PF('" + id + "').hide()");
		verify(getRequestContextMock()).update("hierarchyEditor");
	}

	private SaveElectionResponse stub_create(SaveElectionResponse response) {
		when(getInjectMock(ElectionGroupService.class).save(eq(getUserDataMock()), any(ElectionGroup.class))).thenReturn(response);
		return response;
	}

	private SaveElectionResponse saveElectionResponse(boolean withIdNotUniqueError, ElectionGroup electionGroup) {
		if (withIdNotUniqueError) {
			return SaveElectionResponse.withIdNotUniqueError();
		}
		return SaveElectionResponse.ok().setVersionedObject(electionGroup);
	}

	private ElectionGroup electionGroup() {
		ElectionGroup result = new ElectionGroup(ELECTION_PATH_ELECTION_EVENT);
		result.setId("99");
		result.setName("Name 99");
		result.setElectionEventName("ElectionEvent");
		return result;

	}

	private void stub_isCurrentRemovable(boolean isCurrentRemovable) {
		ElectionEventStatusEnum status = isCurrentRemovable ? ElectionEventStatusEnum.CENTRAL_CONFIGURATION : ElectionEventStatusEnum.CLOSED;
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus().getId()).thenReturn(status.id());
	}

}
