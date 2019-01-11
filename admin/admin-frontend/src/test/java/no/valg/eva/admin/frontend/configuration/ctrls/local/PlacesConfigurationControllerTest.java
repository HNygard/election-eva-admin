package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.exception.ReadOnlyPrivilegeException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.model.local.Place;
import no.valg.eva.admin.common.configuration.service.PollingPlaceService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PlacesConfigurationControllerTest extends BaseLocalConfigurationControllerTest {
	@Test
	public void init_withNoPollingPlaces_verifyState() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		stub_findAdvancePollingPlacesByArea(new ArrayList<>());

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.ADVANCE_POLLING_PLACES);
		assertThat(ctrl.getPlaces()).isEmpty();
		assertThat(ctrl.getPlace()).isNull();
	}

	@Test
	public void init_withPollingPlacesAndNoOriginalPollingPlace_verifyState() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.ADVANCE_POLLING_PLACES);
		assertThat(ctrl.getPlaces()).hasSize(2);
		assertThat(ctrl.getPlace().getName()).isEqualTo("AdvancePollingPlace 1000");
	}

	@Test
	public void init_withPollingPlacesAndOriginalPollingPlace_verifyState() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		ctrl.setPlace(advancePollingPlace("1001"));

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.ADVANCE_POLLING_PLACES);
		assertThat(ctrl.getPlaces()).hasSize(2);
		assertThat(ctrl.getPlace().getName()).isEqualTo("AdvancePollingPlace 1001");
	}

	@Test(dataProvider = "button")
    public void button_withDataProvider_verifyExpected(ButtonType buttonType, boolean isEditable, ConfigurationMode mode, boolean isRendered, boolean isDisabled)
			throws Exception {
        PlacesConfigurationController ctrl = ctrl();
		stub_isEditable(ctrl, isEditable);
		ctrl.setMode(mode);

		Button button = ctrl.button(buttonType);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@DataProvider(name = "button")
	public Object[][] button() {
		return new Object[][] {
				{ ButtonType.CREATE, false, ConfigurationMode.CREATE, true, true },
				{ ButtonType.CREATE, true, ConfigurationMode.CREATE, true, true },
				{ ButtonType.CREATE, true, ConfigurationMode.READ, true, false },
				{ ButtonType.EXECUTE_CREATE, true, ConfigurationMode.READ, false, true },
				{ ButtonType.EXECUTE_CREATE, false, ConfigurationMode.UPDATE, true, true },
				{ ButtonType.EXECUTE_CREATE, true, ConfigurationMode.UPDATE, true, false },
				{ ButtonType.UPDATE, false, ConfigurationMode.UPDATE, false, true },
				{ ButtonType.UPDATE, false, ConfigurationMode.READ, true, true },
				{ ButtonType.UPDATE, true, ConfigurationMode.READ, true, false },
				{ ButtonType.EXECUTE_UPDATE, true, ConfigurationMode.READ, false, true },
				{ ButtonType.EXECUTE_UPDATE, false, ConfigurationMode.UPDATE, true, true },
				{ ButtonType.EXECUTE_UPDATE, true, ConfigurationMode.UPDATE, true, false },
				{ ButtonType.DELETE, false, ConfigurationMode.READ, false, true },
				{ ButtonType.DELETE, false, ConfigurationMode.UPDATE, true, true },
				{ ButtonType.DELETE, true, ConfigurationMode.UPDATE, true, false },
				{ ButtonType.CANCEL, true, ConfigurationMode.READ, false, true },
				{ ButtonType.CANCEL, false, ConfigurationMode.UPDATE, true, true },
				{ ButtonType.CANCEL, true, ConfigurationMode.UPDATE, true, false },
				{ ButtonType.DONE, true, ConfigurationMode.READ, true, true },
				{ ButtonType.APPROVE_TO_SETTLEMENT, true, ConfigurationMode.READ, false, true }
		};
	}

	@Test
	public void placeSelected_withEvent_verifyState() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		ValueChangeEvent event = createMock(ValueChangeEvent.class);
		AdvancePollingPlace place = advancePollingPlace("1000");
		when(event.getNewValue()).thenReturn(place);
		when(getInjectMock(PollingPlaceService.class).findAdvancePollingPlaceByAreaAndId(eq(getUserDataMock()), any(AreaPath.class),
				eq("0001"))).thenReturn(place);

		ctrl.placeSelected(event);

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
	}

	@Test
	public void saveModel_withExistingModel_verifyUpdate() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		ctrl.getPlace().setName("My place");
		AdvancePollingPlace place = (AdvancePollingPlace) ctrl.getPlace();

		ctrl.saveModel();

		verify(getInjectMock(PollingPlaceService.class)).saveAdvancePollingPlace(getUserDataMock(), ELECTION_PATH_ELECTION_GROUP, place);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.advancepollingplace null]");
		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
	}

	@Test
	public void saveModel_withError_setsUpdateModeTrue() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		doThrow(new ReadOnlyPrivilegeException(""))
				.when(getInjectMock(PollingPlaceService.class))
				.saveAdvancePollingPlace(eq(getUserDataMock()), any(ElectionPath.class), any(AdvancePollingPlace.class));

		ctrl.saveModel();

		assertThat(ctrl.getMode()).isSameAs(UPDATE);
		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.evote_application_exception.READ_ONLY_PRIVILEGE");
	}

	@Test
	public void cancelWrite_verifyState() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();

		ctrl.cancelWrite();

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
		assertThat(ctrl.getPlace().getId()).isEqualTo("1000");
	}

	@Test
	public void getName_returnsCorrectName() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.advance_polling_place.name");
	}

	@Test
	public void isDoneStatus_withAdvancePollingPlaceTrue_returnsTrue() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		when(ctrl.getMunicipalityConfigStatus().isAdvancePollingPlaces()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void setDoneStatus_withTrue_setsAdvancePollingPlaceStatus() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setAdvancePollingPlaces(true);
	}

	@Test
	public void setMarker_withPlace_verifySetMarker() throws Exception {
        PlacesConfigurationController ctrl = advancePollingPlaceCtrl();
		((AdvancePollingPlace) ctrl.getPlace()).setGpsCoordinates("10.123124124, 12.234234234");

		ctrl.setMarker();

		assertThat(ctrl.getMapModel()).isNotNull();
		assertThat(ctrl.getMapModel().getMarkers()).hasSize(1);
		assertThat(ctrl.getMapModel().getMarkers().get(0).getLatlng().getLat()).isEqualTo(10.123124124);
		assertThat(ctrl.getMapModel().getMarkers().get(0).getLatlng().getLng()).isEqualTo(12.234234234);
	}

	private void stub_isEditable(PlacesConfigurationController ctrl, boolean isEditable) {
		when(getInjectMock(UserDataController.class).isLocalConfigurationStatus()).thenReturn(isEditable);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataRedigere()).thenReturn(isEditable);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataGodkjenne()).thenReturn(isEditable);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataOppheve()).thenReturn(isEditable);
		when(ctrl.getMvArea().getMunicipality().isLocalConfigurationStatus()).thenReturn(isEditable);
	}

    private PlacesConfigurationController advancePollingPlaceCtrl() throws Exception {
		PlacesConfigurationController ctrl = ctrl(AdvancePollingPlacesConfigurationController.class, MUNICIPALITY);
		AdvancePollingPlace advancePollingPlace = advancePollingPlace("1000");
		mockFieldValue("place", advancePollingPlace);
		stub_findAdvancePollingPlacesByArea(Arrays.asList(advancePollingPlace, advancePollingPlace("1001")));
		when(ctrl.getMainController().getElectionGroup().getElectionGroupPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
		return ctrl;
	}

	private void stub_findAdvancePollingPlacesByArea(List<AdvancePollingPlace> list) {
		when(getInjectMock(PollingPlaceService.class).findAdvancePollingPlacesByArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(list);
	}

    private PlacesConfigurationController ctrl() throws Exception {
        PlacesConfigurationController ctrl = ctrl(new PlacesConfigurationController() {
            @Override
            List collectPollingPlaces() {
                return null;
            }

            @Override
            Place collectPollingPlace(String id) {
                return null;
            }

            @Override
            Place save(Place place) {
                return null;
            }

            @Override
            public ConfigurationView getView() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            boolean hasAccess() {
                return false;
            }

            @Override
            void setDoneStatus(boolean value) {

            }

            @Override
            boolean isDoneStatus() {
                return false;
            }

            @Override
            boolean canBeSetToDone() {
                return false;
            }
        }, MUNICIPALITY);
        initializeMocks(ctrl);
        return ctrl;
    }
	
	
}

