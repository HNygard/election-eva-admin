package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.application.map.Address;
import no.valg.eva.admin.application.map.GpsCoordinates;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ElectionDayPollingPlacesConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

    @Test
    public void addressFound_setsGpsCoordinates() throws Exception {
        ElectionDayPollingPlacesConfigurationController ctrl = ctrl();
        
        assertThat(ctrl.getSelectedViewModel().getGpsCoordinates()).isNullOrEmpty();

        assertThat(ctrl.getSelectedViewModel().getGpsCoordinates()).isNullOrEmpty();

        ctrl.getSelectedViewModel().setAddress("Høydalsmoveien 1234");
        ctrl.getSelectedViewModel().setPostalCode("3880");
        ctrl.getSelectedViewModel().setPostTown("Dalen");
        when(ctrl.getMapService().addressSearch(any()).getAddresses())
                .thenReturn(singletonList(Address.builder().gpsCoordinates(new GpsCoordinates(59.500296, 8.186186)).build()));

        ctrl.doAddressLookup(null);

        assertThat(ctrl.getSelectedViewModel().getGpsCoordinates()).isNotEmpty();
    }

    @Test
    public void addressNotFound_setsGpsCoordinatesBlank() throws Exception {
        ElectionDayPollingPlacesConfigurationController ctrl = ctrl();

        ctrl.getSelectedViewModel().setGpsCoordinates("59.4321, 11.1234");
        assertThat(ctrl.getSelectedViewModel().getGpsCoordinates()).isNotEmpty();

        ctrl.getSelectedViewModel().setGpsCoordinates("59.4321, 11.1234");
        assertThat(ctrl.getSelectedViewModel().getGpsCoordinates()).isNotEmpty();

        ctrl.getSelectedViewModel().setAddress("Høydalsmoveien 1234");
        ctrl.getSelectedViewModel().setPostalCode("3880");
        ctrl.getSelectedViewModel().setPostTown("Dalen");
        when(ctrl.getMapService().addressSearch(any()).getAddresses()).thenReturn(emptyList());

        ctrl.doAddressLookup(null);

        assertThat(ctrl.getSelectedViewModel().getGpsCoordinates()).isEmpty();
    }

    private ElectionDayPollingPlacesConfigurationController ctrl() throws Exception {
        ElectionDayPollingPlacesConfigurationController ctrl = ctrl(ElectionDayPollingPlacesConfigurationController.class, MUNICIPALITY);
        ctrl.setSelectedViewModel(new ElectionDayPollingPlaceViewModel(new ElectionDayPollingPlace(MUNICIPALITY), emptyList()));
        return ctrl;
    }
}
