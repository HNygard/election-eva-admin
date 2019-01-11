package no.valg.eva.admin.frontend.configuration.ctrls.local;

import lombok.NoArgsConstructor;
import no.evote.service.configuration.BoroughService;
import no.evote.service.configuration.CountyService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.cdi.BeanLookupSingleton;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.util.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.TabChangeEvent;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

@Named
@ViewScoped
@NoArgsConstructor
public class LocalConfigurationController extends KontekstAvhengigController {

    private static final String MESSAGE_PROPERTY_CONFIG_LOCAL_AREA_APPROVED = "@config.local.msgs.area_approved";

    private static final long serialVersionUID = -7851217091035590541L;

    // Injected
    private CountyService countyService;
    private MunicipalityService municipalityService;
    private BoroughService boroughService;
    private ElectionGroupService electionGroupService;

    private MunicipalityConfigStatus municipalityStatus;
    private CountyConfigStatus countyStatus;
    private List<ConfigurationController> configurationControllers = new ArrayList<>();
    private int activeControllerIndex = -1;
    private MvArea mvArea;
    private boolean hasBoroughs;
    private ElectionGroup electionGroup;
    private BeanLookupSingleton beanLookupSingleton;

    @Inject
    public LocalConfigurationController(
            CountyService countyService, MunicipalityService municipalityService, BoroughService boroughService,
            ElectionGroupService electionGroupService, BeanLookupSingleton beanLookupSingleton) {
        this.countyService = countyService;
        this.municipalityService = municipalityService;
        this.boroughService = boroughService;
        this.electionGroupService = electionGroupService;
        this.beanLookupSingleton = beanLookupSingleton;
    }

    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        if (getUserData().isElectionEventAdminUser()) {
            setup.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
        }
        return setup;
    }

    @Override
    public void initialized(Kontekst kontekst) {
        mvArea = getMvAreaService().findSingleByPath(kontekst.getValggeografiSti());
        if (mvArea == null) {
            goToContextPicker();
            return;
        }

        electionGroup = determineElectionGroup();
        if (electionGroup == null) {
            MessageUtil.buildMessageForClientId("main-messages", getMessageProvider().get("@config.local.msgs.one_election_group_only"), SEVERITY_ERROR);
            return;
        }

        calculateHasBoroughs();

        setupConfigurationControllers();

        if (!configurationControllers.isEmpty()) {
            calculateSelectedIndex();
            initializeController();
        }

        checkStatus();
    }

    public String getLock() {
        return isConfigurationApproved() ? "eva-icon-lock" : "eva-icon-unlocked";
    }

    public Button button(ButtonType type) {
        switch (type) {
            case APPROVE:
                return isConfigurationEditable() ? enabled(canBeApproved()) : notRendered();
            case REJECT:
                return isConfigurationApproved() && isKonfigurasjonGrunnlagsdataOppheve() ? enabled(canBeRejected()) : notRendered();
            default:
                return notRendered();
        }
    }

    private boolean isConfigurationEditable() {
        return isConfigurationEditableOnCountyLevel() || isConfigurationEditableOnMunicipalityLevel();
    }

    private boolean isConfigurationEditableOnMunicipalityLevel() {
        return isMunicipalityLevel() && isKonfigurasjonGrunnlagsdataRedigere();
    }

    private boolean isConfigurationEditableOnCountyLevel() {
        return isCountyLevel() && isKonfigurasjonGrunnlagsdataRedigere();
    }

    private boolean isConfigurationApproved() {
        return isConfigurationApprovedOnCountyLevel() || isConfigurationApprovedOnMunicipalityLevel();
    }

    private boolean isConfigurationApprovedOnCountyLevel() {
        return isCountyLevel() && mvArea.getCounty().isApprovedConfigurationStatus();
    }

    private boolean isConfigurationApprovedOnMunicipalityLevel() {
        return isMunicipalityLevel() && mvArea.getMunicipality().isApprovedConfigurationStatus();
    }

    public String approve() {
        if (canBeApproved()) {
            if (isCountyLevel()) {
                mvArea.setCounty(countyService.approve(getUserData(), mvArea.getCounty().getPk()));
            } else if (isMunicipalityLevel()) {
                mvArea.setMunicipality(municipalityService.approve(getUserData(), mvArea.getMunicipality().getPk()));
            } else {
                throw unsupportedLevel();
            }
            addGlobalMessage(MESSAGE_PROPERTY_CONFIG_LOCAL_AREA_APPROVED, FacesMessage.SEVERITY_INFO);
        }
        return null;
    }

    public String reject() {
        if (canBeRejected()) {
            if (isCountyLevel()) {
                mvArea.setCounty(countyService.reject(getUserData(), mvArea.getCounty().getPk()));
            } else if (isMunicipalityLevel()) {
                mvArea.setMunicipality(municipalityService.reject(getUserData(), mvArea.getMunicipality().getPk()));
            } else {
                throw unsupportedLevel();
            }
            addGlobalMessage("@config.local.msgs.area_rejected", FacesMessage.SEVERITY_INFO);
        }
        return null;
    }

    public void onTabChange(TabChangeEvent event) {
        setActiveControllerIndex(Integer.parseInt(event.getTab().getClientId().split(":")[1]));
        initializeController();
    }

    public List<ConfigurationController> getControllers() {
        return configurationControllers;
    }

    public MunicipalityService getMunicipalityService() {
        return municipalityService;
    }

    public int getActiveControllerIndex() {
        return activeControllerIndex;
    }

    public void setActiveControllerIndex(int index) {
        activeControllerIndex = index;
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(mvArea);
    }

    public <T extends ConfigurationController> T getController(Class<T> cls) {
        for (ConfigurationController ctrl : getControllers()) {
            if (cls.isAssignableFrom(ctrl.getClass())) {
                return (T) ctrl;
            }
        }
        return null;
    }

    public ElectionCardBaseConfigurationController getElectionCardCtrl() {
        return getController(ElectionCardBaseConfigurationController.class);
    }

    public ListProposalBaseConfigurationController getListProposalCtrl() {
        return getController(ListProposalBaseConfigurationController.class);
    }

    void saveConfigStatuses() {
        if (isCountyLevel()) {
            countyStatus = countyService.saveCountyConfigStatus(getUserData(), countyStatus);
        } else if (isMunicipalityLevel()) {
            municipalityStatus = municipalityService.saveMunicipalityConfigStatus(getUserData(), municipalityStatus,
                    electionGroup.getElectionGroupPath());
        }
        setupConfigurationControllers();
    }

    private boolean isKonfigurasjonGrunnlagsdataRedigere() {
        return getUserAccess().isKonfigurasjonGrunnlagsdataRedigere();
    }

    private boolean isKonfigurasjonOpptellingsvalgstyrer() {
        return getUserAccess().isKonfigurasjonOpptellingsvalgstyrer();
    }

    private boolean isKonfigurasjonGrunnlagsdataOppheve() {
        return getUserAccess().isKonfigurasjonGrunnlagsdataOppheve();
    }

    CountyConfigStatus getCountyStatus() {
        return countyStatus;
    }

    MunicipalityConfigStatus getMunicipalityStatus() {
        return municipalityStatus;
    }

    boolean isCountyLevel() {
        return mvArea != null && mvArea.isCountyLevel();
    }

    boolean isMunicipalityLevel() {
        return mvArea != null && mvArea.isMunicipalityLevel();
    }

    private void addGlobalMessage(String message, FacesMessage.Severity severity) {
        MessageUtil.buildFacesMessage(getFacesContext(), "main-messages", message, null, severity);
    }

    MvArea getMvArea() {
        return mvArea;
    }

    AreaPath getAreaPath() {
        return mvArea == null ? null : AreaPath.from(mvArea.getAreaPath());
    }

    boolean isHasBoroughs() {
        return hasBoroughs;
    }

    ConfigurationController lookup(Class<? extends ConfigurationController> cls) {
        return beanLookupSingleton.lookup(cls);
    }

    ElectionGroup getElectionGroup() {
        return electionGroup;
    }

    private void initializeController() {
        if (getActiveControllerIndex() >= 0) {
            ConfigurationController ctrl = configurationControllers.get(getActiveControllerIndex());
            if (ctrl.checkRequiresDoneBeforeEdit()) {
                ctrl.checkRequiresDoneBeforeDone();
            }
            execute(ctrl::init);
        }
    }

    private void calculateSelectedIndex() {
        String tab = getRequestParameter("tab");
        if (!StringUtils.isEmpty(tab)) {
            for (int i = 0; i < configurationControllers.size(); i++) {
                if (tab.equals(configurationControllers.get(i).getId())) {
                    setActiveControllerIndex(i);
                    break;
                }
            }
        }
        if (getActiveControllerIndex() == -1) {
            // We have no index or index is out of bounds
            setActiveControllerIndex(-1);
            boolean noDone = true;
            int index = 0;
            for (ConfigurationController ctrl : configurationControllers) {
                if (ctrl.isDoneStatus()) {
                    noDone = false;
                } else if (getActiveControllerIndex() == -1) {
                    setActiveControllerIndex(index);
                }
                index++;
            }
            if (noDone) {
                setActiveControllerIndex(-1);
            }
        }

    }

    private void loadCountyStatus() {
        if (isCountyLevel()) {
            countyStatus = countyService.findCountyStatusByArea(getUserData(), getAreaPath());
        } else {
            countyStatus = null;
        }
    }

    private void loadMunicipalityStatus() {
        if (isMunicipalityLevel()) {
            municipalityStatus = municipalityService.findMunicipalityStatusByArea(getUserData(), getAreaPath());
        } else {
            municipalityStatus = null;
        }
    }

    private void setupConfigurationControllers() {

        // list of controllers that localConfiguration uses
        List<Class<? extends ConfigurationController>> controllerClasses = getAvailableControllers();

        configurationControllers.clear();
        loadCountyStatus();
        loadMunicipalityStatus();

        if (isKonfigurasjonGrunnlagsdataRedigere() || isKonfigurasjonOpptellingsvalgstyrer()) {
            for (Class<? extends ConfigurationController> controllerClass : controllerClasses) {
                ConfigurationController controller = lookup(controllerClass);
                controller.setMainController(this);
                controller.setId(getControllerId(controller));
                // determine if user has access to controller
                if (controller.hasAccess()) {
                    configurationControllers.add(controller);
                }
            }
        }
    }

    protected List<Class<? extends ConfigurationController>> getAvailableControllers() {
        return Arrays.asList(
                LanguageConfigurationController.class,
                ElectronicMarkoffsConfigurationController.class,
                ListProposalConfigurationController.class,
                BoroughsListProposalConfigurationController.class,
                ReportCountCategoriesConfigurationController.class,
                ScanningConfigurationController.class,
                AdvancePollingPlacesConfigurationController.class,
                TechnicalPollingDistrictConfigurationController.class,
                ElectionDayPollingPlacesConfigurationController.class,
                PollingStationConfigurationController.class,
                ParentPollingDistrictConfigurationController.class,
                ElectionCardConfigurationController.class,
                BoroughsElectionCardConfigurationController.class,
                FylkesvalgstyreConfigurationController.class,
                ValgstyreConfigurationController.class,
                OpptellingsvalgstyreConfigurationController.class,
                StemmestyreConfigurationController.class,
                BoroughsStemmestyreConfigurationController.class);
    }

    private String getControllerId(ConfigurationController ctrl) {
        String result = ctrl.getClass().getSimpleName();
        int index = result.indexOf("ConfigurationController");
        return index == -1 ? result : result.substring(0, index).toLowerCase(Locale.ENGLISH);
    }

    void checkStatus() {
        if (getUserDataController().isLocalConfigurationStatus()) {
            if (isCountyLevel()) {
                if (mvArea.getCounty().isCentralConfigurationStatus() && !getUserDataController().isCentralConfigurationStatus()) {
                    addGlobalMessage("@config.local.msgs.area_in_central_mode", FacesMessage.SEVERITY_WARN);
                } else if (!mvArea.getCounty().isLocalConfigurationStatus()) {
                    addGlobalMessage(MESSAGE_PROPERTY_CONFIG_LOCAL_AREA_APPROVED, FacesMessage.SEVERITY_INFO);
                }
            } else if (isMunicipalityLevel()) {
                if (mvArea.getMunicipality().isCentralConfigurationStatus() && !getUserDataController().isCentralConfigurationStatus()) {
                    addGlobalMessage("@config.local.msgs.area_in_central_mode", FacesMessage.SEVERITY_WARN);
                } else if (!mvArea.getMunicipality().isLocalConfigurationStatus()) {
                    addGlobalMessage(MESSAGE_PROPERTY_CONFIG_LOCAL_AREA_APPROVED, FacesMessage.SEVERITY_INFO);
                }
            }
        } else {
            addGlobalMessage("@config.local.msgs.election_not_in_local_mode", FacesMessage.SEVERITY_WARN);
        }
    }

    private boolean canBeApproved() {
        // Check that all controllers are set to done
        if (isOngoing()) {
            return false;
        }
        if (isCountyLevel()) {
            return isLocalConfigurationStatusOnCountyLevel() || isOverrideAccessOnCountyLevel();
        }
        if (isMunicipalityLevel()) {
            return isLocalConfigurationStatusOnMunicipalityLevel() || isOverrideAccessOnMunicipalityLevel();
        }
        throw unsupportedLevel();
    }

    private boolean isOngoing() {
        for (ConfigurationController ctrl : configurationControllers) {
            if (ctrl.isRequiresDone() && !ctrl.isDoneStatus()) {
                return true;
            }
        }
        return false;
    }

    private boolean isLocalConfigurationStatusOnCountyLevel() {
        return getUserDataController().isLocalConfigurationStatus() && mvArea.getCounty().isLocalConfigurationStatus();
    }

    private boolean isOverrideAccessOnCountyLevel() {
        return getUserDataController().isOverrideAccess() && !mvArea.getCounty().isApprovedConfigurationStatus();
    }

    private boolean isLocalConfigurationStatusOnMunicipalityLevel() {
        return getUserDataController().isLocalConfigurationStatus() && mvArea.getMunicipality().isLocalConfigurationStatus();
    }

    private boolean isOverrideAccessOnMunicipalityLevel() {
        return getUserDataController().isOverrideAccess() && !mvArea.getMunicipality().isApprovedConfigurationStatus();
    }

    private boolean canBeRejected() {
        if (isCountyLevel()) {
            return mvArea.getCounty().isApprovedConfigurationStatus();
        } else if (isMunicipalityLevel()) {
            return mvArea.getMunicipality().isApprovedConfigurationStatus();
        } else {
            throw unsupportedLevel();
        }
    }

    private IllegalStateException unsupportedLevel() {
        return new IllegalStateException("Invalid level " + mvArea.getAreaLevel() + " found in " + getClass().getSimpleName());
    }

    private void calculateHasBoroughs() {
        List<Borough> boroughs = getMvArea().getMunicipality() == null
                ? new ArrayList<>()
                : boroughService.findByMunicipality(getUserData(), getMvArea().getMunicipality().getPk());
        hasBoroughs = boroughs.size() > 1 || (boroughs.size() == 1 && !boroughs.get(0).isMunicipality1());

    }

    private ElectionGroup determineElectionGroup() {
        List<ElectionGroup> electionGroups = electionGroupService.getElectionGroups(getUserData());
        if (electionGroups.size() != 1) {
            return null;
        }
        return electionGroups.get(0);
    }
}
