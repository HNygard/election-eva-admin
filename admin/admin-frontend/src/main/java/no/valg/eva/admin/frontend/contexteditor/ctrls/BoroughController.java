package no.valg.eva.admin.frontend.contexteditor.ctrls;

import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.BoroughService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.log4j.Logger;

@Named
@ConversationScoped
public class BoroughController extends BaseController {
	private static final Logger LOGGER = Logger.getLogger(BoroughController.class);

	private static final String EDIT_FORM = "level4Form:msg";
	private static final String CREATE_FORM = "level4CreateForm";
	private static final String ID_LABEL = "@area_level[4].nummer";

	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;
	@Inject
	private MvAreaPickerController mvAreaPickerController;
	@Inject
	private BoroughService boroughService;
	@Inject
	private MvAreaService mvAreaService;

	private Borough newBorough;
	private Borough currentBorough;
	private MvArea currentMvArea;
	private MvArea parentMvArea;
	private boolean isReadOnly;

	public void doCreateBorough(final Borough newBorough) {
		boolean hideDialog = false;
		
		if (!newBorough.getId().substring(0, 4).equalsIgnoreCase(parentMvArea.getMunicipality().getId())) {
			String[] summaryParams = { ID_LABEL, "@area_level[3].nummer", "@area.list_areas.message.id_four_first" };
			MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.AREA_ID_MUST_CONFORM, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (idExists(parentMvArea.getMunicipality(), newBorough)) {
				String[] summaryParams = { ID_LABEL, newBorough.getId(), parentMvArea.getMunicipalityName() };
				MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
			} else {
				newBorough.setMunicipality(parentMvArea.getMunicipality());
				if (execute(() -> {
					boroughService.create(userData, newBorough);
					mvAreaPickerController.update(4, parentMvArea.getAreaPath() + "." + newBorough.getId());
				}, CREATE_FORM)) {
					String[] summaryParams = { newBorough.getName(), parentMvArea.getMunicipalityName() };
					MessageUtil.buildFacesMessage(null, MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
					hideDialog = true;
				}
			}
		}
		
		getRequestContext().addCallbackParam("createBoroughHideDialog", hideDialog);
		resetNewBorough();
	}

	public void setMvArea(final MvArea mvArea) {
		this.currentMvArea = mvArea;
		currentBorough = mvArea.getBorough();
		isReadOnly = false;
	}

	public void doUpdateBorough(final Borough borough) {
		boolean hideDialog = false;

		List<MvArea> mvAreaList = mvAreaService.findByPathAndChildLevel(currentMvArea);
		if (hasIdChanged(borough.getPk(), borough) && (mvAreaList != null && !mvAreaList.isEmpty())) {
			String[] summaryParams = { ID_LABEL, "@area_level[4].name" };
			MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.EDIT_ID_NOT_ALLOWED, summaryParams, FacesMessage.SEVERITY_ERROR);
			return;
		}
		
		if (!borough.getId().substring(0, 4).equalsIgnoreCase(parentMvArea.getMunicipality().getId())) {
			String[] summaryParams = { ID_LABEL, "@area_level[3].nummer", "@area.list_areas.message.id_four_first" };
			MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.AREA_ID_MUST_CONFORM, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (hasIdChanged(borough.getPk(), borough) && idExists(parentMvArea.getMunicipality(), borough)) {
				String[] summaryParams = { ID_LABEL, borough.getId(), parentMvArea.getMunicipalityName() };
				MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
			} else {
				if (execute(() -> {
					currentBorough = boroughService.update(userData, borough);
					mvAreaPickerController.update(4, null);
				}, EDIT_FORM)) {
					String[] summaryParams = { "" };
					MessageUtil.buildFacesMessage(null, MessageUtil.UPDATE_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
					hideDialog = true;
				}
			}
		}
		
		getRequestContext().addCallbackParam("editBoroughHideDialog", hideDialog);
	}

	public void doDeleteBorough(final Borough borough) {
		boolean hideDialog = false;
		if (getIsCurrentRemovable()) {
			if (execute(() -> {
				boroughService.delete(userData, borough);
				
				mvAreaPickerController.update(4, null);
				
			})) {
				String[] summaryParams = { borough.getName(), parentMvArea.getMunicipalityName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		} else {
			String[] summaryParams = { borough.getName() };
			MessageUtil.buildFacesMessage(null, MessageUtil.REMOVE_NOT_ALLOWED_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			LOGGER.error("Country " + borough.getName() + " with pk " + borough.getPk() + " is not deletable.");
		}
		getRequestContext().addCallbackParam("editBoroughHideDialog", hideDialog);
	}

	public Borough getNewBorough() {
		return newBorough;
	}

	public void setNewBorough(final Borough newBorough) {
		this.newBorough = newBorough;
	}

	public Borough getCurrentBorough() {
		return currentBorough;
	}

	public void setCurrentBorough(final Borough borough) {
		this.currentBorough = borough;
	}

	public MvArea getCurrentMvArea() {
		return currentMvArea;
	}

	public MvArea getParentMvArea() {
		return parentMvArea;
	}

	public void setParentMvArea(final MvArea parentMvArea) {
		resetNewBorough();
		this.parentMvArea = parentMvArea;
	}

	private void resetNewBorough() {
		newBorough = new Borough();
		newBorough.setId("");
		newBorough.setName("");
	}

	public boolean idExists(final Municipality municipality, final Borough borough) {
		return (boroughService.findBoroughById(userData, municipality.getPk(), borough.getId()) != null);
	}

	public boolean hasIdChanged(final Long oldBoroughPk, final Borough newBorough) {
		Borough origBorough = boroughService.findByPk(userData, oldBoroughPk);
		return (!origBorough.getId().equalsIgnoreCase(newBorough.getId()));
	}

	public Boolean getIsCurrentRemovable() {
		int configLevel = userDataController.getElectionEvent().getElectionEventStatus().getId();
		return configLevel < EvoteConstants.FREEZE_LEVEL_AREA;
	}

	public boolean isReadOnly() {
		// Brukere med override-tilgang skal ha mulighet til å endre selv når
		// konfigurasjonen er godkjent. Se issue #2997.
		return isReadOnly && !userDataController.isOverrideAccess();
	}

	public void setReadOnly(final boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
}
