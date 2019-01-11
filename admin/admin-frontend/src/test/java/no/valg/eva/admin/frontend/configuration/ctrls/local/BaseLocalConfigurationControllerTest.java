package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.Place;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import org.mockito.verification.VerificationMode;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class BaseLocalConfigurationControllerTest extends BaseFrontendTest {

    static final AreaPath ROOT = AreaPath.from("111111");
    static final AreaPath COUNTY = AreaPath.from("111111.22.33");
    static final AreaPath MUNICIPALITY = AreaPath.from("111111.22.33.4444");

    <T extends ConfigurationController> T ctrl(Class<T> cls, AreaPath areaPath) throws Exception {
        T ctrl = initializeMocks(cls);
        return ctrl(ctrl, areaPath);
    }

    <T extends ConfigurationController> T ctrl(T ctrl, AreaPath areaPath) throws Exception {
        return ctrl(localConfigurationController(areaPath), ctrl);
    }

    <T extends ConfigurationController> T ctrl(LocalConfigurationController localConfigurationController, T ctrl) throws Exception {
        ctrl.setMainController(localConfigurationController);
        addControllerToMainController(ctrl, ctrl);
        return ctrl;
    }

    LocalConfigurationController localConfigurationController(AreaPath areaPath) {
        return localConfigurationController(mvArea(areaPath).getValue());
    }

    LocalConfigurationController localConfigurationController(MvArea mvArea) {
        LocalConfigurationController result = createMock(LocalConfigurationController.class);
        when(result.getActiveControllerIndex()).thenReturn(0);
        when(result.getMvArea()).thenReturn(mvArea);
        if (mvArea == null) {
            when(result.getAreaPath()).thenReturn(null);
            when(result.isCountyLevel()).thenReturn(false);
            when(result.isMunicipalityLevel()).thenReturn(false);
        } else {
            AreaPath areaPath = AreaPath.from(mvArea.getAreaPath());
            when(result.getAreaPath()).thenReturn(areaPath);
            boolean b = mvArea.isCountyLevel();
            when(result.isCountyLevel()).thenReturn(b);
            b = mvArea.isMunicipalityLevel();
            when(result.isMunicipalityLevel()).thenReturn(b);
        }
        return result;
    }

    <T extends ConfigurationController> T addControllerToMainController(ConfigurationController ctrl, Class<T> cls) {
        return addControllerToMainController(ctrl, createMock(cls));
    }

    <T extends ConfigurationController> T addControllerToMainController(ConfigurationController ctrl, T toAdd) {
        LocalConfigurationController mainController = ctrl.getMainController();
        List<ConfigurationController> children = mainController.getControllers();
        if (children.size() == 0) {
            children = new ArrayList<>();
            when(mainController.getControllers()).thenReturn(children);
        }
        children.add(toAdd);
        return toAdd;
    }

    MvAreaBuilder mvArea(AreaPath areaPath) {
        return new MvAreaBuilder(areaPath);
    }

    AdvancePollingPlace advancePollingPlace(String id) {
        return advancePollingPlace(id, MUNICIPALITY);
    }

    AdvancePollingPlace advancePollingPlace(String id, AreaPath parentPath) {
        return defaults(new AdvancePollingPlace(parentPath), id);
    }

    ElectionDayPollingPlace electionDayPollingPlace(String id) {
        return electionDayPollingPlace(id, MUNICIPALITY);
    }

    ElectionDayPollingPlace electionDayPollingPlace(String id, AreaPath parentPath) {
        return electionDayPollingPlace(id, parentPath, false);
    }
    
    ElectionDayPollingPlace electionDayPollingPlace(String id, AreaPath parentPath, boolean usePollingStations) {
        ElectionDayPollingPlace result = defaults(new ElectionDayPollingPlace(parentPath), id);
        result.setUsePollingStations(usePollingStations);
        result.setParentName("Parent " + result.getId());
        return result;
    }

    ParentPollingDistrict parentPollingDistrict(String id) {
        return parentPollingDistrict(id, MUNICIPALITY);
    }

    ParentPollingDistrict parentPollingDistrict(String id, AreaPath path) {
        return defaults(new ParentPollingDistrict(path), id);
    }

    RegularPollingDistrict regularPollingDistrict(String id, PollingDistrictType type) {
        return regularPollingDistrict(id, type, MUNICIPALITY);
    }

    RegularPollingDistrict regularPollingDistrict(String id, PollingDistrictType type, AreaPath path) {
        return defaults(new RegularPollingDistrict(path, type), id);
    }

    private <T extends Place> T defaults(T place, String id) {
        place.setPk(Long.parseLong(id));
        place.setId(id);
        place.setName(place.getClass().getSimpleName() + " " + id);
        return place;
    }

    void verifySaveConfigStatus() {
        verifySaveConfigStatus(true);
    }

    void verifySaveConfigStatus(boolean executed) {
        ConfigurationController ctrl = (ConfigurationController) assertTestObject();
        VerificationMode mode = executed ? atLeastOnce() : never();
        verify(ctrl.getMainController(), mode).saveConfigStatuses();
        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
    }

    Borough borough(String id, String name) {
        Borough result = createMock(Borough.class);
        when(result.getId()).thenReturn(id);
        when(result.getName()).thenReturn(name);
        when(result.isMunicipalityBorough()).thenReturn(false);
        return result;
    }

}
