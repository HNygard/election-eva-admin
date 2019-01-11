package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.evote.service.configuration.PollingStationServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollingStationApplicationServiceTest extends LocalConfigApplicationServiceTest {

	@Test
	public void findPollingStationsByArea_withElectronicMarkoffs_returnsEmptyList() throws Exception {
		PollingStationApplicationService service = initializeMocks(PollingStationApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(true));

		assertThat(service.findPollingStationsByArea(createMock(UserData.class), POLLING_PLACE)).isEmpty();
	}

	@Test
	public void findPollingStationsByArea_withoutElectronicMarkoffs_returnsPollingStations() throws Exception {
		PollingStationApplicationService service = initializeMocks(PollingStationApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(false));
		stub_getDivisionListForPollingPlace();

		List<Rode> result = service.findPollingStationsByArea(createMock(UserData.class), POLLING_PLACE);

		assertThat(result).hasSize(1);
	}

	@Test
	public void findPollingStationsByAreaCalculated_withoutElectronicMarkoffs_performsCalculation() throws Exception {
		PollingStationApplicationService service = initializeMocks(PollingStationApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(false));

		service.findPollingStationsByAreaCalculated(createMock(UserData.class), POLLING_PLACE, 2);

		verify(getInjectMock(PollingStationServiceBean.class)).getPollingStationDivision(eq(2), any(PollingPlace.class));

	}

	@Test
	public void recalculatedPollingStationsByArea_withoutElectronicMarkoffs_performsRecalculation() throws Exception {
		PollingStationApplicationService service = initializeMocks(PollingStationApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(false));

		service.recalculatedPollingStationsByArea(createMock(UserData.class), POLLING_PLACE, new ArrayList<>());

		verify(getInjectMock(PollingStationServiceBean.class)).getPollingStationDivision(anyList(), any(PollingPlace.class));
	}

	@Test
	public void save_withoutElectronicMarkoffs_savesStationsAndPerformsFindAgain() throws Exception {
		PollingStationApplicationService service = initializeMocks(PollingStationApplicationService.class);
		UserData userData = createMock(UserData.class);
		stub_municipalityByElectionEventAndId(municipality(false));

		service.save(userData, POLLING_PLACE, new ArrayList<>());

		verify(getInjectMock(PollingStationServiceBean.class)).savePollingStationConfiguration(eq(userData), any(PollingPlace.class), anyList());
		verify(getInjectMock(PollingStationServiceBean.class)).getDivisionListForPollingPlace(any(PollingPlace.class));
	}

	private Municipality municipality(boolean isElectronicMarkoffs) {
		Municipality municipality = new MunicipalityBuilder(AreaPath.OSLO_MUNICIPALITY_ID).getValue();
		when(municipality.isElectronicMarkoffs()).thenReturn(isElectronicMarkoffs);
		PollingPlace place = pollingPlaceEntity("1000");
		when(place.getUsingPollingStations()).thenReturn(true);
		when(place.areaPath()).thenReturn(POLLING_PLACE);
        when(municipality.pollingPlaces()).thenReturn(Collections.singletonList(place));
		return municipality;
	}

	private List<Rode> stub_getDivisionListForPollingPlace() {
        List<Rode> result = Collections.singletonList(createMock(Rode.class));
		when(getInjectMock(PollingStationServiceBean.class).getDivisionListForPollingPlace(any(PollingPlace.class))).thenReturn(result);
		return result;
	}
}
