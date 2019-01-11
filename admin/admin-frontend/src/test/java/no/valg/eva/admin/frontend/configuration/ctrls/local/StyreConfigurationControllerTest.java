package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.configuration.service.ReportingUnitService;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import org.joda.time.LocalDate;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.util.MockUtils.setPrivateField;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.LEDER;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.MEDLEM;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.NESTLEDER;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.SEKRETAER;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.VARAMEDLEM;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.isUniqueResponsibility;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class StyreConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

    @Test
    public void init_withOfficers_verifyState() throws Exception {
        StyreConfigurationController ctrl = controller();
        stub_findByArea();

        ctrl.init();

        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
        assertThat(ctrl.getResponsibleOfficers()).hasSize(1);
    }

    @Test
    public void getStyreAreaPath_returnsAreaPath() throws Exception {
        StyreConfigurationController ctrl = controller();

        assertThat(ctrl.getStyreAreaPath()).isEqualTo(ctrl.getAreaPath());
    }

    @Test(dataProvider = "button")
    public void button_withDataProvider_verifyExpected(ButtonType buttonType, boolean isEditable, ConfigurationMode mode, boolean isRendered,
                                                       boolean isDisabled)
            throws Exception {
        StyreConfigurationController ctrl = controller(isEditable);
        ctrl.setMode(mode);

        Button button = ctrl.button(buttonType);

        assertThat(button.isRendered()).isEqualTo(isRendered);
        assertThat(button.isDisabled()).isEqualTo(isDisabled);
    }

    @DataProvider(name = "button")
    public Object[][] button() {
        return new Object[][]{
                {ButtonType.CREATE, true, ConfigurationMode.READ, true, false},
                {ButtonType.CREATE, false, ConfigurationMode.READ, true, true},
                {ButtonType.CREATE, true, ConfigurationMode.CREATE, true, true},
                {ButtonType.CREATE, false, ConfigurationMode.CREATE, true, true},
                {ButtonType.DELETE, true, ConfigurationMode.READ, true, false},
                {ButtonType.DELETE, false, ConfigurationMode.READ, true, true},
                {ButtonType.DELETE, true, ConfigurationMode.CREATE, true, true},
                {ButtonType.DELETE, false, ConfigurationMode.CREATE, true, true},
                {ButtonType.EXECUTE_DELETE, true, ConfigurationMode.READ, true, false},
                {ButtonType.EXECUTE_DELETE, false, ConfigurationMode.READ, true, true},
                {ButtonType.EXECUTE_DELETE, true, ConfigurationMode.CREATE, true, true},
                {ButtonType.EXECUTE_DELETE, false, ConfigurationMode.CREATE, true, true},
                {ButtonType.SAVE, true, ConfigurationMode.CREATE, true, false},
                {ButtonType.SAVE, false, ConfigurationMode.CREATE, true, true},
                {ButtonType.SAVE, true, ConfigurationMode.READ, true, true},
                {ButtonType.SAVE, false, ConfigurationMode.READ, true, true},
                {ButtonType.APPROVE, true, ConfigurationMode.READ, false, true}

        };
    }

    @Test
    public void prepareCreateResponsibleOfficer_verifyState() throws Exception {
        StyreConfigurationController controller = controller();

        controller.onCreateBoardMemberClick();

        assertThat(controller.getSelectedResponsibleOfficer()).isNull();
        assertThat(controller.getSearchResult()).isNull();
        assertThat(controller.getElectoralRollSearch()).isNotNull();
        assertThat(controller.getMode()).isSameAs(ConfigurationMode.SEARCH);
    }

    @Test
    public void searchResponsibleOfficer_withSsnAndNoResult_addsMessage() throws Exception {
        StyreConfigurationController ctrl = controller();
        ctrl.onCreateBoardMemberClick();
        ctrl.getElectoralRollSearch().setSsn("24036518886");
        ctrl.getElectoralRollSearch().setBirthDate(new LocalDate());
        ctrl.getElectoralRollSearch().setName("Test");
        stub_search(ctrl, 0);

        ctrl.searchResponsibleOfficer();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.electoral_roll_search.emptyResult");
        assertThat(ctrl.getElectoralRollSearch().getBirthDate()).isNull();
        assertThat(ctrl.getElectoralRollSearch().getName()).isNull();
        assertThat(ctrl.getSearchResult()).isEmpty();
    }

    @Test(dataProvider = "searchResponsibleOfficer")
    public void searchResponsibleOfficer_withDataProvider_verifyExpected(String name, String expectedFirstName, String expectedMiddleName,
                                                                         String expectedLastName)
            throws Exception {
        StyreConfigurationController ctrl = controller();
        ctrl.onCreateBoardMemberClick();
        ctrl.getElectoralRollSearch().setName(name);
        stub_search(ctrl, 0);

        ctrl.searchResponsibleOfficer();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@config.local.electoral_roll_search.emptyResult");
        assertThat(ctrl.getSelectedResponsibleOfficer().getFirstName()).isEqualTo(expectedFirstName);
        assertThat(ctrl.getSelectedResponsibleOfficer().getMiddleName()).isEqualTo(expectedMiddleName);
        assertThat(ctrl.getSelectedResponsibleOfficer().getLastName()).isEqualTo(expectedLastName);
    }

    @DataProvider(name = "searchResponsibleOfficer")
    public Object[][] searchResponsibleOfficer() {
        return new Object[][]{
                {"Per Arne Peder Arnesen", "Per", "Arne Peder", "Arnesen"},
                {"Per Arnesen", "Per", null, "Arnesen"},
                {"Arnesen", null, null, "Arnesen"}
        };
    }

    @Test
    public void searchResponsibleOfficer_withSsnAndOneRowResult_setSelected() throws Exception {
        StyreConfigurationController ctrl = controller();
        ctrl.onCreateBoardMemberClick();
        ctrl.getElectoralRollSearch().setSsn("24036518886");
        stub_search(ctrl, 1);

        ctrl.searchResponsibleOfficer();

        assertThat(ctrl.getSearchResult()).hasSize(1);
        assertThat(ctrl.getSelectedResponsibleOfficer()).isNotNull();
    }

    @Test
    public void searchResponsibleOfficer_withBirthDateAndTwoRowResult_verifyState() throws Exception {
        StyreConfigurationController ctrl = controller();
        ctrl.onCreateBoardMemberClick();
        ctrl.getElectoralRollSearch().setSsn("ERROR");
        ctrl.getElectoralRollSearch().setBirthDate(new LocalDate());
        stub_search(ctrl, 2);

        ctrl.searchResponsibleOfficer();

        assertThat(ctrl.getSearchResult()).hasSize(2);
        assertThat(ctrl.getSelectedResponsibleOfficer()).isNull();
    }

    @Test
    public void cancelSearchResponsibleOfficer_verifyState() throws Exception {
        StyreConfigurationController ctrl = controller();
        ctrl.onCreateBoardMemberClick();

        ctrl.cancelSearchResponsibleOfficer();

        assertThat(ctrl.getSelectedResponsibleOfficer()).isNull();
        assertThat(ctrl.getSearchResult()).isNull();
        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
    }

    @Test
    public void saveResponsibleOfficer_withSelected_verifySave() throws Exception {
        StyreConfigurationController controller = controller();
        ResponsibleOfficer officer = stub_selectedResponsibleOfficer();
        when(getInjectMock(ReportingUnitService.class).save(getUserDataMock(), officer)).thenReturn(officer);
        when(getInjectMock(ReportingUnitService.class).validate(getUserDataMock(), officer)).thenReturn(true);
        controller.setDoneStatus(true);

        controller.saveResponsibleOfficer();

        assertThat(controller.getSelectedResponsibleOfficer().getAreaPath()).isEqualTo(controller.getStyreAreaPath());
        assertThat(controller.isDoneStatus()).isFalse();
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.responsibleofficer Test Testesen]");
        verify(getInjectMock(ReportingUnitService.class)).save(getUserDataMock(), officer);
        verify(getInjectMock(ReportingUnitService.class)).findByArea(getUserDataMock(), controller.getStyreAreaPath());
    }

    @Test
    public void cancelSaveResponsibleOfficer_setsSelectedToNull() throws Exception {
        StyreConfigurationController ctrl = controller();
        stub_selectedResponsibleOfficer();

        ctrl.cancelSaveResponsibleOfficer();

        assertThat(ctrl.getSelectedResponsibleOfficer()).isNull();
    }

    @Test
    public void onResponsibleOfficerSelected_setsSelectedOfficer() throws Exception {
        StyreConfigurationController ctrl = controller();
        ResponsibleOfficer officer = new ResponsibleOfficer();
        SelectEvent event = createMock(SelectEvent.class);
        when(event.getObject()).thenReturn(officer);

        ctrl.onResponsibleOfficerSelected(event);

        assertThat(ctrl.getSelectedResponsibleOfficer()).isSameAs(officer);
    }

    @Test
    public void getAddHeaderBorn_withOfficer_returnsHeader() throws Exception {
        StyreConfigurationController ctrl = controller();
        stub_selectedResponsibleOfficer();
        when(getUserDataMock().getJavaLocale()).thenReturn(new Locale("no"));

        assertThat(ctrl.getAddHeaderBorn().toLowerCase()).isEqualTo("24 mar 1965");
    }

    @Test
    public void getAddHeaderAddress_withOfficer_returnsHeader() throws Exception {
        StyreConfigurationController ctrl = controller();
        stub_selectedResponsibleOfficer();

        assertThat(ctrl.getAddHeaderAddress()).isEqualTo("Hesteveien 12, 1234 Stallen");
    }

    @Test
    public void confirmDelete_withSelected_verifyDelete() throws Exception {
        StyreConfigurationController ctrl = controller();
        stub_selectedResponsibleOfficer();
        ctrl.setDoneStatus(true);

        ctrl.confirmDelete();

        assertThat(ctrl.isDoneStatus()).isFalse();
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.deleted, @common.displayable.responsibleofficer Test Testesen]");
        verify(getInjectMock(ReportingUnitService.class)).delete(getUserDataMock(), ctrl.getSelectedResponsibleOfficer());
        verify(getInjectMock(ReportingUnitService.class)).findByArea(getUserDataMock(), ctrl.getStyreAreaPath());
    }

    @Test
    public void isRequiresDone_returnsFalse() throws Exception {
        assertThat(controller().isRequiresDone()).isFalse();
    }

    @Test
    public void canBeSetToDone_withOneOrMoreOfficers_returnsTrue() throws Exception {
        StyreConfigurationController ctrl = controller();
        mockFieldValue("responsibleOfficers", singletonList(responsibleOfficer()));

        assertThat(ctrl.canBeSetToDone()).isTrue();
    }

    @Test
    public void getResponsibilities_returnsFive() throws Exception {
        List<ResponsibilityId> expectedResponsibilityIds = ResponsibilityId.list();

        List<ResponsibilityId> responsibilityIds = controller().getResponsibilities();

        assertThat(responsibilityIds).isEqualTo(expectedResponsibilityIds);
    }

    @Test
    public void prepareDeleteResponsibleOfficer_setsSelected() throws Exception {
        StyreConfigurationController ctrl = controller();
        ResponsibleOfficer officer = createMock(ResponsibleOfficer.class);

        ctrl.setSelectedResponsibleOfficer(officer);

        assertThat(ctrl.getSelectedResponsibleOfficer()).isSameAs(officer);
    }

    @Test
    public void canBeSetToDone_withNoOfficers_returnsFalse() throws Exception {
        StyreConfigurationController ctrl = controller();
        setPrivateField(ctrl, "responsibleOfficers", new ArrayList<>());

        assertThat(ctrl.canBeSetToDone()).isFalse();
    }

    @Test
    public void canBeSetToDone_withInvalidOfficers_returnsFalse() throws Exception {
        StyreConfigurationController ctrl = controller();
        setPrivateField(ctrl, "responsibleOfficers", singletonList(responsibleOfficer(LEDER, false)));

        assertThat(ctrl.canBeSetToDone()).isFalse();
    }

    @Test
    public void getResponsibilities_withUniqueExisting_returns_Non_Unique_Responsibilities() throws Exception {
        StyreConfigurationController ctrl = controller();
        setPrivateField(ctrl, "responsibleOfficers", asList(
                responsibleOfficer(LEDER, true),
                responsibleOfficer(NESTLEDER, true),
                responsibleOfficer(SEKRETAER, true)));

        List<ResponsibilityId> result = ctrl.getResponsibilities();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(VARAMEDLEM);
        assertThat(result.get(1)).isSameAs(MEDLEM);
    }

    @Test
    public void onRowReorder_withEvent_verifyReorder() throws Exception {
        StyreConfigurationController ctrl = controller();
        List<ResponsibleOfficer> responsibleOfficers = asList(
                responsibleOfficer(LEDER, true),
                responsibleOfficer(NESTLEDER, true),
                responsibleOfficer(SEKRETAER, true));
        setPrivateField(ctrl, "responsibleOfficers", responsibleOfficers);
        ReorderEvent event = createMock(ReorderEvent.class);
        when(event.getFromIndex()).thenReturn(0);
        when(event.getToIndex()).thenReturn(2);
        when(getInjectMock(ReportingUnitService.class).saveResponsibleOfficerDisplayOrder(eq(getUserDataMock()), any(AreaPath.class), anyList()))
                .thenReturn(responsibleOfficers);
        when(ctrl.isDoneStatus()).thenReturn(true);

        ctrl.onRowReorder(event);

        verify(responsibleOfficers.get(0)).setDisplayOrder(1);
        verify(responsibleOfficers.get(1)).setDisplayOrder(2);
        verify(responsibleOfficers.get(2)).setDisplayOrder(3);
        verify(ctrl.getCountyConfigStatus()).setReportingUnitFylkesvalgstyre(false);
        verify(getInjectMock(ReportingUnitService.class)).saveResponsibleOfficerDisplayOrder(eq(getUserDataMock()), any(AreaPath.class), anyList());
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.displayable.ResponsibleOfficer SEKRETAER flyttet fra nr. 1 til nr. 3");
    }

    @Test
    public void isSearchMode_withSearchMode_returnsTrue() throws Exception {
        StyreConfigurationController ctrl = controller();
        ctrl.setMode(ConfigurationMode.SEARCH);

        assertThat(ctrl.isSearchMode()).isTrue();
    }

    @Test
    public void isCreateFromElectoralRollMode_withIdAndCreateMode_returnsTrue() throws Exception {
        StyreConfigurationController ctrl = controller();
        ResponsibleOfficer officer = new ResponsibleOfficer();
        officer.setId("id");
        ctrl.setSelectedResponsibleOfficer(officer);
        ctrl.setMode(ConfigurationMode.CREATE);

        assertThat(ctrl.isCreateFromElectoralRollMode()).isTrue();
    }

    @Test
    public void isCreateMode_withoutIdAndWithCreateMode_returnsTrue() throws Exception {
        StyreConfigurationController ctrl = controller();
        ResponsibleOfficer officer = new ResponsibleOfficer();
        ctrl.setSelectedResponsibleOfficer(officer);
        ctrl.setMode(ConfigurationMode.CREATE);

        assertThat(ctrl.isCreateMode()).isTrue();
    }

    @Test
    public void testGetResponsibilitiesForUpdateUser_Unique_Responsibilities_Not_Available() throws Exception {
        StyreConfigurationController controller = controller();

        List<ResponsibleOfficer> responsibleOfficers = asList(responsibleOfficer(1L, LEDER, true),
                responsibleOfficer(2L, NESTLEDER, true),
                responsibleOfficer(3L, MEDLEM, true),
                responsibleOfficer(4L, SEKRETAER, true));
        
        setPrivateField(controller, "responsibleOfficers", responsibleOfficers);
        
        ResponsibleOfficer responsibleOfficerWithNonUniqueResponsibility = responsibleOfficers.get(2);
        setPrivateField(controller, "selectedResponsibleOfficer", responsibleOfficerWithNonUniqueResponsibility);

        List<ResponsibilityId> responsibilitiesForUpdateUser = controller.getResponsibilitiesForUpdateUser();
        assertEquals(responsibilitiesForUpdateUser, getNonUniqueResponsibilities());
    }

    private List<ResponsibilityId> getNonUniqueResponsibilities() {
        return ResponsibilityId.list().stream().filter(responsibilityId -> !isUniqueResponsibility(responsibilityId)).collect(Collectors.toList());
    }

    private StyreConfigurationController controller() throws Exception {
        return controller(true);
    }

    private StyreConfigurationController controller(boolean isEditable) throws Exception {
        return ctrl(initializeMocks(new FylkesvalgstyreConfigurationController() {
            @Override
            public boolean isEditable() {
                return isEditable;
            }
        }), COUNTY);
    }

    private void stub_findByArea() {
        ResponsibleOfficer officer = responsibleOfficer();
        when(getInjectMock(ReportingUnitService.class).findByArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(singletonList(officer));
    }

    private void stub_search(StyreConfigurationController ctrl, int size) {
        List<ResponsibleOfficer> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(responsibleOfficer());
        }
        when(getInjectMock(ReportingUnitService.class).search(getUserDataMock(), ctrl.getAreaPath(), ctrl.getElectoralRollSearch())).thenReturn(result);
    }

    private ResponsibleOfficer stub_selectedResponsibleOfficer() throws Exception {
        ResponsibleOfficer officer = new ResponsibleOfficer();
        officer.setId("24036518886");
        officer.setFirstName("Test");
        officer.setLastName("Testesen");
        officer.setAddress("Hesteveien 12");
        officer.setPostalCode("1234");
        officer.setPostalTown("Stallen");
        mockFieldValue("selectedResponsibleOfficer", officer);
        return officer;
    }

    private ResponsibleOfficer responsibleOfficer() {
        return responsibleOfficer(LEDER, true);
    }

    private ResponsibleOfficer responsibleOfficer(ResponsibilityId responsibilityId, boolean isValid) {
        return responsibleOfficer(1L, responsibilityId, isValid);
    }

    private ResponsibleOfficer responsibleOfficer(Long id, ResponsibilityId responsibilityId, boolean isValid) {
        ResponsibleOfficer result = createMock(ResponsibleOfficer.class);
        when(result.isValid()).thenReturn(isValid);
        when(result.getResponsibilityId()).thenReturn(responsibilityId);
        when(result.display()).thenReturn(responsibilityId.name());
        when(result.getPk()).thenReturn(id);

        return result;
    }

}

