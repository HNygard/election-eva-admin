package no.valg.eva.admin.frontend.election.ctrls;

import no.evote.service.TranslationService;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectionEventControllerTest extends BaseFrontendTest {

    @Test
    public void doInit_withElectionDaysAndThemes_verifyState() throws Exception {
        ElectionEventController ctrl = doInitDefault();

        verify(getInjectMock(UserDataController.class)).invalidateCachedElectionEvent();
        assertThat(ctrl.getUpdateForm().getElectionEvent()).isNotNull();
        assertThat(ctrl.getElectionDayList()).hasSize(2);
        assertThat(ctrl.getElectionEventStatusList()).hasSize(2);
        assertThat(ctrl.getAvailableLocalesSet()).hasSize(2);
        assertThat(ctrl.getLocales()).hasSize(2);
        assertThat(ctrl.getThemes()).hasSize(2);
    }

    @Test
    public void makeNewElectionDay_withElectionEvent_verifyElectionDaySet() throws Exception {
        ElectionEventController electionEventController = doInitDefault();
        Dialog addElectionDayDialog = createMock(Dialog.class);
        MockUtils.setPrivateField(electionEventController, "addElectionDayDialog", addElectionDayDialog);

        electionEventController.makeNewElectionDay();

        assertThat(electionEventController.getElectionDay()).isNotNull();
        assertThat(electionEventController.getElectionDay().getElectionEvent()).isNotNull();
        verify(addElectionDayDialog).open();
    }

    @Test
    public void deleteElectionDay_withOneTotalNumberElectionDays_returnsRequiredElectionDayErrorMessage() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        mockFieldValue("totalNumberElectionDays", 1);

        ctrl.deleteElectionDay();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.required, @election.election_event.election_day]");
    }

    @Test
    public void deleteElectionDay_withDeleteError_returnsNotDeletedErrorMessage() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = getElectionDay(2015, 1, 1);
        electionDay.setPk(1L);
        mockFieldValue("deletedRow", electionDay);
        ElectionEventService service = getInjectMock(ElectionEventService.class);
        when(service.findElectionDayByPk(eq(getUserDataMock()), eq(1L))).thenReturn(electionDay);
        evoteExceptionWhen(ElectionEventService.class).deleteElectionDay(eq(getUserDataMock()), any(ElectionDay.class));

        ctrl.deleteElectionDay();

        assertFacesMessage(FacesMessage.SEVERITY_WARN, "[@election.election_event.election_day_cannot_be_deleted, 01.01.2015]");
    }

    @Test
    public void deleteElectionDay_withValidDelete_returnsDeletedInfoMessage() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = getElectionDay(2015, 1, 1);
        electionDay.setPk(1L);
        mockFieldValue("deletedRow", electionDay);
        mockFieldValue("electionDayList", new ArrayList<ElectionDay>());
        ElectionEventService service = getInjectMock(ElectionEventService.class);
        when(service.findElectionDayByPk(eq(getUserDataMock()), eq(1L))).thenReturn(electionDay);

        ctrl.deleteElectionDay();

        assertFacesMessage(FacesMessage.SEVERITY_INFO, "@election.election_event.election_day_deleted");
        assertThat(ctrl.getElectionDayList()).hasSize(2);
    }

    @Test
    public void setDeletedRow_withElectionDay_verifyDeletedRowAndTotalNumberElectionDaysSet() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = createMock(ElectionDay.class);

        ctrl.setDeletedRow(electionDay);

        assertThat(getPrivateField("deletedRow", ElectionDay.class)).isSameAs(electionDay);
        assertThat(getPrivateField("totalNumberElectionDays", int.class)).isEqualTo(2);
    }

    @Test
    public void doAddElectionDay_withStartTimeAfterStopTime_returnsErrorMessage() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = new ElectionDay();
        electionDay.setStartTime(new LocalTime(10, 0, 0));
        electionDay.setEndTime(new LocalTime(9, 0, 0));
        ctrl.setElectionDay(electionDay);

        ctrl.doAddElectionDay();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.evote_application_exception.START_TIME_NOT_BEFORE_END_TIME, ]");
    }

    @Test
    public void doAddElectionDay_withExistingElectionDay_returnsErrorMessage() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = new ElectionDay();
        electionDay.setStartTime(new LocalTime(10, 0, 0));
        electionDay.setEndTime(new LocalTime(11, 0, 0));
        electionDay.setDate(new LocalDate(2015, 1, 1));
        ctrl.setElectionDay(electionDay);

        ctrl.doAddElectionDay();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@election.election_event.election_day_already_exists, \"null\"]");
    }

    @Test
    public void doAddElectionDay_withCreateError_returnsErrorMessage() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = new ElectionDay();
        electionDay.setStartTime(new LocalTime(10, 0, 0));
        electionDay.setEndTime(new LocalTime(11, 0, 0));
        electionDay.setDate(new LocalDate(2015, 1, 3));
        ctrl.setElectionDay(electionDay);
        evoteExceptionWhen(ElectionEventService.class, "@known.error").createElectionDay(getUserDataMock(), electionDay);

        ctrl.doAddElectionDay();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@known.error");
    }

    @Test
    public void doAddElectionDay_withValidElectionDay_returnsElectionDayListWithSize3() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay electionDay = new ElectionDay();
        electionDay.setStartTime(new LocalTime(10, 0, 0));
        electionDay.setEndTime(new LocalTime(11, 0, 0));
        electionDay.setDate(new LocalDate(2015, 1, 3));
        ctrl.setElectionDay(electionDay);
        Dialog addElectionDayDialog = createMock(Dialog.class);
        MockUtils.setPrivateField(ctrl, "addElectionDayDialog", addElectionDayDialog);

        ctrl.doAddElectionDay();

        assertThat(ctrl.getElectionDayList()).hasSize(3);
        verify(addElectionDayDialog).closeAndUpdate("form:electionDayDataTable", "form:messageBox");
    }

    @Test
    public void doUpdateElectionEvent_withStartAfterEndFound_verifyMessagesAndCreateAndUpdateOperations() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        ElectionDay ed1 = getElectionDay(2015, 1, 1);
        ed1.setStartTime(new LocalTime(10, 0, 0));
        ed1.setEndTime(new LocalTime(9, 0, 0));
        ElectionDay ed2 = getElectionDay(2015, 1, 2);
        ed2.setStartTime(new LocalTime(10, 0, 0));
        ed2.setEndTime(new LocalTime(11, 0, 0));
        ed2.setPk(1L);
        mockFieldValue("electionDayList", Arrays.asList(ed1, ed2));
        ArrayList<FacesMessage> expectedMessages = new ArrayList<>();
        expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.update.successful", null));
        expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_WARN,
                "[@area.polling_place.opening_hours.validate.time_interval.starttime_after_endtime, 10:00, 09:00, 01.01.2015]", null));

        String result = ctrl.doUpdateElectionEvent();

        assertThat(result).isEqualTo("");
        assertFacesMessages(expectedMessages);
        verify(getInjectMock(ElectionEventService.class)).createElectionDay(getUserDataMock(), ed1);
        verify(getInjectMock(ElectionEventService.class)).updateElectionDay(getUserDataMock(), ed2);

    }

    @Test
    public void isRenderDeleteElectionDayLink_withCentralConfStatus_returnsTrue() throws Exception {
        ElectionEventController ctrl = doInitDefault();
        when(ctrl.getUpdateForm().getElectionEvent().getElectionEventStatus().getId()).thenReturn(ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id());

        assertThat(ctrl.isRenderDeleteElectionDayLink()).isTrue();
    }

    @DataProvider(name = "isMissingEnableEventAccess")
    public Object[][] isMissingEnableEventAccess() {
        return new Object[][]{
                {true, true, false},
                {true, false, false},
                {false, true, false},
                {false, false, true}
        };
    }

    private ElectionDay getElectionDay(int year, int month, int day) {
        ElectionDay result = new ElectionDay();
        result.setDate(new LocalDate(year, month, day));
        result.setStartTime(new LocalTime(10, 0, 0));
        result.setEndTime(new LocalTime(11, 0, 0));
        return result;
    }

    private ElectionEvent getElectionEvent(String id) {
        ElectionEvent result = new ElectionEvent();
        result.setId(id);
        return result;
    }

    private ElectionEventStatus getElectionEventStatus(int id) {
        ElectionEventStatus result = new ElectionEventStatus();
        result.setId(id);
        result.setName("election.event.status[" + id + "]");
        return result;
    }

    private ElectionEventLocale getElectionEventLocale(long pk) {
        ElectionEventLocale result = new ElectionEventLocale();
        Locale locale = new Locale();
        locale.setPk(pk);
        locale.setId(String.valueOf(pk));
        locale.setName("locale" + pk);
        result.setLocale(locale);
        return result;
    }

    private void stub_findElectionDaysByElectionEvent() {
        List<ElectionDay> electionDayList = new ArrayList<>(Arrays.asList(getElectionDay(2015, 1, 1), getElectionDay(2015, 1, 2)));
        when(getInjectMock(ElectionEventService.class).findElectionDaysByElectionEvent(eq(getUserDataMock()), any())).thenReturn(electionDayList);
    }

    private void stub_findAllElectionEvents() {
        List<ElectionEvent> electionEventList = new ArrayList<>(Arrays.asList(getElectionEvent("ee1"), getElectionEvent("ee2")));
        when(getInjectMock(ElectionEventService.class).findAll(getUserDataMock())).thenReturn(electionEventList);
    }

    private void stub_findAllElectionEventStatuses() {
        List<ElectionEventStatus> electionEventStatusList = new ArrayList<>(Arrays.asList(getElectionEventStatus(1), getElectionEventStatus(2)));
        when(getInjectMock(ElectionEventService.class).findAllElectionEventStatuses(eq(getUserDataMock()))).thenReturn(electionEventStatusList);
    }

    private void stub_getElectionEventLocalesForEvent() {
        List<ElectionEventLocale> result = new ArrayList<>(Arrays.asList(getElectionEventLocale(1L), getElectionEventLocale(2L)));
        when(getInjectMock(ElectionEventService.class).getElectionEventLocalesForEvent(eq(getUserDataMock()), any(ElectionEvent.class))).thenReturn(result);
    }

    private void stub_getAllLocales() {
        List<Locale> result = new ArrayList<>(Arrays.asList(getElectionEventLocale(1L).getLocale(), getElectionEventLocale(2L).getLocale()));
        when(getInjectMock(TranslationService.class).getAllLocales()).thenReturn(result);
    }

    private ElectionEventController doInitDefault() throws Exception {
        ElectionEventController ctrl = initializeMocks(ElectionEventController.class);
        stub_findElectionDaysByElectionEvent();
        stub_findAllElectionEvents();
        stub_findAllElectionEventStatuses();
        stub_getAllLocales();
        stub_getElectionEventLocalesForEvent();
        URL url = getClass().getResource("/css/themes");
        when(getServletContainer().getServletContextMock().getRealPath("resources/css/themes")).thenReturn(url.getFile());
        when(getMessageProviderMock().get("@common.date.date_pattern")).thenReturn("dd.MM.yyyy");
        when(getMessageProviderMock().get("@common.date.time_pattern")).thenReturn("HH:mm");

        ctrl.init();

        return ctrl;
    }

}

