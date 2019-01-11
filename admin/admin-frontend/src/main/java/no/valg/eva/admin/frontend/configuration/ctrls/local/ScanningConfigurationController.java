package no.valg.eva.admin.frontend.configuration.ctrls.local;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.CountyService;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ScanningConfig;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.rbac.domain.model.Operator;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
@NoArgsConstructor
public class ScanningConfigurationController extends ConfigurationController {

    private MunicipalityService municipalityService;
    private CountyService countyService;
    private AdminOperatorService adminOperatorService;
    private ElectionGroupService electionGroupService;

    @Getter
    @Setter
    private Municipality municipality;
    @Getter
    @Setter
    private County county;
    @Getter
    @Setter
    private List<Operator> operatorsFound;
    @Getter
    @Setter
    private Operator operatorSelected;
    @Getter
    @Setter
    private String operatorSearchName;

    @Inject
    public ScanningConfigurationController(MunicipalityService municipalityService, CountyService countyService, AdminOperatorService adminOperatorService,
                                           ElectionGroupService electionGroupService) {
        this.municipalityService = municipalityService;
        this.countyService = countyService;
        this.adminOperatorService = adminOperatorService;
        this.electionGroupService = electionGroupService;
    }

    @Override
    public void init() {
        operatorsFound = new ArrayList<>();
        if (isCountyLevel()) {
            this.county = countyService.findByPkWithScanningConfig(getUserData(), getMvArea().getCounty().getPk());
            setMode(getConfigurationModeFor(county.getScanningConfig()));
        } else if (isMunicipalityLevel()) {
            this.municipality = municipalityService.findByPkWithScanningConfig(getMvArea().getMunicipality().getPk());
            setMode(getConfigurationModeFor(municipality.getScanningConfig()));
        } else {
            throw unsupportedLevel();
        }
    }

    private ConfigurationMode getConfigurationModeFor(ScanningConfig scanningConfig) {
        if (scanningConfig == null) {
            return ConfigurationMode.UPDATE;
        } else {
            return ConfigurationMode.READ;
        }
    }

    public ScanningConfig getScanningConfig() {
        if (isCountyLevel()) {
            return county.getOrCreateScanningConfig();
        } else if (isMunicipalityLevel()) {
            return municipality.getOrCreateScanningConfig();
        }
        throw unsupportedLevel();
    }

    @Override
    public ConfigurationView getView() {
        return ConfigurationView.SCANNING;
    }

    @Override
    public String getName() {
        return "@config.local.accordion.scanning.name";
    }

    @Override
    boolean hasAccess() {
        return (isCountyLevel() || isMunicipalityLevel()) && scanningIsEnabledOnElectionGroupLevel();
    }

    private boolean scanningIsEnabledOnElectionGroupLevel() {
        return electionGroupService.isScanningEnabled(getUserData());
    }

    @Override
    public Button button(ButtonType type) {
        if (type == ButtonType.EXECUTE_UPDATE) {
            return getSaveChangesButton();
        } else if (type == ButtonType.UPDATE) {
            return getEditButton();
        } else if (type == ButtonType.DONE) {
            return getDoneButton();
        }
        return super.button(type);
    }

    private Button getSaveChangesButton() {
        if (getMode() == ConfigurationMode.UPDATE) {
            return Button.renderedAndEnabled();
        } else {
            return Button.enabled(false);
        }
    }

    private Button getEditButton() {
        if (getMode() == ConfigurationMode.UPDATE) {
            return Button.enabled(false);
        } else {
            return Button.renderedAndEnabled();
        }
    }

    private Button getDoneButton() {
        if (getMode() == ConfigurationMode.READ && !isDoneStatus()) {
            return Button.renderedAndEnabled();
        } else {
            return Button.enabled(false);
        }
    }

    @Override
    public boolean isDoneStatus() {
        if (isCountyLevel()) {
            return getCountyConfigStatus().isScanning();
        } else if (isMunicipalityLevel()) {
            return getMunicipalityConfigStatus().isScanning();
        } else {
            throw unsupportedLevel();
        }
    }

    @Override
    void setDoneStatus(boolean value) {
        if (isCountyLevel()) {
            getCountyConfigStatus().setScanning(value);
        } else if (isMunicipalityLevel()) {
            getMunicipalityConfigStatus().setScanning(value);
        } else {
            throw unsupportedLevel();
        }
    }

    @Override
    public void saveDone() {
        execute(() -> {
            if (saveDone(true)) {
                MessageUtil.buildDetailMessage("@config.local.scanning.finishedMessage", FacesMessage.SEVERITY_INFO);
            }
            init();
        });
    }

    public void prepareUpdate() {
        saveDone(false);
        init();
        setMode(ConfigurationMode.UPDATE);
    }

    public void saveChanges() {
        saveDone(false);
        saveScanningConfig();
    }

    private void saveScanningConfig() {
        execute(() -> {
            if (isCountyLevel()) {
                UserData userData = getUserData();
                countyService.updateScanningConfiguration(userData, county);
            } else if (isMunicipalityLevel()) {
                UserData userData = getUserData();
                municipalityService.updateScanningConfiguration(userData, municipality);
            }
            MessageUtil.buildDetailMessage("@config.local.scanning.savedMessage", FacesMessage.SEVERITY_INFO);
        });
    }

    @Override
    public boolean isRequiresDone() {
        return false;
    }

    @Override
    boolean canBeSetToDone() {
        return getMode() == ConfigurationMode.READ;
    }

    @Override
    public boolean isEditable() {
        return getMode() == ConfigurationMode.UPDATE;
    }

    public void goToSearchForOperator() {
        operatorsFound = new ArrayList<>();
        setMode(ConfigurationMode.SEARCH);
    }

    public boolean isShowScanningConfigForm() {
        return getMode() == ConfigurationMode.READ || getMode() == ConfigurationMode.UPDATE;
    }

    public boolean isShowSearchForOperatorForm() {
        return getMode() == ConfigurationMode.SEARCH && operatorsFound.isEmpty();
    }

    public boolean isShowListOperatorResultsForm() {
        return getMode() == ConfigurationMode.SEARCH && !operatorsFound.isEmpty();
    }

    public void performOperatorSearch() {
        operatorsFound = findOperators(operatorSearchName);
        prepareNextStepAfterSearch(operatorsFound);
    }

    private List<Operator> findOperators(String operatorSearchName) {
        return adminOperatorService.operatorsInAreaByName(getUserData(), getMvArea().areaPath(), operatorSearchName);
    }

    private void prepareNextStepAfterSearch(List<Operator> operatorsFound) {
        if (operatorsFound.isEmpty()) {
            MessageUtil.buildDetailMessage("@config.local.scanning.search.no.matches.found", FacesMessage.SEVERITY_INFO);
        } else if (operatorsFound.size() == 1) {
            MessageUtil.buildDetailMessage("@config.local.scanning.search.one.match.found",
                    new String[]{operatorsFound.get(0).getFullName()}, FacesMessage.SEVERITY_INFO);
            mapOperatorDataToForm(operatorsFound.get(0));
            setMode(ConfigurationMode.UPDATE);
        } // The remaining case: We have found multiple search results. Nothing to do. Rendering of screen will handle this
    }

    private void mapOperatorDataToForm(Operator operator) {
        getScanningConfig().setResponsibleFullName(operator.getFullName());
        getScanningConfig().setResponsiblePhoneNumber(operator.getTelephoneNumber());
        getScanningConfig().setResponsibleEmail(operator.getEmail());
    }

    public void cancelOperatorSearch() {
        setMode(ConfigurationMode.UPDATE);
    }

    public void onOperatorSelected() {
        mapOperatorDataToForm(operatorSelected);
        setMode(ConfigurationMode.UPDATE);
    }
}
