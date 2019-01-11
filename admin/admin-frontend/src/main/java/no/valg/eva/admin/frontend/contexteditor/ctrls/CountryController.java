package no.valg.eva.admin.frontend.contexteditor.ctrls;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.CountryService;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.log4j.Logger;

@Named
@ConversationScoped
public class CountryController extends BaseController {

	private static final String EDIT_FORM = "level1Form:msg";
	private static final String CREATE_FORM = "level1CreateForm";
	private static final String ID_LABEL = "@area_level[1].nummer";

	private static final Logger LOGGER = Logger.getLogger(CountryController.class);

	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;
	@Inject
	private transient CountryService countryService;
	@Inject
	private MvAreaPickerController mvAreaPickerController;

	private Country newCountry;
	private Country currentCountry;
	private MvArea currentMvArea;
	private MvArea parentMvArea;
	private boolean isReadOnly;

	public void doCreateCountry(final Country newCountry) {
		boolean hideDialog = false;
		if (idExists(parentMvArea.getElectionEvent(), newCountry)) {
			String[] summaryParams = { ID_LABEL, newCountry.getId(), parentMvArea.getElectionEventName() };
			MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (execute(() -> {
				countryService.create(userData, newCountry);
				mvAreaPickerController.update(1, parentMvArea.getAreaPath() + "." + newCountry.getId());
			}, CREATE_FORM)) {
				hideDialog = true;
				String[] summaryParams = { newCountry.getName(), parentMvArea.getElectionEventName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			}
		}
		getRequestContext().addCallbackParam("createCountryHideDialog", hideDialog);
		resetNewCountry();
	}

	public void setMvArea(final MvArea mvArea) {
		this.currentMvArea = mvArea;
		currentCountry = mvArea.getCountry();
		isReadOnly = false;
	}

	public void doUpdateCountry(final Country country) {
		boolean hideDialog = false;

		if (hasIdChanged(country.getPk(), country) && idExists(parentMvArea.getElectionEvent(), country)) {
			String[] summaryParams = { ID_LABEL, country.getId(), parentMvArea.getElectionEventName() };
			MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (execute(() -> {
				currentCountry = countryService.update(userData, country);
				mvAreaPickerController.update(1, null);
			}, EDIT_FORM)) {
				String[] summaryParams = { "" };
				MessageUtil.buildFacesMessage(null, MessageUtil.UPDATE_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("editCountryHideDialog", hideDialog);
	}

	public void doDeleteCountry(final Country country) {
		boolean hideDialog = false;
		if (getIsCurrentRemovable()) {
			if (execute(() -> {
				countryService.delete(userData, country);
				mvAreaPickerController.update(1, null);

			})) {
				String[] summaryParams = { country.getName(), parentMvArea.getElectionEventName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		} else {
			String[] summaryParams = { country.getName() };
			MessageUtil.buildFacesMessage(null, MessageUtil.REMOVE_NOT_ALLOWED_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			LOGGER.error("Country " + country.getName() + " with pk " + country.getPk() + " is not deletable.");
		}
		getRequestContext().addCallbackParam("editCountryHideDialog", hideDialog);
	}

	public Country getNewCountry() {
		return newCountry;
	}

	public void setNewCountry(final Country newCountry) {
		this.newCountry = newCountry;
	}

	public Country getCurrentCountry() {
		return currentCountry;
	}

	public void setCurrentCountry(final Country currentCountry) {
		this.currentCountry = currentCountry;
	}

	public MvArea getCurrentMvArea() {
		return currentMvArea;
	}

	public MvArea getParentMvArea() {
		return parentMvArea;
	}

	public void setParentMvArea(final MvArea parentMvArea) {
		this.parentMvArea = parentMvArea;
		resetNewCountry();
	}

	private void resetNewCountry() {
		newCountry = new Country();
		newCountry.setId("");
		newCountry.setName("");
		newCountry.setElectionEvent(parentMvArea.getElectionEvent());
	}

	public boolean idExists(final ElectionEvent electionEvent, final Country country) {
		return (countryService.findCountryById(userData, electionEvent.getPk(), country.getId()) != null);
	}

	public boolean hasIdChanged(final Long oldCountryPk, final Country newCountry) {
		Country origCountry = countryService.findByPk(userData, oldCountryPk);
		return (!origCountry.getId().equalsIgnoreCase(newCountry.getId()));
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
