package no.valg.eva.admin.frontend.contexteditor.ctrls;

import lombok.NoArgsConstructor;
import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.domain.model.MvElection;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ConversationScoped
@NoArgsConstructor // CDI
public class ElectionGroupController extends BaseElectionController {

	// Injected
	private MvElectionService mvElectionService;
	private ElectionGroupService electionGroupService;
	private ElectionGroup currentElectionGroup;

	@Inject
	public ElectionGroupController(ElectionGroupService electionGroupService, MvElectionService mvElectionService) {
		this.electionGroupService = electionGroupService;
		this.mvElectionService = mvElectionService;
	}

	public void prepareForCreate(MvElection parentMvElection) {
		resetNewElectionGroup(parentMvElection);
	}

	public void doCreateElectionGroup(ElectionGroup newElectionGroup) {
		execute(() -> {
			if (checkSaveElectionResponse(electionGroupService.save(getUserData(), newElectionGroup), newElectionGroup)) {
				getMvElectionPickerController().update(ElectionLevelEnum.ELECTION_GROUP.getLevel(), newElectionGroup.getElectionGroupPath().path());
				MessageUtil.buildDetailMessage(MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, getSummaryParams(newElectionGroup, false),
						FacesMessage.SEVERITY_INFO);
				currentElectionGroup = null;
				closeDialogAndUpdateHierarchyEditor("createElectionLevel1Widget");
			}
		});
	}

	public void prepareForUpdate(MvElectionMinimal mvElectionMini) {
		MvElection mvElection = mvElectionService.findByPk(mvElectionMini.getPk());
		currentElectionGroup = electionGroupService.get(getUserData(), mvElection.getElectionGroup().electionPath());
		setReadOnly(false);
	}

	public void doUpdateElectionGroup(ElectionGroup electionGroup) {
		execute(() -> {
			if (checkSaveElectionResponse(electionGroupService.save(getUserData(), electionGroup), electionGroup)) {
				getMvElectionPickerController().update(ElectionLevelEnum.ELECTION_GROUP.getLevel(), null);
				MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, MessageUtil.UPDATE_SUCCESSFUL_KEY);
				currentElectionGroup = null;
				closeDialogAndUpdateHierarchyEditor("editElectionLevel1Widget");
			}
		});
	}

	public void doDeleteElectionGroup(ElectionGroup electionGroup) {
		if (isCurrentRemovable()) {
			execute(() -> {
				electionGroupService.delete(getUserData(), electionGroup.getElectionGroupPath());
				getMvElectionPickerController().update(ElectionLevelEnum.ELECTION_GROUP.getLevel(), null);
				MessageUtil.buildDetailMessage(MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, getSummaryParams(electionGroup, false),
						FacesMessage.SEVERITY_INFO);
			});
		}
	}

	public ElectionGroup getCurrentElectionGroup() {
		return currentElectionGroup;
	}

	private void resetNewElectionGroup(MvElection parentMvElection) {
		currentElectionGroup = new ElectionGroup(parentMvElection.getElectionEvent().electionPath());
		currentElectionGroup.setElectionEventName(parentMvElection.getElectionEventName());
		currentElectionGroup.setId("");
		currentElectionGroup.setName("");
		currentElectionGroup.setElectronicMarkoffs(true);
		currentElectionGroup.setScanningPermitted(true);
		currentElectionGroup.setAdvanceVoteInBallotBox(true);
		currentElectionGroup.setValidateRoleAndListProposal(true);
		currentElectionGroup.setValidatePollingPlaceElectoralBoardAndListProposal(true);
	}

	private boolean checkSaveElectionResponse(SaveElectionResponse saveElectionResponse, ElectionGroup electionGroup) {
		if (saveElectionResponse.idNotUniqueError()) {
			MessageUtil.buildDetailMessage(MessageUtil.CHOOSE_UNIQUE_ID, getSummaryParams(electionGroup, true), FacesMessage.SEVERITY_ERROR);
			return false;
		}
		return true;
	}

	private String[] getSummaryParams(ElectionGroup electionGroup, boolean levelInfo) {
		if (levelInfo) {
			return new String[] { "@election_level[1].name", electionGroup.getId(), electionGroup.getElectionEventName() };
		}
		return new String[] { electionGroup.getName(), electionGroup.getElectionEventName() };
	}

}
