package no.valg.eva.admin.frontend.contexteditor.ctrls;

import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

public abstract class BaseElectionController extends BaseController {

	@Inject
	private MessageProvider messageProvider;
	@Inject
	private UserDataController userDataController;
	@Inject
	private MvElectionPickerController mvElectionPickerController;

	private boolean isReadOnly;

	public boolean isCurrentRemovable() {
		return userDataController.getElectionEvent().getElectionEventStatus().getId() < EvoteConstants.FREEZE_LEVEL_AREA;
	}

	public boolean isReadOnly() {
		return isReadOnly && !userDataController.isOverrideAccess();
	}

	public void setReadOnly(final boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public boolean isInputDisabled() {
		return userDataController.isCurrentElectionEventDisabled() || isReadOnly();
	}

	void updateHierarchyEditor() {
		FacesUtil.updateDom("hierarchyEditor");
	}

	void closeDialog(String id) {
		FacesUtil.executeJS("PF('" + id + "').hide()");
	}

	void closeDialogAndUpdateHierarchyEditor(String id) {
		FacesUtil.executeJS("PF('" + id + "').hide()");
		updateHierarchyEditor();
	}
	
	UserData getUserData() {
		return userDataController.getUserData();
	}

	MessageProvider getMessageProvider() {
		return messageProvider;
	}

	UserDataController getUserDataController() {
		return userDataController;
	}

	MvElectionPickerController getMvElectionPickerController() {
		return mvElectionPickerController;
	}
}
