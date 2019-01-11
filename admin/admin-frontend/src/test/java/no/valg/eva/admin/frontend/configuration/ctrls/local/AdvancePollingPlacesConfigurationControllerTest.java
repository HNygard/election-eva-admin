package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.application.map.Address;
import no.valg.eva.admin.application.map.GpsCoordinates;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.service.PollingPlaceService;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdvancePollingPlacesConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

    @Test
    public void initCreate_verifyState() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();

        ctrl.initCreate();

        assertThat(ctrl.getPlace()).isNotNull();
        assertThat(ctrl.getPlace().getPath().path()).isEqualTo(MUNICIPALITY.path());
        assertThat(ctrl.getPlace().isPublicPlace()).isTrue();
        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.CREATE);
    }

    @Test
    public void saveModel_withNewModel_verifyCreate() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.initCreate();
        ctrl.getPlace().setName("My place");
        AdvancePollingPlace place = ctrl.getPlace();

        ctrl.saveModel();

        verify(getInjectMock(PollingPlaceService.class)).saveAdvancePollingPlace(getUserDataMock(), ELECTION_PATH_ELECTION_GROUP, place);
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.advancepollingplace null]");
        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
    }

    @Test
    public void confirmDelete_withPlace_verifyDelete() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        AdvancePollingPlace place = ctrl.getPlace();

        ctrl.confirmDelete();

        verify(getInjectMock(PollingPlaceService.class)).deleteAdvancePollingPlace(getUserDataMock(), place);
        assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.deleted, @common.displayable.advancepollingplace AdvancePollingPlace 1000]");
        assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
        assertThat(ctrl.getPlace().getId()).isEqualTo("1000");
    }

    @Test
    public void saveModel_withDuplicateIdError_returnsErrorMessage() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        doThrow(new EvoteException(ErrorCode.ERROR_CODE_0505_UNIQUE_CONSTRAINT_VIOLATION, null, "Something is rotten", "Something is rotten"))
                .when(getInjectMock(PollingPlaceService.class)).saveAdvancePollingPlace(getUserDataMock(), ELECTION_PATH_ELECTION_GROUP, ctrl.getPlace());

        ctrl.saveModel();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@database.error.unique_constraint_violation, b5e4bcfa]");
    }

    @Test
    public void canBeSetToDone_withValidPlaces_returnsTrue() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.init();

        assertThat(ctrl.canBeSetToDone()).isTrue();
    }

    @Test
    public void canBeSetToDone_withInvalidPlaces_returnsFalse() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.init();
        ctrl.getPlaces().get(0).setId(null);

        assertThat(ctrl.canBeSetToDone()).isFalse();
    }

    private AdvancePollingPlacesConfigurationController ctrl() throws Exception {
        AdvancePollingPlacesConfigurationController result = ctrl(AdvancePollingPlacesConfigurationController.class, MUNICIPALITY);
        AdvancePollingPlace advancePollingPlace = advancePollingPlace("1000");
        mockFieldValue("place", advancePollingPlace);
        stub_findAdvancePollingPlacesByArea(asList(advancePollingPlace, advancePollingPlace("1001")));
        when(result.getMainController().getElectionGroup().getElectionGroupPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
        return result;
    }

    private void stub_findAdvancePollingPlacesByArea(List<AdvancePollingPlace> list) {
        when(getInjectMock(PollingPlaceService.class).findAdvancePollingPlacesByArea(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(list);
    }

    @Test
    public void addressLookup_ifPlaceIsPublicHasAddressPostalCodeAndPostTown() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.initCreate();
        mockMapServiceResultsFor(ctrl);

        ctrl.getPlace().setAddress("Tjuklandsgatan 1");
        ctrl.getPlace().setPostalCode("0405");
        ctrl.getPlace().setPostTown("Oslo");
        ctrl.getPlace().setPublicPlace(true);
        ctrl.doAddressLookup(null);

        verify(getRequestContextMock()).update(addressLookupComponentUpdatedList());
    }

    private void mockMapServiceResultsFor(AdvancePollingPlacesConfigurationController ctrl) {
        when(ctrl.getMapService().addressSearch(any()).getAddresses())
                .thenReturn(singletonList(Address.builder().gpsCoordinates(new GpsCoordinates(59.500296, 8.186186)).build()));
    }

    private List<String> addressLookupComponentUpdatedList() {
        return asList(
                "configurationPanel:0:advancePollingPlace:form:gpsCoordinates",
                "configurationPanel:0:advancePollingPlace:form:gpsCoordinatesError",
                "configurationPanel:0:advancePollingPlace:form:map");
    }

    @Test(dataProvider = "invalidAddressInput")
    public void noAddressLookup_givenInvalidInput(String address, String postalCode, String postTown, boolean isPublicPlace) throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.initCreate();
        mockMapServiceResultsFor(ctrl);

        ctrl.getPlace().setAddress(address);
        ctrl.getPlace().setPostalCode(postalCode);
        ctrl.getPlace().setPostTown(postTown);
        ctrl.getPlace().setPublicPlace(isPublicPlace);
        ctrl.doAddressLookup(null);

        verify(getRequestContextMock(), never()).update(addressLookupComponentUpdatedList());
    }

    @DataProvider
    private Object[][] invalidAddressInput() {
        return new Object[][]{
                {"Tjuklandsgatan 2", "0405", "Oslo", false},
                {"Tjuklandsgatan 2", "0405", null, true},
                {"Tjuklandsgatan 2", null, "Oslo", true},
                {null, "0405", "Oslo", true},
                {null, null, null, true},
                {null, null, null, false},
        };
    }

    @Test
    public void addressFound_setsGpsCoordinates() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.initCreate();
        mockMapServiceResultsFor(ctrl);

		assertThat(ctrl.getPlace().getGpsCoordinates()).isNullOrEmpty();

		ctrl.getPlace().setPublicPlace(true);
		ctrl.getPlace().setAddress("Høydalsmoveien 1234");
		ctrl.getPlace().setPostalCode("3880");
		ctrl.getPlace().setPostTown("Dalen");
        ctrl.doAddressLookup(null);

        assertThat(ctrl.getPlace().getGpsCoordinates()).isNotEmpty();
    }

    @Test
    public void addressNotFound_setsGpsCoordinatesBlank() throws Exception {
        AdvancePollingPlacesConfigurationController ctrl = ctrl();
        ctrl.initCreate();

        ctrl.getPlace().setGpsCoordinates("59.1234, 12.4321");
        assertThat(ctrl.getPlace().getGpsCoordinates()).isNotEmpty();

        ctrl.getPlace().setPublicPlace(true);
        ctrl.getPlace().setAddress("Høydalsmoveien 1234");
        ctrl.getPlace().setPostalCode("3880");
        ctrl.getPlace().setPostTown("Dalen");
        when(ctrl.getMapService().addressSearch(any()).getAddresses()).thenReturn(emptyList());

        ctrl.doAddressLookup(null);

        assertThat(ctrl.getPlace().getGpsCoordinates()).isEmpty();
    }
}
