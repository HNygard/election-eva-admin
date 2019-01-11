package no.valg.eva.admin.frontend.configuration.ctrls.local;

import lombok.Builder;
import lombok.Data;
import no.evote.service.configuration.CountyService;
import no.valg.eva.admin.common.AreaPath;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScanningConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

    @Test(dataProvider = "testData")
    public void init_municipalityLevel(ScanningConfig scanningConfig, ConfigurationMode expectedConfigurationMode) throws Exception {
        ScanningConfigurationController ctrl = ctrl(MUNICIPALITY);
        Municipality municipalityWithoutScanningConfig = Municipality.builder().scanningConfig(scanningConfig).build();
        when(getInjectMock(MunicipalityService.class).findByPkWithScanningConfig(anyLong())).thenReturn(municipalityWithoutScanningConfig);

        ctrl.init();

        assertThat(ctrl.getView()).isSameAs(ConfigurationView.SCANNING);
        assertThat(ctrl.getMunicipality()).isEqualTo(municipalityWithoutScanningConfig);
        assertThat(ctrl.getMode()).isEqualTo(expectedConfigurationMode);
        assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.scanning.name");
    }

    @DataProvider
    private Object[][] testData() {
        return new Object[][]{
                {new ScanningConfig(), ConfigurationMode.READ},
                {null, ConfigurationMode.UPDATE},
        };
    }

    private ScanningConfigurationController ctrl(AreaPath area) throws Exception {
        return ctrl(ScanningConfigurationController.class, area);
    }

    @Test(dataProvider = "testData")
    public void init_countyLevel(ScanningConfig scanningConfig, ConfigurationMode expectedConfigurationMode) throws Exception {
        ScanningConfigurationController ctrl = ctrl(COUNTY);
        County countyWithoutScanningConfig = County.builder().scanningConfig(scanningConfig).build();
        when(getInjectMock(CountyService.class).findByPkWithScanningConfig(eq(getUserDataMock()), anyLong())).thenReturn(countyWithoutScanningConfig);

        ctrl.init();

        assertThat(ctrl.getView()).isSameAs(ConfigurationView.SCANNING);
        assertThat(ctrl.getCounty()).isEqualTo(countyWithoutScanningConfig);
        assertThat(ctrl.getMode()).isEqualTo(expectedConfigurationMode);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void init_otherLevel_fails() throws Exception {
        ScanningConfigurationController ctrl = ctrl(ROOT);
        ctrl.init();
    }

    @Test
    public void getScanningConfig_countyLevel() throws Exception {
        ScanningConfigurationController ctrl = ctrl(COUNTY);
        ScanningConfig scanningConfig = new ScanningConfig();
        ctrl.setCounty(County.builder().scanningConfig(scanningConfig).build());

        ScanningConfig returnedScanningConfig = ctrl.getScanningConfig();

        assertThat(returnedScanningConfig).isEqualTo(scanningConfig);
    }

    @Test
    public void getScanningConfig_municipalityLevel() throws Exception {
        ScanningConfigurationController ctrl = ctrl(MUNICIPALITY);
        ScanningConfig scanningConfig = new ScanningConfig();
        ctrl.setMunicipality(Municipality.builder().scanningConfig(scanningConfig).build());

        ScanningConfig returnedScanningConfig = ctrl.getScanningConfig();

        assertThat(returnedScanningConfig).isEqualTo(scanningConfig);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void getScanningConfig_invalidLevel() throws Exception {
        ScanningConfigurationController ctrl = ctrl(ROOT);
        ctrl.getScanningConfig();
    }

    @Test(dataProvider = "hasAccessTestData")
    public void hasAccess(AreaPath areaPath, boolean enabledInElectionGroup, boolean expectedAccess) throws Exception {
        ScanningConfigurationController ctrl = ctrl(areaPath);
        when(getInjectMock(ElectionGroupService.class).isScanningEnabled(any())).thenReturn(enabledInElectionGroup);

        boolean actualAccess = ctrl.hasAccess();

        assertThat(actualAccess).isEqualTo(expectedAccess);
    }

    @DataProvider
    private Object[][] hasAccessTestData() {
        return new Object[][]{
                {COUNTY, true, true},
                {COUNTY, false, false},
                {MUNICIPALITY, true, true},
                {MUNICIPALITY, false, false},
                {ROOT, true, false},
                {ROOT, false, false}
        };
    }

    @Test(dataProvider = "buttonTestData")
    public void button(ButtonType buttonType, ConfigurationMode mode, AreaPath areaPath, Button excpectedButton) throws Exception {
        ScanningConfigurationController ctrl = ctrl(areaPath);
        ctrl.setMode(mode);

        Button actualButton = ctrl.button(buttonType);

        assertThat(actualButton).isEqualTo(excpectedButton);
    }

    @DataProvider
    private Object[][] buttonTestData() {
        return new Object[][]{
                {ButtonType.EXECUTE_UPDATE, ConfigurationMode.UPDATE, COUNTY, Button.enabled(true)},
                {ButtonType.UPDATE, ConfigurationMode.UPDATE, COUNTY, Button.enabled(false)},
                {ButtonType.DONE, ConfigurationMode.UPDATE, COUNTY, Button.enabled(false)},
                {ButtonType.EXECUTE_UPDATE, ConfigurationMode.READ, COUNTY, Button.enabled(false)},
                {ButtonType.UPDATE, ConfigurationMode.READ, COUNTY, Button.enabled(true)},
                {ButtonType.DONE, ConfigurationMode.READ, COUNTY, Button.enabled(true)},
                {ButtonType.EXECUTE_UPDATE, ConfigurationMode.SEARCH, COUNTY, Button.enabled(false)},
                {ButtonType.UPDATE, ConfigurationMode.SEARCH, COUNTY, Button.enabled(true)},
                {ButtonType.DONE, ConfigurationMode.SEARCH, COUNTY, Button.enabled(false)},
                {ButtonType.APPROVE, ConfigurationMode.SEARCH, COUNTY, Button.notRendered()},
        };
    }

    @Test(dataProvider = "prepareUpdateTestData")
    public void prepareUpdate(AreaPath areaPath) throws Exception {
        ScanningConfigurationController ctrl = ctrl(areaPath);

        ctrl.prepareUpdate();

        assertThat(ctrl.isDoneStatus()).isEqualTo(false);
        assertThat(ctrl.getMode()).isEqualTo(ConfigurationMode.UPDATE);
    }

    @DataProvider
    private Object[][] prepareUpdateTestData() {
        return new Object[][]{
                {COUNTY},
                {MUNICIPALITY},
        };
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void prepareUpdate_invalidLevel() throws Exception {
        ScanningConfigurationController ctrl = ctrl(ROOT);
        ctrl.prepareUpdate();
    }

    @Test
    public void saveDone() throws Exception {
        ScanningConfigurationController ctrl = ctrl(COUNTY);

        ctrl.saveDone();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.scanning.finishedMessage");
    }


    @Test(dataProvider = "saveChangesTestData")
    public void saveChanges(AreaPath areaPath) throws Exception {
        ScanningConfigurationController ctrl = ctrl(areaPath);

        ctrl.saveChanges();

        assertThat(ctrl.isDoneStatus()).isEqualTo(false);
        assertThat(ctrl.getMode()).isEqualTo(ConfigurationMode.READ);
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.scanning.savedMessage");
        if (areaPath.isMunicipalityLevel()) verify(getInjectMock(MunicipalityService.class)).updateScanningConfiguration(any(), any());
        if (areaPath.isCountyLevel()) verify(getInjectMock(CountyService.class)).updateScanningConfiguration(any(), any());
    }

    @DataProvider
    private Object[][] saveChangesTestData() {
        return new Object[][]{
                {COUNTY},
                {MUNICIPALITY},
        };
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void saveChanges_invalidLevel() throws Exception {
        ScanningConfigurationController ctrl = ctrl(ROOT);
        ctrl.saveChanges();
    }

    @Test
    public void isShowXxx_searchWithMultipleOperatorsFound() throws Exception {
        DialogTestData data = DialogTestData.builder().area(COUNTY).ctrl(ctrl(COUNTY)).build().init();

        verifyCanSeeScanningConfigurationForm(data.getCtrl());
        data.getCtrl().goToSearchForOperator();
        verfiyCanSeeOperatorSearchForm(data.getCtrl());
        data.getCtrl().setOperatorSearchName(data.getSearchString());
        when(getInjectMock(AdminOperatorService.class)
                .operatorsInAreaByName(getUserDataMock(), data.getArea(), data.getSearchString()))
                .thenReturn(asList(data.getOperator1(), data.getOperator2()));
        data.getCtrl().performOperatorSearch();
        verifyCanSeeSearchResults(data.getCtrl());
        data.getCtrl().setOperatorSelected(data.getOperator1());
        data.getCtrl().onOperatorSelected();
        verifyCanSeeScanningConfigurationForm(data.getCtrl());
        verifyNameMapping(data.getCtrl(), data.getOperator1());
    }

    @Test
    public void isShowXxx_searchWithNoOperatorsFound() throws Exception {
        DialogTestData data = DialogTestData.builder().area(MUNICIPALITY).ctrl(ctrl(MUNICIPALITY)).build().init();

        data.getCtrl().goToSearchForOperator();
        verfiyCanSeeOperatorSearchForm(data.getCtrl());
        data.getCtrl().setOperatorSearchName(data.getSearchString());
        when(getInjectMock(AdminOperatorService.class)
                .operatorsInAreaByName(getUserDataMock(), data.getArea(), data.getSearchString()))
                .thenReturn(emptyList());
        data.getCtrl().performOperatorSearch();
        verfiyCanSeeOperatorSearchForm(data.getCtrl());
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.scanning.search.no.matches.found");
        data.getCtrl().cancelOperatorSearch();
        verifyCanSeeScanningConfigurationForm(data.getCtrl());
    }

    @Test
    public void isShowXxx_searchWithOneOperatorFound() throws Exception {
        DialogTestData data = DialogTestData.builder().area(COUNTY).ctrl(ctrl(COUNTY)).build().init();

        verifyCanSeeScanningConfigurationForm(data.getCtrl());
        data.getCtrl().goToSearchForOperator();
        verfiyCanSeeOperatorSearchForm(data.getCtrl());
        data.getCtrl().setOperatorSearchName(data.getSearchString());
        when(getInjectMock(AdminOperatorService.class)
                .operatorsInAreaByName(getUserDataMock(), data.getArea(), data.getSearchString()))
                .thenReturn(singletonList(data.getOperator1()));
        data.getCtrl().performOperatorSearch();
        verifyCanSeeScanningConfigurationForm(data.getCtrl());
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.scanning.search.one.match.found, " + data.getOperator1().getFullName() + "]");
        verifyNameMapping(data.getCtrl(), data.getOperator1());
    }

    private void verifyCanSeeScanningConfigurationForm(ScanningConfigurationController ctrl) {
        verifyPagesShown(ctrl, true, false, false);
    }

    private void verfiyCanSeeOperatorSearchForm(ScanningConfigurationController ctrl) {
        verifyPagesShown(ctrl, false, true, false);
    }

    private void verifyCanSeeSearchResults(ScanningConfigurationController ctrl) {
        verifyPagesShown(ctrl, false, false, true);
    }

    private void verifyPagesShown(ScanningConfigurationController ctrl, boolean canSeeConfigForm, boolean canSeeOperatorSearch, boolean canSeeSearchResults) {
        assertThat(ctrl.isShowScanningConfigForm()).isEqualTo(canSeeConfigForm);
        assertThat(ctrl.isShowSearchForOperatorForm()).isEqualTo(canSeeOperatorSearch);
        assertThat(ctrl.isShowListOperatorResultsForm()).isEqualTo(canSeeSearchResults);
    }

    private void verifyNameMapping(ScanningConfigurationController ctrl, Operator operator) {
        assertThat(ctrl.getScanningConfig().getResponsibleFullName()).isEqualTo(operator.getFullName());
        assertThat(ctrl.getScanningConfig().getResponsiblePhoneNumber()).isEqualTo(operator.getTelephoneNumber());
        assertThat(ctrl.getScanningConfig().getResponsibleEmail()).isEqualTo(operator.getEmail());
    }
}

@Data
@Builder
class DialogTestData {
    private AreaPath area;
    private ScanningConfigurationController ctrl;
    private String searchString;
    private Operator operator1;
    private Operator operator2;
    private ScanningConfig scanningConfig;

    public DialogTestData init() {
        ctrl.init();
        searchString = "someName";
        operator1 = Operator.builder().firstName("NummerEn").telephoneNumber("12345678").email("an@em.ail").build();
        operator2 = Operator.builder().firstName("NummerTo").build();
        scanningConfig = new ScanningConfig();
        ctrl.setMunicipality(Municipality.builder().scanningConfig(scanningConfig).build());
        ctrl.setCounty(County.builder().scanningConfig(scanningConfig).build());
        return this;
    }
}
