package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.evote.service.configuration.ReportCountCategoryServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityLocalConfigStatus;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MunicipalityApplicationServiceTest extends MockUtilsTestCase {

	@Test
	public void saveMunicipalityConfigStatus_withExisting_updatesExisting() throws Exception {
		MunicipalityApplicationService service = initializeMocks(MunicipalityApplicationService.class);
		MunicipalityConfigStatus status = municipalityConfigStatus(true, false, false);
		Municipality municipality = stub_municipalityByElectionEventAndId(createMock(Municipality.class));

		service.saveMunicipalityConfigStatus(createMock(UserData.class), status, ELECTION_PATH_ELECTION_GROUP);

		verify(municipality).updateStatus(status);
		verify(municipality).setLocale(any(Locale.class));
		verify(municipality).setElectronicMarkoffs(anyBoolean());
	}

	@Test
	public void findMunicipalityStatusByArea_withExisting_verifyResult() throws Exception {
		MunicipalityApplicationService service = initializeMocks(MunicipalityApplicationService.class);
		Municipality municipality = stub_municipalityByElectionEventAndId(createMock(Municipality.class));
		MunicipalityLocalConfigStatus dbStatus = createMock(MunicipalityLocalConfigStatus.class);
		when(municipality.getLocalConfigStatus()).thenReturn(dbStatus);

		MunicipalityConfigStatus result = service.findMunicipalityStatusByArea(createMock(UserData.class), AreaPath.from("111111.22.33.4444"));

		assertThat(result.isReportingUnitStemmestyre()).isEqualTo(dbStatus.isReportingUnitStemmestyre());
		assertThat(result.isLanguage()).isEqualTo(dbStatus.isLanguage());
		assertThat(result.isListProposals()).isEqualTo(dbStatus.isListProposals());
	}

	@Test
	public void maintainIntegrity_withElectronicMarkoffAndElectionDayDone_setAllElectionDayPlacesPollingStationToFalse() throws Exception {
		MunicipalityApplicationService service = initializeMocks(MunicipalityApplicationService.class);
		MunicipalityConfigStatus status = new MunicipalityConfigStatus(AREA_PATH_MUNICIPALITY, "Name");
		status.setElectionPollingPlaces(true);
		status.setUseElectronicMarkoffs(true);
		Municipality municipality = createMock(Municipality.class);
		when(municipality.getLocalConfigStatus().isElectionPollingPlaces()).thenReturn(false);
		List<PollingPlace> places = Arrays.asList(
				pollingPlace(true),
				pollingPlace(false));
		when(municipality.pollingPlaces()).thenReturn(places);

		service.maintainIntegrity(createMock(UserData.class), status, municipality, createMock(ElectionGroup.class));

		verify(places.get(0)).setUsingPollingStations(false);
		verify(places.get(1), never()).setUsingPollingStations(false);
	}

	@Test
	public void maintainIntegrity_withCountCategoryVoAndCentral_removesAllChildrenAndParentDistricts() throws Exception {
		MunicipalityApplicationService service = initializeMocks(MunicipalityApplicationService.class);
		MunicipalityConfigStatus status = new MunicipalityConfigStatus(AREA_PATH_MUNICIPALITY, "Name");
		status.setCountCategories(true);
		Municipality municipality = createMock(Municipality.class);
		when(municipality.getLocalConfigStatus().isCountCategories()).thenReturn(false);
		when(getInjectMock(ReportCountCategoryServiceBean.class).isValgtingOrdinaereAndSentraltSamlet(any(Municipality.class),
				any(ElectionGroup.class))).thenReturn(true);
		parents(municipality, 2);
		List<PollingDistrict> children = children(municipality, 2);

		service.maintainIntegrity(createMock(UserData.class), status, municipality, createMock(ElectionGroup.class));

		verify(children.get(0)).setPollingDistrict(null);
		verify(children.get(1)).setPollingDistrict(null);
		verify(getInjectMock(PollingDistrictRepository.class)).delete(any(UserData.class), eq(1L));
		verify(getInjectMock(PollingDistrictRepository.class)).delete(any(UserData.class), eq(2L));
	}

	@Test
	public void markerAvkryssningsmanntallKjort_medTrue_setterRiktigFlagg() throws Exception {
		MunicipalityApplicationService service = initializeMocks(MunicipalityApplicationService.class);
		Municipality municipality = stub_municipalityByElectionEventAndId(createMock(Municipality.class));

		service.markerAvkryssningsmanntallKjort(createMock(UserData.class), createMock(AreaPath.class), true);

		verify(municipality).setAvkrysningsmanntallKjort(true);
	}

	private List<PollingDistrict> parents(Municipality municipality, int size) {
		List<PollingDistrict> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			PollingDistrict pd = createMock(PollingDistrict.class);
			when(pd.getPk()).thenReturn((long) i + 1);
			result.add(pd);
		}
		when(municipality.parentPollingDistricts()).thenReturn(result);
		return result;
	}

	private List<PollingDistrict> children(Municipality municipality, int size) {
		List<PollingDistrict> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(createMock(PollingDistrict.class));
		}
		when(municipality.childPollingDistricts()).thenReturn(result);
		return result;
	}

	private PollingPlace pollingPlace(boolean usingPollingStations) {
		PollingPlace result = createMock(PollingPlace.class);
		when(result.isElectionDayVoting()).thenReturn(true);
		when(result.getUsingPollingStations()).thenReturn(usingPollingStations);
		return result;
	}

	private Municipality stub_municipalityByElectionEventAndId(Municipality municipality) {
        when(getInjectMock(MunicipalityRepository.class).municipalityByElectionEventAndId(anyLong(), any())).thenReturn(municipality);
		return municipality;
	}

	private MunicipalityConfigStatus municipalityConfigStatus(boolean isReportingUnitStemmestyre, boolean isLanguage, boolean isListProposals) {
		MunicipalityConfigStatus status = createMock(MunicipalityConfigStatus.class);
		when(status.isReportingUnitStemmestyre()).thenReturn(isReportingUnitStemmestyre);
		when(status.isLanguage()).thenReturn(isLanguage);
		when(status.isListProposals()).thenReturn(isListProposals);
		return status;
	}

}
