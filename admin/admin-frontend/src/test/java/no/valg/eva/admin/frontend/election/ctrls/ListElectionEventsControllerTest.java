package no.valg.eva.admin.frontend.election.ctrls;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.security.UserData;
import no.evote.service.TranslationService;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ListElectionEventsControllerTest extends BaseFrontendTest {

	@Test
	public void init_verifyState() throws Exception {
		ListElectionEventsController ctrl = setupDefault();

		assertThat(ctrl.getCreateForm()).isNull();
		assertThat(ctrl.getElectionEventList()).hasSize(2);
		assertThat(ctrl.getAvailableLocalesSet()).isEmpty();
		assertThat(ctrl.getLocales()).hasSize(2);
		assertThat(ctrl.getThemes()).hasSize(2);
	}

	@Test
	public void doGetCreateElectionEvent_verifyState() throws Exception {
		ListElectionEventsController ctrl = initializeMocks(ListElectionEventsController.class);
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectoralRollLinesPerPage()).thenReturn(30);
		ctrl.getAvailableLocalesSet().add(createMock(Locale.class));
		Dialog dialog = createMock(Dialog.class);
		MockUtils.setPrivateField(ctrl, "createElectionEventDialog", dialog);

		ctrl.doGetCreateElectionEvent();

		assertThat(ctrl.getCreateForm()).isNotNull();
		assertThat(ctrl.getCreateForm().getElectionEvent()).isNotNull();
		assertThat(ctrl.getCreateForm().getElectionEvent().getElectoralRollLinesPerPage()).isEqualTo(30);
		assertThat(ctrl.getAvailableLocalesSet()).isEmpty();
		verify(dialog).open();
	}

	@Test
	public void reOpenElectionEvent_withElectionEventToReOpen_verifyMessageAndApproveConfiguration() throws Exception {
		ListElectionEventsController ctrl = setupDefault();
		ElectionEvent electionEventToReOpen = createMock(ElectionEvent.class);
		ctrl.setElectionEventToReOpen(electionEventToReOpen);

		ctrl.reOpenElectionEvent();

		assertThat(ctrl.getElectionEventToReOpen()).isNotNull();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@election.election_event.reopen.message, \"null\"]");
		verify(getInjectMock(ElectionEventService.class)).approveConfiguration(getUserDataMock(), 0L);
	}

	@Test
	public void doCreateElectionEvent_withNoAsynchPropsSet_createsSynchronously() throws Exception {
		ListElectionEventsController ctrl = setupDefault();
		ctrl.doGetCreateElectionEvent();
		ElectionEvent electionEvent = new ElectionEvent();
        when(getInjectMock(ElectionEventService.class).create(any(), any(), eq(false), eq(VotingHierarchy.NONE),
				eq(CountingHierarchy.NONE), any(), anySet())).thenReturn(electionEvent);
        when(getInjectMock(ElectionEventService.class).findById(any(UserData.class), any())).thenReturn(null);

		ctrl.doCreateElectionEvent();

        verify(getInjectMock(ElectionEventService.class)).create(any(), any(), eq(false), eq(VotingHierarchy.NONE),
				eq(CountingHierarchy.NONE), any(), anySet());
	}

	@Test
	public void doCreateElectionEvent_withAsynchPropsSet_createsAsynchronously() throws Exception {
		ListElectionEventsController ctrl = setupDefault();
		ctrl.doGetCreateElectionEvent();
		ctrl.getCreateForm().setAllowCopying(true);
		ctrl.getCreateForm().setCopyFromEvent(100L);
		ctrl.getCreateForm().setCopyAreas(true);

		ElectionEvent electionEvent = new ElectionEvent();
		when(
                getInjectMock(ElectionEventService.class).create(any(), any(), eq(false),
						eq(VotingHierarchy.AREA_HIERARCHY),
						eq(CountingHierarchy.AREA_HIERARCHY), any(), anySet())).thenReturn(electionEvent);
        when(getInjectMock(ElectionEventService.class).findById(any(UserData.class), any())).thenReturn(null);

		ctrl.doCreateElectionEvent();

        verify(getInjectMock(ElectionEventService.class)).createAsync(any(), any(), eq(false),
				eq(VotingHierarchy.AREA_HIERARCHY),
				eq(CountingHierarchy.AREA_HIERARCHY), any(), anySet());
	}

	@Test
	public void doCreateElectionEvent_withExistingId_returnsErrorMessage() throws Exception {
		ListElectionEventsController ctrl = setupDefault();
		ctrl.doGetCreateElectionEvent();
		when(getInjectMock(ElectionEventService.class).findById(any(UserData.class), anyString())).thenReturn(createMock(ElectionEvent.class));

		ctrl.doCreateElectionEvent();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ELECTION_ID, @election_level[0].name, null]");
	}

	@Test
	public void valueChangeSelectManyCheckbox_withSelectedLocales_returnsLocales() throws Exception {
		ListElectionEventsController ctrl = setupDefault();
		ValueChangeEvent event = createMock(ValueChangeEvent.class);
		when(event.getNewValue()).thenReturn(asList("1", "2"));

		ctrl.valueChangeSelectManyCheckbox(event);

		assertThat(ctrl.getAvailableLocalesSet()).hasSize(2);
	}

	@Test
	public void isStatusClosed_withClosedEventStatus_returnsTrue() throws Exception {
		ListElectionEventsController ctrl = setupDefault();
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		when(electionEvent.getElectionEventStatus().getId()).thenReturn(ElectionEventStatusEnum.CLOSED.id());

		assertThat(ctrl.isStatusClosed(electionEvent)).isTrue();
	}

	private ListElectionEventsController setupDefault() throws Exception {
		ListElectionEventsController result = initializeMocks(ListElectionEventsController.class);
		stub_findAllElectionEvents();
		stub_getAllLocales();
		stub_findAllElectionEventStatuses();
		URL url = getClass().getResource("/css/themes");
		when(getServletContainer().getServletContextMock().getRealPath("resources/css/themes")).thenReturn(url.getFile());
		when(getMessageProviderMock().get("@common.date.date_pattern")).thenReturn("dd.MM.yyyy");
		when(getMessageProviderMock().get("@common.date.time_pattern")).thenReturn("HH:mm");
		result.init();
		return result;
	}

	private void stub_findAllElectionEvents() {
		List<ElectionEvent> electionEventList = new ArrayList<>(asList(getElectionEvent("ee1"), getElectionEvent("ee2")));
		when(getInjectMock(ElectionEventService.class).findAll(getUserDataMock())).thenReturn(electionEventList);
	}

	private void stub_getAllLocales() {
		List<Locale> result = new ArrayList<>(asList(getElectionEventLocale(1L).getLocale(), getElectionEventLocale(2L).getLocale()));
		when(getInjectMock(TranslationService.class).getAllLocales()).thenReturn(result);
	}

	private void stub_findAllElectionEventStatuses() {
		List<ElectionEventStatus> electionEventStatusList = new ArrayList<>(asList(getElectionEventStatus(1), getElectionEventStatus(2)));
		when(getInjectMock(ElectionEventService.class).findAllElectionEventStatuses(eq(getUserDataMock()))).thenReturn(electionEventStatusList);
	}

	private ElectionEvent getElectionEvent(String id) {
		ElectionEvent result = new ElectionEvent();
		result.setId(id);
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

	private ElectionEventStatus getElectionEventStatus(int id) {
		ElectionEventStatus result = new ElectionEventStatus();
		result.setId(id);
		result.setName("election.event.status[" + id + "]");
		return result;
	}
}

