package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PollingPlaceApplicationServiceTest extends LocalConfigApplicationServiceTest {

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void saveAdvancePollingPlace_withNoPollingDistricts_throwsEntityNotFoundException() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        UserData userData = userData();
        AdvancePollingPlace place = createMock(AdvancePollingPlace.class);
        when(place.getPk()).thenReturn(null);
        Municipality municipality = stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).getValue());
        doThrow(new EntityNotFoundException()).when(municipality).getMunicipalityPollingDistrict();

        service.saveAdvancePollingPlace(userData, ELECTION_PATH_ELECTION_GROUP, place);
    }

    @Test
    public void saveAdvancePollingPlace_withPollingDistrict_verifyCreate() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        UserData userData = userData();
        AdvancePollingPlace place = new AdvancePollingPlace(MUNICIPALITY);
        place.setId("0001");
        place.setName("My place");
        stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withPollingDistricts(pollingDistrictEntity("0000")).getValue());

        service.saveAdvancePollingPlace(userData, ELECTION_PATH_ELECTION_GROUP, place);

        ArgumentCaptor<PollingPlace> captor = ArgumentCaptor.forClass(PollingPlace.class);

        verify(getInjectMock(PollingPlaceRepository.class)).create(eq(userData), captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("0001");
        assertThat(captor.getValue().getName()).isEqualTo("My place");
        assertThat(captor.getValue().isElectionDayVoting()).isEqualTo(false);
        assertThat(captor.getValue().getUsingPollingStations()).isEqualTo(false);
        assertThat(captor.getValue().getAddressLine2()).isNull();
        assertThat(captor.getValue().getAddressLine3()).isNull();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void saveAdvancePollingPlace_withInvalidLevel_throwsIllegalArgumentException() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        AdvancePollingPlace place = new AdvancePollingPlace(COUNTY);

        service.saveAdvancePollingPlace(userData(), ELECTION_PATH_ELECTION_GROUP, place);
    }

    @Test
    public void saveAdvancePollingPlace_withValidLevel_verifyUpdate() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        AdvancePollingPlace place = new AdvancePollingPlace(AreaPath.from(MUNICIPALITY_POLLING_DISTRICT.path() + ".1000"));
        place.setPk(10L);
        place.setId("0002");
        place.setName("My place");
        PollingPlace pollingPlace = createMock(PollingPlace.class);
        when(getInjectMock(PollingPlaceRepository.class).findByPk(10L)).thenReturn(pollingPlace);

        service.saveAdvancePollingPlace(userData(), ELECTION_PATH_ELECTION_GROUP, place);

        verify(pollingPlace).setId("0002");
        verify(pollingPlace).setName("My place");
    }

    @Test
    public void deleteAdvancePollingPlace_withPlace_verifyDelete() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        UserData userData = userData();
        AdvancePollingPlace place = new AdvancePollingPlace(MUNICIPALITY);
        PollingPlace pollingPlace = createMock(PollingPlace.class);
        when(getInjectMock(PollingPlaceRepository.class).findByPk(anyLong())).thenReturn(pollingPlace);

        service.deleteAdvancePollingPlace(userData, place);

        verify(getInjectMock(PollingPlaceRepository.class)).delete(userData, pollingPlace.getPk());
    }

    @Test
    public void findAdvancePollingPlacesByArea_withPollingPlaces_verifyResult() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withAdvancePollingPlaces("0001", "0002", "0003").getValue());

        List<AdvancePollingPlace> result = service.findAdvancePollingPlacesByArea(userData(), MUNICIPALITY);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo("0001");
        assertThat(result.get(1).getId()).isEqualTo("0002");
        assertThat(result.get(2).getId()).isEqualTo("0003");
    }

    @Test
    public void findAdvancePollingPlacesByAreaAndId_withPollingPlaces_verifyResult() throws Exception {
        PollingPlaceApplicationService service = initializeMocks(PollingPlaceApplicationService.class);
        stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withAdvancePollingPlaces("0001", "0002").getValue());

        assertThat(service.findAdvancePollingPlaceByAreaAndId(userData(), MUNICIPALITY, "0002")).isNotNull();
    }
}

