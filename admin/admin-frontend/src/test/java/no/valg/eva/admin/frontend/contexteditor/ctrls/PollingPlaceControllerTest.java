package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.configuration.LegacyPollingPlaceService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaPickerController;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PollingPlaceControllerTest extends BaseFrontendTest {

	@Test
	public void changePollingPlaceType_withNoneType_verifyNoInteractionsOnNewPollingPlace() throws Exception {
		PollingPlaceController ctrl = initializeMocks(PollingPlaceController.class);
		ValueChangeEvent eventMock = createMock(ValueChangeEvent.class);
		PollingPlace newPollingPlace = mockField("pollingPlace", PollingPlace.class);
		when(eventMock.getNewValue()).thenReturn(PollingPlaceController.POLLING_PLACE_NONE);

		ctrl.changePollingPlaceType(eventMock);

		verifyNoMoreInteractions(newPollingPlace);
	}

	@Test
	public void changePollingPlaceType_withAdvanceType_verifyCorrectInteractionsOnNewPollingPlace() throws Exception {
		PollingPlaceController ctrl = initializeMocks(PollingPlaceController.class);
		ValueChangeEvent eventMock = createMock(ValueChangeEvent.class);
		PollingPlace newPollingPlace = mockField("pollingPlace", PollingPlace.class);
		when(eventMock.getNewValue()).thenReturn(PollingPlaceController.ADVANCE_POLLING_PLACE);
		when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);

		ctrl.changePollingPlaceType(eventMock);

		verify(newPollingPlace).setElectionDayVoting(false);
		verify(newPollingPlace).setAdvanceVoteInBallotBox(true);
	}

	@Test
	public void changePollingPlaceType_withElectionTypeType_verifyCorrectInteractionsOnNewPollingPlace() throws Exception {
		PollingPlaceController ctrl = initializeMocks(PollingPlaceController.class);
		ValueChangeEvent eventMock = createMock(ValueChangeEvent.class);
		PollingPlace newPollingPlace = mockField("pollingPlace", PollingPlace.class);
		when(eventMock.getNewValue()).thenReturn(PollingPlaceController.ELECTION_DAY_VOTING_PLACE);
		when(getUserDataMock().getOperatorAreaPath().isRootLevel()).thenReturn(true);

		ctrl.changePollingPlaceType(eventMock);

		verify(newPollingPlace).setElectionDayVoting(true);
	}

	@Test
	public void doCreatePollingPlace_withExistingId_returnsExistError() throws Exception {
		PollingPlaceController ctrl = setup_newPollingPlace(true);
		ctrl.setPollingPlace(ctrl.getPollingPlace());

		ctrl.doCreatePollingPlace();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, @area_level[6].nummer, null, null]");
		verify(getRequestContextMock()).addCallbackParam("createPollingPlaceHideDialog", false);
	}

	@Test
	public void doCreatePollingPlace_withCreateException_returnsErrorMessage() throws Exception {
		PollingPlaceController ctrl = setup_newPollingPlace(false);
		evoteExceptionWhen(LegacyPollingPlaceService.class, "@hello").create(getUserDataMock(), ctrl.getPollingPlace());
		ctrl.setPollingPlace(ctrl.getPollingPlace());

		ctrl.doCreatePollingPlace();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@hello");
		verify(getRequestContextMock()).addCallbackParam("createPollingPlaceHideDialog", false);
	}

	@Test
	public void doCreatePollingPlace_withValidInput_returnsErrorMessage() throws Exception {
		PollingPlaceController ctrl = setup_newPollingPlace(false);
		PollingPlace newPollingPlace = createMock(PollingPlace.class);
		when(newPollingPlace.getName()).thenReturn("Marienlyst skole");
		when(getInjectMock(LegacyPollingPlaceService.class).create(eq(getUserDataMock()), any(PollingPlace.class))).thenReturn(newPollingPlace);
		ctrl.getPollingPlace().setId("0401");
		ctrl.setPollingPlace(newPollingPlace);

		ctrl.doCreatePollingPlace();

		verify(newPollingPlace).setPollingDistrict(any(PollingDistrict.class));
		verify(newPollingPlace).setAdvanceVoteInBallotBox(true);
		verify(getInjectMock(MvAreaPickerController.class)).update(eq(AreaLevelEnum.POLLING_PLACE.getLevel()), anyString());
		assertThat(ctrl.getPollingPlace().getId()).isNull();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_create.successful, Marienlyst skole, null]");
	}

	private void stub_findPollingPlaceById(PollingPlace pollingPlace) {
        when(getInjectMock(LegacyPollingPlaceService.class).findPollingPlaceById(eq(getUserDataMock()), anyLong(), any())).thenReturn(pollingPlace);
	}

	private PollingPlaceController setup_newPollingPlace(boolean idExists) throws Exception {
		PollingPlaceController ctrl = initializeMocks(PollingPlaceController.class);
		MvArea mvAreaMock = createMock(MvArea.class);
		ctrl.setParentMvArea(mvAreaMock);
		PollingPlace newPollingPlace = ctrl.getPollingPlace();
		if (idExists) {
			stub_findPollingPlaceById(newPollingPlace);
		} else {
			stub_findPollingPlaceById(null);
		}
		return ctrl;
	}

}
