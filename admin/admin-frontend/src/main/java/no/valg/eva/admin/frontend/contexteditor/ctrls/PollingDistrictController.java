package no.valg.eva.admin.frontend.contexteditor.ctrls;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.LegacyPollingDistrictService;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.apache.log4j.Logger;

@Named
@ConversationScoped
public class PollingDistrictController extends BaseController {
	private static final Logger LOGGER = Logger.getLogger(PollingDistrictController.class);

	protected static final String POLLING_DISTRICT_TYPE_KEY_ORDINARY = "@area.polling_district.polling_district_type_select.ordinary";
	protected static final String POLLING_DISTRICT_TYPE_KEY_MUNICIPALITY = "@area.polling_district.municipality";
	protected static final String POLLING_DISTRICT_TYPE_KEY_TECHNICAL = "@area.polling_district.technical_polling_district";
	protected static final String POLLING_DISTRICT_TYPE_KEY_PARENT = "@area.polling_district.parent_polling_district";
	protected static final String POLLING_DISTRICT_TYPE_KEY_CHILD = "@area.polling_district.child_polling_district";
	private static final String EDIT_FORM = "level5Form:msg";
	private static final String CREATE_FORM = "level5CreateForm";
	private static final String ID_LABEL = "@area_level[5].nummer";
	private static final int POLLING_DISTRICT_TYPE_ORDINARY = 1;
	private static final int POLLING_DISTRICT_TYPE_MUNICIPALITY = 2;

	// Injected
	private UserDataController userDataController;
	private MvAreaPickerController mvAreaPickerController;
	private MessageProvider messageProvider;
	private LegacyPollingDistrictService pollingDistrictService;

	private PollingDistrict pollingDistrict;
	private MvArea currentMvArea;
	private MvArea parentMvArea;
	private int pollingDistrictType;
	private List<PollingDistrict> pollingDistrictsForParentList;
	private boolean typeMunicipalityCanBeCreated = false;
	private boolean isReadOnly;

	public PollingDistrictController() {
		// For CDI
	}

	@Inject
	public PollingDistrictController(UserDataController userDataController, MvAreaPickerController mvAreaPickerController, MessageProvider messageProvider,
			LegacyPollingDistrictService pollingDistrictService) {
		this.userDataController = userDataController;
		this.mvAreaPickerController = mvAreaPickerController;
		this.messageProvider = messageProvider;
		this.pollingDistrictService = pollingDistrictService;
	}

	public String getLevel5DialogHeader() {
		if (isReadOnly()) {
			return translate("@area_level[5].name");
		} else {
			return translate("@common.redact") + " " + translate("@area_level[5].name");
		}
	}

	public void setParentMvArea(MvArea parentMvArea) {
		resetNewPollingDistrict();
		this.parentMvArea = parentMvArea;
		typeMunicipalityCanBeCreated = parentMvArea.getBorough().isMunicipality1()
				&& !pollingDistrictService.municipalityProxyExists(getUserData(), parentMvArea.getMunicipality().getPk());
	}

	public void doCreatePollingDistrict() {
		boolean hideDialog = false;

		if (idExists(parentMvArea.getBorough(), pollingDistrict)) {
			String[] summaryParams = { ID_LABEL, pollingDistrict.getId(), parentMvArea.getBoroughName() };
			MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (pollingDistrictType == POLLING_DISTRICT_TYPE_MUNICIPALITY && typeMunicipalityCanBeCreated) {
				pollingDistrict.setMunicipality(true);
			}
			pollingDistrict.setBorough(parentMvArea.getBorough());
			if (execute(() -> {
				pollingDistrictService.create(getUserData(), pollingDistrict);
				mvAreaPickerController.update(AreaLevelEnum.POLLING_DISTRICT.getLevel(), parentMvArea.getAreaPath() + "." + pollingDistrict.getId());
			}, CREATE_FORM)) {
				String[] summaryParams = { pollingDistrict.getName(), parentMvArea.getMunicipalityName() + " - " + parentMvArea.getBoroughName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("createPollingDistrictHideDialog", hideDialog);
		resetNewPollingDistrict();
	}

	public void setMvArea(MvArea mvArea) {
		currentMvArea = mvArea;
		isReadOnly = false;
		pollingDistrict = mvArea.getPollingDistrict();
		pollingDistrict.setChildPollingDistrict(pollingDistrict.getPollingDistrict() != null);
		if (pollingDistrict.isParentPollingDistrict()) {
			pollingDistrictsForParentList = pollingDistrictService.findPollingDistrictsForParent(getUserData(), pollingDistrict);
		} else {
			pollingDistrictsForParentList = null;
		}
	}

	public void doUpdatePollingDistrict() {
		boolean hideDialog = false;
		if (hasIdChanged(pollingDistrict.getPk(), pollingDistrict) && idExists(parentMvArea.getBorough(), pollingDistrict)) {
			String[] summaryParams = { ID_LABEL, pollingDistrict.getId(), parentMvArea.getMunicipalityName() + " - " + parentMvArea.getBoroughName() };
			MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
		} else {
			if (execute(() -> {
				pollingDistrict = pollingDistrictService.update(getUserData(), pollingDistrict);
				mvAreaPickerController.update(AreaLevelEnum.POLLING_DISTRICT.getLevel(), null);
			}, EDIT_FORM)) {
				String[] summaryParams = { "" };
				MessageUtil.buildFacesMessage(null, MessageUtil.UPDATE_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		}
		getRequestContext().addCallbackParam("editPollingDistrictHideDialog", hideDialog);
	}

	public void doDeletePollingDistrict(PollingDistrict pollingDistrict) {
		boolean hideDialog = false;
		if (getCurrentRemovable()) {
			if (execute(() -> {
				pollingDistrictService.delete(getUserData(), pollingDistrict);
				mvAreaPickerController.update(AreaLevelEnum.POLLING_DISTRICT.getLevel(), null);
			})) {
				String[] summaryParams = { pollingDistrict.getName(), parentMvArea.getMunicipalityName() + " - " + parentMvArea.getBoroughName() };
				MessageUtil.buildFacesMessage(null, MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
				hideDialog = true;
			}
		} else {
			String[] summaryParams = { pollingDistrict.getName() };
			MessageUtil.buildFacesMessage(null, MessageUtil.REMOVE_NOT_ALLOWED_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
			LOGGER.error("Polling district " + pollingDistrict.getName() + " with pk " + pollingDistrict.getPk() + " is not deletable.");
		}
		getRequestContext().addCallbackParam("editPollingDistrictHideDialog", hideDialog);
	}

	public PollingDistrict getPollingDistrict() {
		return pollingDistrict;
	}

	public void setPollingDistrict(PollingDistrict pollingDistrict) {
		this.pollingDistrict = pollingDistrict;
	}

	public MvArea getCurrentMvArea() {
		return currentMvArea;
	}

	public MvArea getParentMvArea() {
		return parentMvArea;
	}

	private void resetNewPollingDistrict() {
		pollingDistrict = new PollingDistrict();
		pollingDistrict.setName("");
		pollingDistrict.setId("");
		pollingDistrict.setParentPollingDistrict(false);
		pollingDistrict.setChildPollingDistrict(false);
		pollingDistrict.setMunicipality(false);
		pollingDistrict.setPollingDistrict(null);
		pollingDistrictType = POLLING_DISTRICT_TYPE_ORDINARY;
	}

	public List<PollingDistrict> getPollingDistrictsForParentList() {
		return pollingDistrictsForParentList;
	}

	public int getPollingDistrictType() {
		return pollingDistrictType;
	}

	public void setPollingDistrictType(int pollingDistrictType) {
		this.pollingDistrictType = pollingDistrictType;
	}

	public boolean idExists(Borough borough, PollingDistrict pollingDistrict) {
		return pollingDistrictService.findPollingDistrictById(getUserData(), borough.getPk(), pollingDistrict.getId()) != null;
	}

	public boolean hasIdChanged(Long oldPollingDistrictPk, PollingDistrict newPollingDistrict) {
		PollingDistrict origPollingDistrict = pollingDistrictService.findByPk(getUserData(), oldPollingDistrictPk);
		return (!origPollingDistrict.getId().equalsIgnoreCase(newPollingDistrict.getId()));
	}

	private boolean isCurrentTypeEditable() {
		return pollingDistrict != null && pollingDistrict.isEditableCentrally();
	}

	public Boolean getCurrentRemovable() {
		int configLevel = userDataController.getElectionEvent().getElectionEventStatus().getId();
		return isCurrentTypeEditable() && configLevel < EvoteConstants.FREEZE_LEVEL_AREA;
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public List<SelectItem> getPollingDistrictTypes() {
		List<SelectItem> items = new ArrayList<>();
		items.add(new SelectItem(POLLING_DISTRICT_TYPE_ORDINARY, translate(POLLING_DISTRICT_TYPE_KEY_ORDINARY)));
		items.add(new SelectItem(POLLING_DISTRICT_TYPE_MUNICIPALITY, translate(POLLING_DISTRICT_TYPE_KEY_MUNICIPALITY)));
		return items;
	}

	public String getPollingDistrictTypeText(PollingDistrict pollingDistrict) {
		if (pollingDistrict == null) {
			return "";
		}
		switch (pollingDistrict.type()) {
		case MUNICIPALITY:
			return translate(POLLING_DISTRICT_TYPE_KEY_MUNICIPALITY);
		case PARENT:
			return translate(POLLING_DISTRICT_TYPE_KEY_PARENT);
		case TECHNICAL:
			return translate(POLLING_DISTRICT_TYPE_KEY_TECHNICAL);
		case CHILD:
			return translate(POLLING_DISTRICT_TYPE_KEY_CHILD);
		default:
			return translate(POLLING_DISTRICT_TYPE_KEY_ORDINARY);
		}
	}

	private String translate(String msgKey) {
		return messageProvider.get(msgKey);
	}

	private UserData getUserData() {
		return userDataController.getUserData();
	}
}
