package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.Displayable;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.common.configuration.model.local.Place;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.bakgrunnsjobb.service.BakgrunnsjobbService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;

public abstract class ConfigurationController extends BaseController {

    static final int SELECT_ID_LENGTH = 35;

    private static final long serialVersionUID = -6684899184519544819L;

    @Inject
    private UserDataController userDataController;
    @Inject
    private MessageProvider messageProvider;
    @Inject
    private BakgrunnsjobbService bakgrunnsjobbService;

    private ConfigurationMode mode = ConfigurationMode.READ;
    private LocalConfigurationController mainController;
    private String id;

    public boolean isEditable() {
        if (getMvArea() == null) {
            return false;
        }

        boolean requiresDoneBeforeEditOK = checkRequiresDoneBeforeEdit(false);

        if (userDataController.isOverrideAccess() && !isAreaApproved() && requiresDoneBeforeEditOK) {
            return true;
        }

        if (!userDataController.getUserAccess().isKonfigurasjonGrunnlagsdataRedigere() && !userDataController.isOverrideAccess()) {
            return false;
        }

        return isLocalStatusOk() && requiresDoneBeforeEditOK;
    }

    private boolean isAreaApproved() {
        return (isCountyLevel() && getMvArea().getCounty().isApprovedConfigurationStatus())
                || (isMunicipalityLevel() && getMvArea().getMunicipality().isApprovedConfigurationStatus());
    }

    private boolean isLocalStatusOk() {
        boolean localElectionStatus = userDataController.isLocalConfigurationStatus();
        boolean localCountyStatus = !isCountyLevel() || getMvArea().getCounty().isLocalConfigurationStatus();
        boolean localMunicipalityStatus = !isMunicipalityLevel() || getMvArea().getMunicipality().isLocalConfigurationStatus();
        return localElectionStatus && localCountyStatus && localMunicipalityStatus;
    }

    public void unlock() {
        saveDone(false);
    }

    public void unlockAndUpdateDOM() {
        unlock();
        FacesUtil.updateDom(Arrays.asList(
                "configurationPanel",
                "approve-form"));
    }

    public boolean isUseElectronicMarkoffsConfigured() {
        // True dersom det er konfigurert at man skal ha elektronisk manntall og konfig er ferdig for dette punktet.
        return getMunicipalityConfigStatus() != null && getMunicipalityConfigStatus().isUseElectronicMarkoffs()
                && getMunicipalityConfigStatus().isElectronicMarkoffs();
    }

    public String getConfirmDeleteMessage(Displayable displayable) {
        if (displayable == null) {
            return "?";
        }
        return MessageUtil.getDeleteConfirmMessage(displayable);
    }

    public String getSelectId(Place place) {
        return StringUtils.abbreviate(place.getId() + "-" + place.getName(), SELECT_ID_LENGTH);
    }

    public Button button(ButtonType type) {
        if (type == ButtonType.DONE) {

            boolean isEditable = userDataController.getUserAccess().isKonfigurasjonGrunnlagsdataGodkjenne() && isReadMode() && isEditable();
            return enabled(isEditable && !isDoneStatus() && canBeSetToDone() && checkRequiresDoneBeforeDone(false));
        }
        return notRendered();
    }

    public boolean isWriteMode() {
        return this.mode.equals(ConfigurationMode.UPDATE) || this.mode.equals(ConfigurationMode.CREATE);
    }

    public boolean isReadMode() {
        return this.mode.equals(ConfigurationMode.READ);
    }

    public void cancelWrite() {
        this.mode = ConfigurationMode.READ;
    }

    public void setUpdateMode() {
        this.mode = ConfigurationMode.UPDATE;
    }

    public ConfigurationMode getMode() {
        return this.mode;
    }

    public void setMode(ConfigurationMode mode) {
        this.mode = mode;
    }

    public String getIcon() {
        if (getAreaPath() != null && getAreaPath().isRootLevel()) {
            return "";
        }
        return isDoneStatus() ? "eva-icon-checkmark completed" : "eva-icon-warning";
    }

    /**
     * Metoden som kalles når man klikker på "Ferdig"-knappen for en fane
     */
    public void saveDone() {
        saveDone(true);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    boolean checkIsVoterNumbersGenerated() {
        //           om det er tilstrekkelig at manntallsnummergenerering er startet, eller om den må ha fullført uten feil
        return bakgrunnsjobbService.erManntallsnummergenereringStartetEllerFullfort(getUserData());
    }

    /**
     * @param finished Settes til true dersom fanen skal lagres som ferdigstilt
     * @return true dersom ferdigstatus ("doneStatus") endret seg som resultat av denne operasjonen
     */
    boolean saveDone(boolean finished) {
        this.setMode(ConfigurationMode.READ);

        if (isNewState(finished)) {
            setDoneStatus(finished);
            boolean ctrlsStateChanged = execute(() -> {
                mainController.saveConfigStatuses();
                setMode(ConfigurationMode.READ);
                if (!finished) {
                    unlockChildren();
                }
            });

            if (!ctrlsStateChanged) {
                setDoneStatus(!finished);
                return false;
            }
        }
        return true;
    }

    private boolean isNewState(boolean newState) {
        return newState != isDoneStatus();
    }

    <T extends ConfigurationController> T getController(Class<T> cls) {
        for (ConfigurationController ctrl : mainController.getControllers()) {
            if (cls.isAssignableFrom(ctrl.getClass())) {
                return (T) ctrl;
            }
        }
        return null;
    }

    boolean checkRequiresDoneBeforeEdit() {
        return checkRequiresDoneBeforeEdit(true);
    }

    boolean checkRequiresDoneBeforeEdit(boolean message) {
        for (Class<? extends ConfigurationController> cls : getRequiresDoneBeforeEdit()) {
            ConfigurationController ctrl = getController(cls);
            if (ctrl != null && !ctrl.isDoneStatus()) {
                if (message) {
                    MessageUtil.buildDetailMessage("@config.local.checkRequiresDoneBeforeEdit",
                            new String[]{ctrl.getName(), getName()},
                            FacesMessage.SEVERITY_INFO);
                }
                return false;
            }
        }
        return true;
    }

    boolean checkRequiresDoneBeforeDone() {
        return checkRequiresDoneBeforeDone(true);
    }

    boolean checkRequiresDoneBeforeDone(boolean message) {
        for (Class<? extends ConfigurationController> cls : getRequiresDoneBeforeDone()) {
            ConfigurationController ctrl = getController(cls);
            if (ctrl != null && !ctrl.isDoneStatus()) {
                if (message) {
                    MessageUtil.buildDetailMessage("@config.local.checkRequiresDoneBeforeDone",
                            new String[]{ctrl.getName(), this.getName()},
                            FacesMessage.SEVERITY_INFO);
                }
                return false;
            }
        }
        return true;
    }

    boolean isCountyLevel() {
        return mainController != null && mainController.isCountyLevel();
    }

    boolean isMunicipalityLevel() {
        return mainController != null && mainController.isMunicipalityLevel();
    }

    UserData getUserData() {
        return userDataController.getUserData();
    }

    UserDataController getUserDataController() {
        return userDataController;
    }

    MessageProvider getMessageProvider() {
        return messageProvider;
    }

    MvArea getMvArea() {
        return mainController.getMvArea();
    }

    AreaPath getAreaPath() {
        return mainController.getAreaPath();
    }

    boolean isHasBoroughs() {
        return mainController.isHasBoroughs();
    }

    CountyConfigStatus getCountyConfigStatus() {
        return mainController.getCountyStatus();
    }

    MunicipalityConfigStatus getMunicipalityConfigStatus() {
        return mainController.getMunicipalityStatus();
    }

    String getWithBaseId(String id) {
        return "configurationPanel:" + mainController.getActiveControllerIndex() + ":" + id;
    }

    IllegalStateException unsupportedLevel() {
        return new IllegalStateException("Invalid level " + getMvArea().getAreaLevel() + " found in " + getClass().getSimpleName());
    }

    public boolean isRequiresDone() {
        return true;
    }

    Class<? extends ConfigurationController>[] getRequiresDoneBeforeEdit() {
        return new Class[0];
    }

    Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
        return new Class[0];
    }

    LocalConfigurationController getMainController() {
        return mainController;
    }

    void setMainController(LocalConfigurationController mainController) {
        this.mainController = mainController;
    }

    private void unlockChildren() {
        final List<ConfigurationController> children = findChildren();
        final boolean newState = false;
        children.stream()
                .filter(child -> child.isNewState(newState) && child.saveDone(newState))
                .forEach(child -> {
                    String[] params = {child.getName()};
                    MessageUtil.buildDetailMessage("@config.local.childrenReopened", params, FacesMessage.SEVERITY_INFO);
                });
    }

    private List<ConfigurationController> findChildren() {
        return mainController.getControllers().stream().filter(ctrl -> ctrl.requires(this)).collect(Collectors.toList());
    }

    private boolean requires(ConfigurationController parent) {
        Class<? extends ConfigurationController>[] merged = ArrayUtils.addAll(
                getRequiresDoneBeforeDone(),
                getRequiresDoneBeforeEdit());
        if (merged != null) {
            for (Class<? extends ConfigurationController> cls : merged) {
                if (cls.isAssignableFrom(parent.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Abtract methods required for children
     */

    public abstract void init();

    public abstract ConfigurationView getView();

    public abstract String getName();

    /**
     * Er denne konfigurasjonsfanen tilgjengelig for denne brukeren i dette området?
     * Kan også brukes til å skule kategorier som er konfigurert til å ikke være i bruk
     * (feks. skanning-konfigurasjon, som kan konfigureres av/på i valggruppen)
     */
    abstract boolean hasAccess();

    abstract void setDoneStatus(boolean value);

    /**
     * Er denne konfigurasjonsfanen ferdig? Hvis ferdig, returnerer true, ellers false
     */
    abstract boolean isDoneStatus();

    /**
     * Kan denne konfigurasjonskategorien settes til ferdig? (feks.: er data skrevet inn og gyldige?)
     */
    abstract boolean canBeSetToDone();

}
