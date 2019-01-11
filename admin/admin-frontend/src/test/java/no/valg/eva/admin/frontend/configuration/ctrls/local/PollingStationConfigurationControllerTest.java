package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.common.configuration.service.PollingPlaceService;
import no.valg.eva.admin.common.configuration.service.PollingStationService;
import no.valg.eva.admin.felles.bakgrunnsjobb.service.BakgrunnsjobbService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PollingStationConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

    @Test
    public void init_withVoterNumbersGeneratedAndPlacesData_returnsErrorMessage() throws Exception {
        PollingStationConfigurationController pollingStationConfigurationController = controller(true);
        stub_isVoterNumbersGenerated();

        pollingStationConfigurationController.init();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.local.manntallsnummerErGenerert");
        assertThat(pollingStationConfigurationController.getPlaces()).hasSize(2);
        assertThat(pollingStationConfigurationController.getPlace()).isNotNull();
        assertThat(pollingStationConfigurationController.getPlace().getId()).isEqualTo("0001");
        assertThat(pollingStationConfigurationController.getPollingStations()).hasSize(2);
    }

    @Test
    public void init_withNoPollingStations_returnsInfoMessage() throws Exception {
        PollingStationConfigurationController ctrl = controller(false);

        ctrl.init();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.polling_station.noPollingStationsConfigured");
        assertThat(ctrl.getPlaces()).isEmpty();
        assertThat(ctrl.getPlace()).isNull();
        assertThat(ctrl.getPollingStations()).isEmpty();
    }

    @Test
    public void getView_returnsPollingStationView() throws Exception {
        assertThat(controller(true).getView()).isSameAs(ConfigurationView.POLLING_STATION);
    }

    @Test
    public void getName_returnsCorrectName() throws Exception {
        assertThat(controller(true).getName()).isEqualTo("@config.local.accordion.polling_stations.name");
    }

    @Test(enabled = false, dataProvider = "hasAccessProvider")
    public void hasAccess_withDataProvider_verifyExpected(AccessTest test, boolean expected) throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        when(ctrl.getMainController().isMunicipalityLevel()).thenReturn(test.isMunicipalityLevel());
        stub_isUseElectronicMarkoffsConfigured(ctrl, test.isUseElectronicMarkoffsConfigured());
        when(ctrl.getController(ElectionDayPollingPlacesConfigurationController.class).isDoneStatus() && !ctrl.collectPollingPlaces().isEmpty()).thenReturn(
                test.isDoneWithPollingStations());

        assertThat(ctrl.hasAccess()).isEqualTo(expected);
    }

    private static final AccessTest NOT_MUNICIPALITY_LEVEL = new AccessTest(false, false, false);
    private static final AccessTest USES_ELECTRONIC_MARKOFFS = new AccessTest(true, true, false);
    private static final AccessTest DONE_WITHOUT_POLLINGSTATIONS = new AccessTest(true, false, true);
    private static final AccessTest DONE_WITH_POLLINGSTATIONS = new AccessTest(true, false, false);

    @DataProvider(name = "hasAccessProvider")
    public Object[][] hasAccessProvider() {
        return new Object[][]{
                {NOT_MUNICIPALITY_LEVEL, false},
                {USES_ELECTRONIC_MARKOFFS, false},
                {DONE_WITHOUT_POLLINGSTATIONS, false},
                {DONE_WITH_POLLINGSTATIONS, true}
        };
    }

    @Test(dataProvider = "buttonProvider")
    public void button_withDataProvider_verifyExpected(ButtonType buttonType, ButtonTest test, boolean rendered, boolean enabled) throws Exception {
        PollingStationConfigurationController ctrl = controller(true, test.getPollingStationsSize(), true);
        ctrl.init();
        ctrl.setMode(test.getConfigurationMode());
        mockFieldValue("dirty", test.isDirty());

        Button result = ctrl.button(buttonType);

        assertThat(result.isRendered()).isEqualTo(rendered);
        assertThat(result.isDisabled()).isEqualTo(!enabled);
    }

    private static final ButtonTest WITH_ONE_POLLING_STATION = new ButtonTest(1, ConfigurationMode.READ, false);
    private static final ButtonTest WITH_TWO_POLLING_STATION = new ButtonTest(2, ConfigurationMode.READ, false);
    private static final ButtonTest WITH_MAX_POLLING_STATION = new ButtonTest(14, ConfigurationMode.READ, false);
    private static final ButtonTest WITH_WRITE_MODE = new ButtonTest(2, ConfigurationMode.UPDATE, false);
    private static final ButtonTest WITH_DIRTY = new ButtonTest(2, ConfigurationMode.READ, true);
    private static final ButtonTest WITH_WRITE_MODE_AND_DIRTY = new ButtonTest(2, ConfigurationMode.UPDATE, true);

    @DataProvider(name = "buttonProvider")
    public Object[][] buttonProvider() {
        return new Object[][]{
                {ButtonType.PREV, WITH_ONE_POLLING_STATION, true, false},
                {ButtonType.PREV, WITH_TWO_POLLING_STATION, true, true},
                {ButtonType.NEXT, WITH_MAX_POLLING_STATION, true, false},
                {ButtonType.NEXT, WITH_TWO_POLLING_STATION, true, true},
                {ButtonType.UPDATE, WITH_WRITE_MODE, true, false},
                {ButtonType.UPDATE, WITH_WRITE_MODE_AND_DIRTY, true, true},
                {ButtonType.SAVE, WITH_DIRTY, true, true},
                {ButtonType.CANCEL, WITH_DIRTY, true, true},
                {ButtonType.APPROVE_TO_SETTLEMENT, WITH_DIRTY, false, false},
        };
    }

    @Test
    public void isEditable_withVoterNumbersGenerated_returnsFalse() throws Exception {
        PollingStationConfigurationController ctrl = controller(true, 2, null);
        stub_isVoterNumbersGenerated();
        ctrl.init();

        assertThat(ctrl.isEditable()).isFalse();
    }

    @Test
    public void isDoneStatus_withPollingStationsStatusTrue_returnsTrue() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        when(ctrl.getMunicipalityConfigStatus().isPollingStations()).thenReturn(true);

        assertThat(ctrl.isDoneStatus()).isTrue();
    }

    @Test
    public void setDoneStatus_withTrue_setsPollingStationsStatus() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);

        ctrl.setDoneStatus(true);

        verify(ctrl.getMunicipalityConfigStatus()).setPollingStations(true);
    }

    @Test
    public void canBeSetToDone_withVoterNumbersGenerated_returnsTrue() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        stub_isVoterNumbersGenerated();
        ctrl.init();

        assertThat(ctrl.canBeSetToDone()).isTrue();
    }

    @Test
    public void canBeSetToDone_withPollingPlaceWithoutStations_returnsFalse() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        ctrl.init();

        assertThat(ctrl.canBeSetToDone()).isFalse();
    }

    @Test
    public void cancelWrite_verifyState() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        ctrl.init();
        ctrl.setMode(ConfigurationMode.UPDATE);
        mockFieldValue("dirty", true);

        ctrl.cancelWrite();

        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
        assertThat(ctrl.isDirty()).isFalse();
        verify(getInjectMock(PollingStationService.class), atLeastOnce()).findPollingStationsByArea(eq(getUserDataMock()), any(AreaPath.class));
    }

    @Test
    public void setUpdateMode_withDoneStatus_verifyState() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        when(ctrl.getMunicipalityConfigStatus().isPollingStations()).thenReturn(true);
        ctrl.init();

        ctrl.setUpdateMode();

        assertThat(ctrl.isDirty()).isTrue();
        verify(ctrl.getMunicipalityConfigStatus()).setPollingStations(false);
        verify(getRequestContextMock()).update(Arrays.asList("configurationPanel", "approve-form"));
    }

    @Test
    public void setUpdateMode_withUndoneStatus_verifyState() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        when(ctrl.getMunicipalityConfigStatus().isPollingStations()).thenReturn(false);
        ctrl.init();

        ctrl.setUpdateMode();

        assertThat(ctrl.isDirty()).isTrue();
        verify(getRequestContextMock()).update(Collections.singletonList(
                "configurationPanel:0:pollingStation:form"));
    }

    @Test
    public void getRequiresDoneBeforeEdit_withElectionGroupElectronicMarkoffs_verifyResult() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        when(ctrl.getMainController().getElectionGroup().isElectronicMarkoffs()).thenReturn(true);

        Class<? extends ConfigurationController>[] classes = ctrl.getRequiresDoneBeforeEdit();

        assertThat(classes).hasSize(1);
        assertThat(classes[0].isAssignableFrom(ElectronicMarkoffsConfigurationController.class)).isTrue();
    }

    @Test
    public void getRequiresDoneBeforeDone_verifyResult() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);

        Class<? extends ConfigurationController>[] classes = ctrl.getRequiresDoneBeforeDone();

        assertThat(classes).hasSize(1);
        assertThat(classes[0].isAssignableFrom(ElectionDayPollingPlacesConfigurationController.class)).isTrue();
    }

    @Test
    public void addPollingStation_withOriginallyNoneAndNoVoters_returnsErrorMessage() throws Exception {
        PollingStationConfigurationController ctrl = controller(true, 0, true);
        ctrl.init();
        stub_findPollingStationsByAreaCalculated(1, null);

        ctrl.addPollingStation();

        assertThat(ctrl.getPollingStations()).isEmpty();
        assertThat(ctrl.isDirty()).isTrue();
        assertThat(ctrl.getMode()).isEqualTo(ConfigurationMode.READ);
        assertThat(ctrl.getPlace().isHasPollingStations()).isFalse();
        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.polling_stations.emptyElectoralRoll");
    }

    @Test
    public void subtractPollingStation_withOriginallyTwo_returnsErrorMessage() throws Exception {
        PollingStationConfigurationController ctrl = controller(true, 4, true);
        ctrl.init();
        stub_findPollingStationsByAreaCalculated(3, Arrays.asList(
                rode("0001", "A", "B", 10),
                rode("0002", "A", "B", 10),
                rode("0003", "A", "B", 10)));

        ctrl.subtractPollingStation();

        assertThat(ctrl.getPollingStations()).hasSize(3);
        assertThat(ctrl.isDirty()).isTrue();
        assertThat(ctrl.getMode()).isEqualTo(ConfigurationMode.READ);
        assertThat(ctrl.getPlace().isHasPollingStations()).isTrue();
    }

    @Test
    public void recalculateChanges_withInvalidList_returnsErrorMessage() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        ctrl.init();
        ctrl.getPollingStations().set(0, new Rode("Å", "G"));

        ctrl.recalculateChanges();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.polling_stations.divisionList.rangesNotFollowing");
    }

    @Test
    public void recalculateChanges_withValidList_verifyRecalculate() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        ctrl.init();
        ctrl.getPollingStations().set(0, new Rode("A", "G"));
        ctrl.getPollingStations().set(1, new Rode("H", "Å"));

        ctrl.recalculateChanges();

        assertThat(ctrl.getMode()).isEqualTo(ConfigurationMode.READ);
        assertThat(ctrl.getPlace().isHasPollingStations()).isTrue();
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.polling_station.recalculateChangesOK");
        verify(getInjectMock(PollingStationService.class)).recalculatedPollingStationsByArea(eq(getUserDataMock()), eq(MUNICIPALITY), anyList());
    }

    @Test
    public void saveChanges_withInvalidList_returnsErrorMessage() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        ctrl.init();
        ctrl.getPollingStations().set(0, new Rode("Å", "G"));

        ctrl.saveChanges();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.polling_stations.divisionList.rangesNotFollowing");
    }

    @Test
    public void saveChanges_withValidList_verifySave() throws Exception {
        PollingStationConfigurationController ctrl = controller(true);
        ctrl.init();
        ctrl.getPollingStations().set(0, new Rode("A", "G"));
        ctrl.getPollingStations().set(1, new Rode("H", "Å"));

        ctrl.saveChanges();

        assertThat(ctrl.isDirty()).isFalse();
        assertThat(ctrl.getPlace().isHasPollingStations()).isTrue();
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.polling_station.saveChangesOK");
        verify(getInjectMock(PollingStationService.class)).save(eq(getUserDataMock()), eq(MUNICIPALITY), anyList());
    }

    private PollingStationConfigurationController controller(boolean withElectionDays) throws Exception {
        return controller(withElectionDays, 2, true);
    }

    private PollingStationConfigurationController controller(boolean withElectionDays, int pollingStations, Boolean isEditable) throws Exception {
        PollingStationConfigurationController result = ctrl(initializeMocks(new PollingStationConfigurationController() {
            @Override
            public boolean isEditable() {
                if (isEditable == null) {
                    return super.isEditable();
                }
                return isEditable;
            }

            @Override
            boolean isParentEditable() {
                return true;
            }
        }), MUNICIPALITY);
        addControllerToMainController(result, ElectionDayPollingPlacesConfigurationController.class);
        stub_getPlacesWithPollingStation(result, withElectionDays);
        stub_findPollingStationsByArea(pollingStations);
        return result;
    }

    private List<ElectionDayPollingPlace> stub_getPlacesWithPollingStation(PollingStationConfigurationController pollingStationConfigurationController, boolean withElectionDays) {
        List<ElectionDayPollingPlace> result = new ArrayList<>();
        if (withElectionDays) {
            result = Arrays.asList(
                    electionDayPollingPlace("0001", MUNICIPALITY, true),
                    electionDayPollingPlace("0001", MUNICIPALITY, true));
        }

        when(getInjectMock(PollingPlaceService.class).findElectionDayPollingPlacesByArea(getUserDataMock(), pollingStationConfigurationController.getAreaPath())).thenReturn(result);

        return result;
    }

    private List<Rode> stub_findPollingStationsByArea(int pollingStations) {
        List<Rode> result = new ArrayList<>();
        for (int i = 0; i < pollingStations; i++) {
            result.add(rode((i + 1) + "", "a", "b", i * 100));
        }
        when(getInjectMock(PollingStationService.class).findPollingStationsByArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(result);
        return result;
    }

    private Rode rode(String id, String first, String last, int voters) {
        return new Rode(id, first, last, voters);
    }

    private void stub_isVoterNumbersGenerated() {
        when(getInjectMock(BakgrunnsjobbService.class).erManntallsnummergenereringStartetEllerFullfort(eq(getUserDataMock()))).thenReturn(true);
    }

    private void stub_isUseElectronicMarkoffsConfigured(PollingStationConfigurationController ctrl, boolean value) {
        when(ctrl.getMunicipalityConfigStatus().isUseElectronicMarkoffs()).thenReturn(value);
        when(ctrl.getMunicipalityConfigStatus().isElectronicMarkoffs()).thenReturn(value);
    }

    private void stub_findPollingStationsByAreaCalculated(int number, List<Rode> list) {
        when(getInjectMock(PollingStationService.class).findPollingStationsByAreaCalculated(
                eq(getUserDataMock()), any(AreaPath.class), eq(number))).thenReturn(list);
    }

    private static class AccessTest {
        private boolean isMunicipalityLevel;
        private boolean isUseElectronicMarkoffsConfigured;
        private boolean isDoneWithPollingStations;

        public AccessTest(boolean isMunicipalityLevel, boolean isUseElectronicMarkoffsConfigured, boolean isDoneWithPollingStations) {
            this.isMunicipalityLevel = isMunicipalityLevel;
            this.isUseElectronicMarkoffsConfigured = isUseElectronicMarkoffsConfigured;
            this.isDoneWithPollingStations = isDoneWithPollingStations;
        }

        public boolean isMunicipalityLevel() {
            return isMunicipalityLevel;
        }

        public boolean isUseElectronicMarkoffsConfigured() {
            return isUseElectronicMarkoffsConfigured;
        }

        public boolean isDoneWithPollingStations() {
            return isDoneWithPollingStations;
        }
    }

    private static class ButtonTest {
        private int pollingStationsSize;
        private ConfigurationMode configurationMode;
        private boolean dirty;

        public ButtonTest(int pollingStationsSize, ConfigurationMode configurationMode, boolean dirty) {
            this.pollingStationsSize = pollingStationsSize;
            this.configurationMode = configurationMode;
            this.dirty = dirty;
        }

        public int getPollingStationsSize() {
            return pollingStationsSize;
        }

        public ConfigurationMode getConfigurationMode() {
            return configurationMode;
        }

        public boolean isDirty() {
            return dirty;
        }
    }
}

