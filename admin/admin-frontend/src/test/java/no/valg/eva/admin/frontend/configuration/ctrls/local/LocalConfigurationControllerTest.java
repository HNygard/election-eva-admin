package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.service.configuration.CountyService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.primefaces.event.TabChangeEvent;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.LOCAL_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class LocalConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

    @Test
    public void init_withAdminUserAndNoAreaPathParameter_redirectsToPicker() throws Exception {
        LocalConfigurationController ctrl = ctrl();
        when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
        getServletContainer().setRequestURI("/my/uri");
        when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");

        ctrl.init();

        verify(getFacesContextMock().getExternalContext())
                .redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|2,3][side|uri|/my/uri]");
    }

    @Test
    public void init_withAdminUserAndInvalidAreaPathParameter_redirectsToPicker() throws Exception {
        LocalConfigurationController ctrl = ctrl();
        when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
        getServletContainer().setRequestURI("/my/uri");
        getServletContainer().setRequestParameter("ctx", data(COUNTY).serialize());
        when(getInjectMock(MvAreaService.class).findSingleByPath(anyString(), eq(COUNTY))).thenReturn(null);
        when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");

        ctrl.init();

        verify(getFacesContextMock().getExternalContext())
                .redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|2,3][side|uri|/my/uri]");
    }

    @Test
    public void init_withValidConfigAndTwoActive_setupControllers() throws Exception {
        MyLocalConfigurationController ctrl = ctrl();
        getServletContainer().setRequestParameter("tab", null);
        AreaPath areaPath = mvArea(MUNICIPALITY).getValue().areaPath();
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(areaPath);
        when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
        when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataRedigere()).thenReturn(true);
        ctrl.init();
        when(ctrl.getLookups().get(ReportCountCategoriesConfigurationController.class.getName()).hasAccess()).thenReturn(true);
        when(ctrl.getLookups().get(ReportCountCategoriesConfigurationController.class.getName()).isDoneStatus()).thenReturn(true);
        when(ctrl.getLookups().get(AdvancePollingPlacesConfigurationController.class.getName()).hasAccess()).thenReturn(true);
        when(ctrl.getLookups().get(AdvancePollingPlacesConfigurationController.class.getName()).checkRequiresDoneBeforeEdit()).thenReturn(true);
        when(getInjectMock(CountyService.class).findCountyStatusByArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(null);

        ctrl.init();

        assertThat(ctrl.getMvArea()).isNotNull();
        assertThat(ctrl.getActiveControllerIndex()).isEqualTo(1);
        assertThat(ctrl.getControllers()).hasSize(2);
        verify(ctrl.getLookups().get(ReportCountCategoriesConfigurationController.class.getName()), atLeastOnce()).setMainController(
                any(LocalConfigurationController.class));
    }

    @Test
    public void init_withValidConfigAndOneActive_setupControllers() throws Exception {
        MyLocalConfigurationController ctrl = ctrl();
        getServletContainer().setRequestParameter("tab", "advancepollingplaces");
        AreaPath areaPath = mvArea(MUNICIPALITY).getValue().areaPath();
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(areaPath);
        when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
        when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataRedigere()).thenReturn(true);
        ctrl.init();
        when(ctrl.getLookups().get(AdvancePollingPlacesConfigurationController.class.getName()).hasAccess()).thenReturn(true);
        when(getInjectMock(MunicipalityService.class).findMunicipalityStatusByArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(null);

        ctrl.init();

        verify(ctrl.getLookups().get(AdvancePollingPlacesConfigurationController.class.getName()), atLeastOnce()).setMainController(
                any(LocalConfigurationController.class));
    }

    @Test
    public void init_withCanBeApproved_verifyActiveIndex() throws Exception {
        LocalConfigurationController ctrl = ctrl();
        getServletContainer().setRequestParameter("index", null);
        MvArea mvArea = mvArea(MUNICIPALITY).getValue();
        // when(getInjectMock(UserDataController.class).getOperatorArea()).thenReturn(mvArea);
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_MUNICIPALITY);
        when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
        stub_canBeApproved(mvArea, true);

        ctrl.init();

        assertThat(ctrl.getActiveControllerIndex()).isEqualTo(-1);
    }

    @Test(dataProvider = "buttonApproved")
    public void buttonApproved_withDataProvider_verifyExpected(AreaPath path, boolean hasAccess, boolean canBeApproved, boolean isRendered, boolean isEnabled)
            throws Exception {
        LocalConfigurationController ctrl = ctrl();
        MvArea mvArea = mvArea(path).getValue();
        mockFieldValue("mvArea", mvArea);
        when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataRedigere()).thenReturn(hasAccess);
        stub_canBeApproved(mvArea, canBeApproved);
        mockFieldValue("configurationControllers", new ArrayList<>());

        Button button = ctrl.button(ButtonType.APPROVE);

        assertThat(button.isRendered()).isEqualTo(isRendered);
        assertThat(button.isDisabled()).isEqualTo(!isEnabled);
    }

    @DataProvider(name = "buttonApproved")
    public Object[][] buttonApproved() {
        return new Object[][]{
                {COUNTY, true, true, true, true},
                {COUNTY, false, true, false, false},
                {COUNTY, true, false, true, false},
                {MUNICIPALITY, true, true, true, true},
                {MUNICIPALITY, false, true, false, false},
                {MUNICIPALITY, true, false, true, false}
        };
    }

    @Test(dataProvider = "buttonReject")
    public void buttonReject_withDataProvider_verifyExpected(AreaPath path, boolean hasAccess, boolean canBeRejected, boolean isRendered, boolean isEnabled)
            throws Exception {
        LocalConfigurationController ctrl = ctrl();
        MvArea mvArea = mvArea(path).getValue();
        mockFieldValue("mvArea", mvArea);
        when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataOppheve()).thenReturn(hasAccess);
        stub_canBeRejected(mvArea, canBeRejected);

        Button button = ctrl.button(ButtonType.REJECT);

        assertThat(button.isRendered()).isEqualTo(isRendered);
        assertThat(button.isDisabled()).isEqualTo(!isEnabled);
    }

    @DataProvider(name = "buttonReject")
    public Object[][] buttonReject() {
        return new Object[][]{
                {COUNTY, true, true, true, true},
                {COUNTY, false, true, false, false},
                {COUNTY, true, false, false, false},
                {MUNICIPALITY, true, true, true, true},
                {MUNICIPALITY, false, true, false, false},
                {MUNICIPALITY, true, false, false, false}
        };
    }

    @Test(dataProvider = "approve")
    public void approve_withDataProvider_verifyExpected(AreaPath path) throws Exception {
        LocalConfigurationController ctrl = ctrl();
        MvArea mvArea = mvArea(path).getValue();
        mockFieldValue("mvArea", mvArea);
        stub_canBeApproved(mvArea, true);
        mockFieldValue("configurationControllers", new ArrayList<>());

        ctrl.approve();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.msgs.area_approved");
        if (mvArea.isCountyLevel()) {
            verify(getInjectMock(CountyService.class)).approve(eq(getUserDataMock()), anyLong());
        } else {
            verify(getInjectMock(MunicipalityService.class)).approve(eq(getUserDataMock()), anyLong());
        }
    }

    @DataProvider(name = "approve")
    public Object[][] approve() {
        return new Object[][]{
                {COUNTY},
                {MUNICIPALITY}
        };
    }

    @Test(dataProvider = "reject")
    public void reject_withDataProvider_verifyExpected(AreaPath path) throws Exception {
        LocalConfigurationController ctrl = ctrl();
        MvArea mvArea = mvArea(path).getValue();
        mockFieldValue("mvArea", mvArea);
        stub_canBeRejected(mvArea, true);

        ctrl.reject();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.msgs.area_rejected");
        if (mvArea.isCountyLevel()) {
            verify(getInjectMock(CountyService.class)).reject(eq(getUserDataMock()), anyLong());
        } else {
            verify(getInjectMock(MunicipalityService.class)).reject(eq(getUserDataMock()), anyLong());
        }
    }

    @DataProvider(name = "reject")
    public Object[][] reject() {
        return new Object[][]{
                {COUNTY},
                {MUNICIPALITY}
        };
    }

    @Test
    public void onTabChange_withOneController_verifyState() throws Exception {
        LocalConfigurationController ctrl = ctrl();
        mockFieldValue("configurationControllers", new ArrayList<>());
        ctrl.getControllers().add(createMock(AdvancePollingPlacesConfigurationController.class));
        TabChangeEvent event = createMock(TabChangeEvent.class);
        when(event.getTab().getClientId()).thenReturn("tab:0:something");
        mockFieldValue("mvArea", mvArea(COUNTY).getValue());

        ctrl.onTabChange(event);

        assertThat(ctrl.getActiveControllerIndex()).isEqualTo(0);
    }

    @Test(dataProvider = "checkStatus")
    public void checkStatus_withDataProvider_verifyMessage(AreaPath path, ElectionEventStatusEnum eventStatus, MunicipalityStatusEnum areaStatus,
                                                           FacesMessage.Severity severity, String msg) throws Exception {
        LocalConfigurationController ctrl = ctrl();
        MvArea mvArea = mvArea(path).getValue();
        mockFieldValue("mvArea", mvArea);
        stub_eventStatus(eventStatus);
        stub_areaStatus(mvArea, areaStatus);

        ctrl.checkStatus();

        assertFacesMessage(severity, msg);
    }

    @DataProvider(name = "checkStatus")
    public Object[][] checkStatus() {
        return new Object[][]{
                {COUNTY, CENTRAL_CONFIGURATION, MunicipalityStatusEnum.CENTRAL_CONFIGURATION, FacesMessage.SEVERITY_WARN,
                        "@config.local.msgs.election_not_in_local_mode"},
                {COUNTY, LOCAL_CONFIGURATION, MunicipalityStatusEnum.CENTRAL_CONFIGURATION, FacesMessage.SEVERITY_WARN,
                        "@config.local.msgs.area_in_central_mode"},
                {COUNTY, LOCAL_CONFIGURATION, MunicipalityStatusEnum.APPROVED_CONFIGURATION, FacesMessage.SEVERITY_INFO,
                        "@config.local.msgs.area_approved"},
                {MUNICIPALITY, CENTRAL_CONFIGURATION, MunicipalityStatusEnum.CENTRAL_CONFIGURATION, FacesMessage.SEVERITY_WARN,
                        "@config.local.msgs.election_not_in_local_mode"},
                {MUNICIPALITY, LOCAL_CONFIGURATION, MunicipalityStatusEnum.CENTRAL_CONFIGURATION, FacesMessage.SEVERITY_WARN,
                        "@config.local.msgs.area_in_central_mode"},
                {MUNICIPALITY, LOCAL_CONFIGURATION, MunicipalityStatusEnum.APPROVED_CONFIGURATION, FacesMessage.SEVERITY_INFO,
                        "@config.local.msgs.area_approved"}
        };
    }

    @Test
    public void saveConfigStatuses_withCountyLevel_savesCountyStatuses() throws Exception {
        LocalConfigurationController ctrl = ctrl();
        mockFieldValue("mvArea", mvArea(COUNTY).getValue());
        List<ConfigurationController> configurationControllers = mockField("configurationControllers", List.class);

        ctrl.saveConfigStatuses();

        verify(getInjectMock(CountyService.class)).saveCountyConfigStatus(eq(getUserDataMock()), any());
        verify(configurationControllers).clear();
    }

    @Test
    public void saveConfigStatuses_withMunicipalityLevel_savesMunicipalityStatuses() throws Exception {
        LocalConfigurationController ctrl = ctrl();
        mockFieldValue("mvArea", mvArea(MUNICIPALITY).getValue());
        mockFieldValue("electionGroup", electionGroup());

        ctrl.saveConfigStatuses();

        verify(getInjectMock(MunicipalityService.class)).saveMunicipalityConfigStatus(eq(getUserDataMock()),
                any(), any(ElectionPath.class));
    }

    private void stub_eventStatus(ElectionEventStatusEnum eventStatus) {
        when(getInjectMock(UserDataController.class).isLocalConfigurationStatus()).thenReturn(eventStatus == LOCAL_CONFIGURATION);
        when(getInjectMock(UserDataController.class).isCentralConfigurationStatus()).thenReturn(eventStatus == CENTRAL_CONFIGURATION);
    }

    private void stub_areaStatus(MvArea mvArea, MunicipalityStatusEnum areaStatus) {
        when(mvArea.getCounty().isCentralConfigurationStatus()).thenReturn(areaStatus == MunicipalityStatusEnum.CENTRAL_CONFIGURATION);
        when(mvArea.getCounty().isLocalConfigurationStatus()).thenReturn(areaStatus == MunicipalityStatusEnum.LOCAL_CONFIGURATION);
        when(mvArea.getCounty().isApprovedConfigurationStatus()).thenReturn(areaStatus == MunicipalityStatusEnum.APPROVED_CONFIGURATION);
        if (mvArea.isMunicipalityLevel()) {
            when(mvArea.getMunicipality().isCentralConfigurationStatus()).thenReturn(areaStatus == MunicipalityStatusEnum.CENTRAL_CONFIGURATION);
            when(mvArea.getMunicipality().isLocalConfigurationStatus()).thenReturn(areaStatus == MunicipalityStatusEnum.LOCAL_CONFIGURATION);
            when(mvArea.getMunicipality().isApprovedConfigurationStatus()).thenReturn(areaStatus == MunicipalityStatusEnum.APPROVED_CONFIGURATION);
        }
    }

    private void stub_canBeApproved(MvArea mvArea, boolean canBeApproved) {
        when(getInjectMock(UserDataController.class).isLocalConfigurationStatus()).thenReturn(canBeApproved);
        when(mvArea.getCounty().isLocalConfigurationStatus()).thenReturn(canBeApproved);
        when(mvArea.getCounty().isApprovedConfigurationStatus()).thenReturn(!canBeApproved);
        if (mvArea.isMunicipalityLevel()) {
            when(mvArea.getMunicipality().isLocalConfigurationStatus()).thenReturn(canBeApproved);
            when(mvArea.getMunicipality().isApprovedConfigurationStatus()).thenReturn(!canBeApproved);
        }
        if (canBeApproved) {
            isOverrideAccess();
        }
    }

    private void stub_canBeRejected(MvArea mvArea, boolean canBeRejected) {
        when(mvArea.getCounty().isApprovedConfigurationStatus()).thenReturn(canBeRejected);
        if (mvArea.isMunicipalityLevel()) {
            when(mvArea.getMunicipality().isApprovedConfigurationStatus()).thenReturn(canBeRejected);
        }
    }

    private MyLocalConfigurationController ctrl() throws Exception {
        MyLocalConfigurationController result = initializeMocks(new MyLocalConfigurationController());
        when(getInjectMock(ElectionGroupService.class).getElectionGroups(getUserDataMock())).thenReturn(Collections.singletonList(electionGroup()));
        return result;
    }

    private ElectionGroup electionGroup() {
        ElectionGroup result = new ElectionGroup(ELECTION_PATH_ELECTION_EVENT);
        result.setId("01");
        return result;
    }

    private Kontekst data(AreaPath areaPath) {
        Kontekst data = new Kontekst();
        data.setValggeografiSti(ValggeografiSti.fra(areaPath));
        return data;
    }

    public static class MyLocalConfigurationController extends LocalConfigurationController {

        private Map<String, ConfigurationController> lookups = new HashMap<>();

        public Map<String, ConfigurationController> getLookups() {
            return lookups;
        }

        @Override
        ConfigurationController lookup(Class<? extends ConfigurationController> cls) {
            ConfigurationController result = lookups.get(cls.getName());
            if (result == null) {
                result = mock(cls, RETURNS_DEEP_STUBS);
                lookups.put(cls.getName(), result);
            }
            return result;
        }
    }
}

