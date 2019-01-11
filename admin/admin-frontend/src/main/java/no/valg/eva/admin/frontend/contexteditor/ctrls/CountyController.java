package no.valg.eva.admin.frontend.contexteditor.ctrls;

import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.CountyService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.log4j.Logger;

@Named
@ConversationScoped
public class CountyController extends BaseController {
	private static final Logger LOGGER = Logger.getLogger(CountyController.class);

	private static final String EDIT_FORM = "level2Form:msg";
	private static final String CREATE_FORM = "level2CreateForm";
	private static final String ID_LABEL = "@area_level[2].nummer";

	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;
	@Inject
	private transient CountyService countyService;
	@Inject
	private transient MvAreaService mvAreaService;
	@Inject
	private MvAreaPickerController mvAreaPickerController;

	private County newCounty;
	private County currentCounty;
	private MvArea currentMvArea;
	private MvArea parentMvArea;
	private boolean isReadOnly;

	public void doCreateCounty(final County newCounty) {
		boolean hideDialog = false;

		if (idExists(parentMvArea.getCountry(), newCounty)) {
			String[] summaryParams = { ID_LABEL, newCounty.getId(), parentMvArea.getCountryName() };
			MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			newCounty.setCountry(parentMvArea.getCountry());
			if (execute(() -> {
				countyService.create(userData, newCounty);
				mvAreaPickerController.update(2, parentMvArea.getAreaPath() + "." + newCounty.getId());
			}, CREATE_FORM)) {
				String[] summaryParams = { newCounty.getName(), parentMvArea.getCountryName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("createCountyHideDialog", hideDialog);
		resetNewCounty();
	}

	public void setMvArea(final MvArea mvArea) {
		this.currentMvArea = mvArea;
		currentCounty = mvArea.getCounty();
		isReadOnly = false;
	}

	public void doUpdateCounty(final County county) {
		boolean hideDialog = false;

		List<MvArea> mvAreaList = mvAreaService.findByPathAndChildLevel(currentMvArea);
		if (hasIdChanged(county.getPk(), county) && (mvAreaList != null && !mvAreaList.isEmpty())) {
			String[] summaryParams = { ID_LABEL, "@area_level[2].name" };
			MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.EDIT_ID_NOT_ALLOWED, summaryParams, FacesMessage.SEVERITY_ERROR);
			return;
		}
		if (hasIdChanged(county.getPk(), county) && idExists(parentMvArea.getCountry(), county)) {
			String[] summaryParams = { ID_LABEL, county.getId(), parentMvArea.getElectionEventName() };
			MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (execute(() -> {
				currentCounty = countyService.update(userData, county);
				mvAreaPickerController.update(2, null);
			}, EDIT_FORM)) {
				String[] summaryParams = { "" };
				MessageUtil.buildFacesMessage(null, MessageUtil.UPDATE_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("editCountyHideDialog", hideDialog);
	}

	public void doDeleteCounty(final County county) {
		boolean hideDialog = false;
		if (getIsCurrentRemovable()) {
			if (execute(() -> {
				countyService.delete(userData, county);
				mvAreaPickerController.update(2, null);
			})) {
				String[] summaryParams = { county.getName(), parentMvArea.getCountryName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		} else {
			String[] summaryParams = { county.getName() };
			MessageUtil.buildFacesMessage(null, MessageUtil.REMOVE_NOT_ALLOWED_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			LOGGER.error("County " + county.getName() + " with pk " + county.getPk() + " is not deletable.");
		}
		getRequestContext().addCallbackParam("editCountyHideDialog", hideDialog);
	}

	public County getNewCounty() {
		return newCounty;
	}

	public void setNewCounty(final County newCounty) {
		this.newCounty = newCounty;
	}

	public County getCurrentCounty() {
		return currentCounty;
	}

	public void setCurrentCounty(final County currentCounty) {
		this.currentCounty = currentCounty;
	}

	public MvArea getCurrentMvArea() {
		return currentMvArea;
	}

	public MvArea getParentMvArea() {
		return parentMvArea;
	}

	public void setParentMvArea(final MvArea parentMvArea) {
		resetNewCounty();
		this.parentMvArea = parentMvArea;
	}

	private void resetNewCounty() {
		newCounty = new County();
		newCounty.setId("");
		newCounty.setName("");
	}

	public boolean idExists(final Country country, final County county) {
		return (countyService.findCountyById(userData, country.getPk(), county.getId()) != null);
	}

	public boolean hasIdChanged(final Long oldCountyPk, final County newCounty) {
		County origCounty = countyService.findByPk(userData, oldCountyPk);
		return (!origCounty.getId().equalsIgnoreCase(newCounty.getId()));
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
