package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.CountyService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.apache.log4j.Logger;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@ConversationScoped
public class MunicipalityController extends BaseController {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(MunicipalityController.class);

    private static final String EDIT_FORM = "level3Form:msg";
    private static final String CREATE_FORM = "level3CreateForm";
    private static final String ID_LABEL = "@area_level[3].nummer";

    @Inject
    private UserData userData;
    @Inject
    private UserDataController userDataController;
    @Inject
    private transient MunicipalityService municipalityService;
    @Inject
    private transient CountyService countyService;
    @Inject
    private transient MvAreaService mvAreaService;
    @Inject
    private MvAreaPickerController mvAreaPickerController;

    private Municipality newMunicipality;
    private Municipality currentMunicipality;
    private MvArea currentMvArea;
    private MvArea parentMvArea;
    private boolean isReadOnly;

    public void doCreateMunicipality(final Municipality newMunicipality) {
        boolean hideDialog = false;

        newMunicipality.setCounty(parentMvArea.getCounty());
        if (!newMunicipality.getId().substring(0, 2).equalsIgnoreCase(parentMvArea.getCounty().getId())) {
            String[] summaryParams = {ID_LABEL, "@area_level[2].nummer", "@area.list_areas.message.id_two_first"};
            MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.AREA_ID_MUST_CONFORM, summaryParams, FacesMessage.SEVERITY_ERROR);
        } else {
            if (idExists(parentMvArea.getCounty(), newMunicipality)) {
                String[] summaryParams = {ID_LABEL, newMunicipality.getId(), parentMvArea.getCountyName()};
                MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
            } else {
                newMunicipality.setCounty(parentMvArea.getCounty());
                newMunicipality.setElectronicMarkoffs(false);
                if (execute(() -> {
                    municipalityService.create(userData, newMunicipality);
                    mvAreaPickerController.update(AreaLevelEnum.MUNICIPALITY.getLevel(), parentMvArea.getAreaPath() + "." + newMunicipality.getId());
                }, CREATE_FORM)) {
                    hideDialog = true;
                    String[] summaryParams = {newMunicipality.getName(), parentMvArea.getCountyName()};
                    MessageUtil.buildFacesMessage(null, MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
                }
            }
        }
        getRequestContext().addCallbackParam("createMunicipalityHideDialog", hideDialog);
        resetNewMunicipality();
    }

    public County getCurrentCounty() {
        if (currentMunicipality == null) {
            return null;
        }
        return countyService.findByMunicipality(userData, currentMunicipality.getPk());
    }

    public void setMvArea(final MvArea mvArea) {
        currentMunicipality = mvArea.getMunicipality();
        currentMvArea = mvArea;
        isReadOnly = false;
    }

    public void doUpdateMunicipality(final Municipality municipality) {
        boolean hideDialog = false;

        List<MvArea> mvAreaList = mvAreaService.findByPathAndChildLevel(currentMvArea);
        if (hasIdChanged(municipality.getPk(), municipality) && (mvAreaList != null && !mvAreaList.isEmpty())) {
            String[] summaryParams = {ID_LABEL, "@area_level[3].name"};
            MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.EDIT_ID_NOT_ALLOWED, summaryParams, FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (!municipality.getId().substring(0, 2).equalsIgnoreCase(parentMvArea.getCounty().getId())) {
            String[] summaryParams = {ID_LABEL, "@area_level[2].nummer", "@area.list_areas.message.id_two_first"};
            MessageUtil.buildFacesMessage(CREATE_FORM, MessageUtil.AREA_ID_MUST_CONFORM, summaryParams, FacesMessage.SEVERITY_ERROR);
        } else {
            if (hasIdChanged(municipality.getPk(), municipality) && idExists(parentMvArea.getCounty(), municipality)) {
                String[] summaryParams = {ID_LABEL, municipality.getId(), parentMvArea.getCountyName()};
                MessageUtil.buildFacesMessage(EDIT_FORM, MessageUtil.CHOOSE_UNIQUE_ID, summaryParams, FacesMessage.SEVERITY_ERROR);
            } else {
                if (execute(() -> {
                    currentMunicipality = municipalityService.update(userData, municipality);
                    mvAreaPickerController.update(AreaLevelEnum.MUNICIPALITY.getLevel(), null);
                }, EDIT_FORM)) {
                    String[] summaryParams = {""};
                    MessageUtil.buildFacesMessage(null, MessageUtil.UPDATE_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
                    hideDialog = true;
                }
            }
        }
        getRequestContext().addCallbackParam("editMunicipalityHideDialog", hideDialog);
    }

    public void doDeleteMunicipality(final Municipality municipality) {
        boolean hideDialog = false;
        if (getIsCurrentRemovable()) {
            if (execute(() -> {
                municipalityService.delete(userData, municipality);
                mvAreaPickerController.update(AreaLevelEnum.MUNICIPALITY.getLevel(), null);
            })) {
                String[] summaryParams = {municipality.getName(), parentMvArea.getCountyName()};
                MessageUtil.buildFacesMessage(null, MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
                hideDialog = true;
            }
        } else {
            String[] summaryParams = {municipality.getName()};
            MessageUtil.buildFacesMessage(null, MessageUtil.REMOVE_NOT_ALLOWED_KEY, summaryParams, FacesMessage.SEVERITY_INFO);
            LOGGER.error("Municipality " + municipality.getName() + " with pk " + municipality.getPk() + " is not deletable.");
        }
        getRequestContext().addCallbackParam("editMunicipalityHideDialog", hideDialog);
    }

    public Municipality getNewMunicipality() {
        return newMunicipality;
    }

    public void setNewMunicipality(final Municipality newMunicipality) {
        this.newMunicipality = newMunicipality;
    }

    public Municipality getCurrentMunicipality() {
        return currentMunicipality;
    }

    public void setCurrentMunicipality(final Municipality currentMunicipality) {
        this.currentMunicipality = currentMunicipality;
    }

    public Locale getCurrentMunicipalityLocale() {
        return currentMunicipality != null ? municipalityService.getLocale(userData, currentMunicipality) : null;
    }

    public void setCurrentMunicipalityLocale(final Locale currentMunicipalityLocale) {
        if (currentMunicipality != null) {
            currentMunicipality.setLocale(currentMunicipalityLocale);
        }
    }

    public Locale getNewMunicipalityLocale() {
        return newMunicipality != null ? municipalityService.getLocale(userData, newMunicipality) : null;
    }

    public void setNewMunicipalityLocale(final Locale newMunicipalityLocale) {
        if (newMunicipality != null) {
            newMunicipality.setLocale(newMunicipalityLocale);
        }
    }

    public MvArea getCurrentMvArea() {
        return currentMvArea;
    }

    public MvArea getParentMvArea() {
        return parentMvArea;
    }

    public void setParentMvArea(final MvArea parentMvArea) {
        resetNewMunicipality();
        this.parentMvArea = parentMvArea;
    }

    private void resetNewMunicipality() {
        newMunicipality = new Municipality();
        newMunicipality.setId("");
        newMunicipality.setName("");
        newMunicipality.setElectronicMarkoffs(false);
        newMunicipality.setTechnicalPollingDistrictsAllowed(false);
        newMunicipality.setElectionCardText(null);
    }

    public boolean idExists(final County county, final Municipality municipality) {
        return municipalityService.findMunicipalityById(userData, county.getPk(), municipality.getId()) != null;
    }

    public boolean hasIdChanged(final Long oldMunicipalityPk, final Municipality newMunicipality) {
        Municipality origMunicipality = municipalityService.findByPk(oldMunicipalityPk);
        return !origMunicipality.getId().equalsIgnoreCase(newMunicipality.getId());
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
