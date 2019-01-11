package no.valg.eva.admin.frontend.contexteditor.ctrls;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.LegacyPollingPlaceService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.log4j.Logger;

@Named
@ConversationScoped
public class PollingPlaceController extends BaseController {
	private static final Logger LOGGER = Logger.getLogger(PollingPlaceController.class);

	private static final String EDIT_FORM = "level6Form:msg";
	private static final String CREATE_FORM = "level6CreateForm";
	private static final String ID_LABEL = "@area_level[6].nummer";
	static final int POLLING_PLACE_NONE = 0;
	static final int ADVANCE_POLLING_PLACE = 1;
	static final int ELECTION_DAY_VOTING_PLACE = 2;

	// Injected
	private UserDataController userDataController;
	private MvAreaPickerController mvAreaPickerController;
	private LegacyPollingPlaceService pollingPlaceService;

	private PollingPlace pollingPlace;
	private MvArea currentMvArea;
	private MvArea parentMvArea;
	private int pollingPlaceType;
	private boolean isReadOnly;

	public PollingPlaceController() {
		// CDI
	}

	@Inject
	public PollingPlaceController(UserDataController userDataController, MvAreaPickerController mvAreaPickerController,
			LegacyPollingPlaceService pollingPlaceService) {
		this.userDataController = userDataController;
		this.mvAreaPickerController = mvAreaPickerController;
		this.pollingPlaceService = pollingPlaceService;
	}

	public void changePollingPlaceType(ValueChangeEvent event) {
		pollingPlaceType = (Integer) event.getNewValue();
		switch (pollingPlaceType) {

		case ADVANCE_POLLING_PLACE:
			pollingPlace.setElectionDayVoting(false);
			if (getUserData().isElectionEventAdminUser()) {
				pollingPlace.setAdvanceVoteInBallotBox(true);
			}
			break;

		case ELECTION_DAY_VOTING_PLACE:
			pollingPlace.setElectionDayVoting(true);
			break;

		default:
			break;
		}
	}

	public void setParentMvArea(MvArea parentMvArea) {
		resetNewPollingPlace();
		this.parentMvArea = parentMvArea;
	}

	public void doCreatePollingPlace() {
		boolean hideDialog = false;
		if (idExists(parentMvArea.getPollingDistrict(), pollingPlace)) {
			String[] summaryParams = { ID_LABEL, pollingPlace.getId(), parentMvArea.getPollingDistrictName() };
			MessageUtil.buildFacesMessage(getFacesContext(), CREATE_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			pollingPlace.setPollingDistrict(parentMvArea.getPollingDistrict());
			if (!pollingPlace.isElectionDayVoting()) {
				if (!getUserData().isElectionEventAdminUser()) {
					pollingPlace.setAdvanceVoteInBallotBox(true);
				}
			}
			if (execute(() -> {
				pollingPlace = pollingPlaceService.create(getUserData(), pollingPlace);
				String[] summaryParams = { pollingPlace.getName(), parentMvArea.getPollingDistrictName() };
				MessageUtil.buildFacesMessage(getFacesContext(), null, MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams,
						FacesMessage.SEVERITY_INFO);
				mvAreaPickerController.update(AreaLevelEnum.POLLING_PLACE.getLevel(), parentMvArea.getAreaPath() + "." + pollingPlace.getId());
			}, CREATE_FORM)) {
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("createPollingPlaceHideDialog", hideDialog);
		if (hideDialog) {
			resetNewPollingPlace();
		}
	}

	public void setMvArea(MvArea mvArea) {
		this.currentMvArea = mvArea;
		isReadOnly = false;
		pollingPlace = mvArea.getPollingPlace();
	}

	public void doUpdatePollingPlace() {
		boolean hideDialog = false;

		if (hasIdChanged(pollingPlace.getPk(), pollingPlace) && idExists(parentMvArea.getPollingDistrict(), pollingPlace)) {
			String[] summaryParams = { ID_LABEL, pollingPlace.getId(), parentMvArea.getPollingDistrictName() };
			MessageUtil.buildFacesMessage(getFacesContext(), EDIT_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (execute(() -> {
				this.pollingPlace = pollingPlaceService.update(getUserData(), pollingPlace);
				mvAreaPickerController.update(AreaLevelEnum.POLLING_PLACE.getLevel(), null);
			}, EDIT_FORM)) {
				String[] summaryParams = { "" };
				MessageUtil.buildFacesMessage(getFacesContext(), null, MessageUtil.UPDATE_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("editPollingPlaceHideDialog", hideDialog);
	}

	public void doDeletePollingPlace(PollingPlace pollingPlace) {
		boolean hideDialog = false;
		if (getIsCurrentRemovable()) {
			if (execute(() -> {
				pollingPlaceService.delete(getUserData(), pollingPlace);
				mvAreaPickerController.update(AreaLevelEnum.POLLING_PLACE.getLevel(), null);
			})) {
				String[] summaryParams = { pollingPlace.getName(), parentMvArea.getPollingDistrictName() };
				MessageUtil.buildFacesMessage(getFacesContext(), null, MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		} else {
			String[] summaryParams = { pollingPlace.getName() };
			MessageUtil.buildFacesMessage(getFacesContext(), null, MessageUtil.REMOVE_NOT_ALLOWED_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			LOGGER.error("Polling place " + pollingPlace.getName() + " with pk " + pollingPlace.getPk() + " is not deletable.");
		}
		getRequestContext().addCallbackParam("editPollingPlaceHideDialog", hideDialog);
	}

	public PollingPlace getPollingPlace() {
		return pollingPlace;
	}

	public void setPollingPlace(PollingPlace pollingPlace) {
		this.pollingPlace = pollingPlace;
	}

	public MvArea getCurrentMvArea() {
		return currentMvArea;
	}

	public MvArea getParentMvArea() {
		return parentMvArea;
	}

	private void resetNewPollingPlace() {
		pollingPlace = new PollingPlace();
		pollingPlace.setUsingPollingStations(false);
		pollingPlaceType = POLLING_PLACE_NONE;
	}

	public boolean idExists(PollingDistrict pollingDistrict, PollingPlace pollingPlace) {
		return pollingPlaceService.findPollingPlaceById(getUserData(), pollingDistrict.getPk(), pollingPlace.getId()) != null;
	}

	public boolean hasIdChanged(Long oldPollingPlacePk, PollingPlace newPollingPlace) {
		PollingPlace origPollingPlace = pollingPlaceService.findByPk(getUserData(), oldPollingPlacePk);
		return (!origPollingPlace.getId().equalsIgnoreCase(newPollingPlace.getId()));
	}

	public Boolean getIsCurrentRemovable() {
		int configLevel = userDataController.getElectionEvent().getElectionEventStatus().getId();
		return configLevel < EvoteConstants.FREEZE_LEVEL_AREA;
	}

	public int getPollingPlaceType() {
		return pollingPlaceType;
	}

	public void setPollingPlaceType(int pollingPlaceType) {
		this.pollingPlaceType = pollingPlaceType;
	}

	public boolean isPollingPlaceTypeSelected() {
		return pollingPlaceType != POLLING_PLACE_NONE;
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public boolean isRenderBallotOrEnvelopeChoice(PollingPlace pollingPlace) {
		if (pollingPlace == null) {
			return false;
		}
		if (pollingPlace.getPk() == null) {
			// New polling place
			return isPollingPlaceTypeSelected() && !pollingPlace.isElectionDayVoting();
		}
		// Existing polling place
		return !pollingPlace.isElectionDayVoting();
	}

	private UserData getUserData() {
		return userDataController.getUserData();
	}
}
