package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ElectionCardConfigApplicationServiceTest extends LocalConfigApplicationServiceTest {

    @Test
    public void findElectionCardByArea_withMunicipalityArea_returnsElectionCard() throws Exception {
        ElectionCardConfig card = findElectionCardByArea_init(MUNICIPALITY);

        assertThat(card).isNotNull();
        assertThat(card.getInfoText()).isEqualTo("card text");
        assertThat(card.getReportingUnit()).isNotNull();
        assertThat(card.getPlaces()).hasSize(1);
        verify(card.getPlaces().get(0)).setInfoText("card text");
    }

    @Test
    public void findElectionCardByArea_withRootArea_returnsElectionCard() throws Exception {
        ElectionCardConfig card = findElectionCardByArea_init(ROOT);

        assertThat(card).isNotNull();
        assertThat(card.getReportingUnit()).isNotNull();
        assertThat(card.getInfoText()).isNull();
        assertThat(card.getPlaces()).isEmpty();
        assertThat(card.getReportingUnit().getAreaName()).isEqualTo("ElectionEvent 111111");
    }

    private ElectionCardConfig findElectionCardByArea_init(AreaPath path) throws Exception {
        ElectionCardConfigApplicationService service = initializeMocks(ElectionCardConfigApplicationService.class);
        ReportingUnit reportingUnit = stub_getReportingUnit(path);
        if (path.isMunicipalityLevel()) {
            stub_findElectionDayPollingPlacesByArea();
            when(reportingUnit.getMvArea().getMunicipality().getElectionCardText()).thenReturn("card text");
        }
        UserData userData = userData();
        when(userData.getOperatorMvElection().getElectionPath()).thenReturn("111111");

        return service.findElectionCardByArea(userData, path);
    }

    @Test
    public void save_withElectionCard_shouldUpdateUnitMunicipalityAndPlaces() throws Exception {
        ElectionCardConfigApplicationService service = initializeMocks(ElectionCardConfigApplicationService.class);
        ElectionCardConfig card = initSave();
        PollingPlace dbPlace = getInjectMock(PollingPlaceRepository.class).findByPk(anyLong());

        service.save(userData(), card);

        verifyDbReportingUnitUpdate();
        verifyDbMunicipalityUpdate();
        verifyPollingPlaceOperations(dbPlace);
    }

    private void verifyDbReportingUnitUpdate() {
        ReportingUnit dbReportingUnit = getInjectMock(ReportingUnitDomainService.class).getReportingUnit(new UserData(), AREA_PATH_MUNICIPALITY);
        verify(dbReportingUnit).setAddressLine1("New address");
        verify(dbReportingUnit).setAddressLine2(null);
        verify(dbReportingUnit).setAddressLine3(null);
        verify(dbReportingUnit).setPostalCode("0123");
        verify(dbReportingUnit).setPostTown("By");
    }

    private void verifyDbMunicipalityUpdate() {
        Municipality dbMunicipality = getInjectMock(MunicipalityRepository.class).findByPk(10L);
        verify(dbMunicipality).setElectionCardText("New info text");
    }

    private void verifyPollingPlaceOperations(PollingPlace dbPlace) {
        ArgumentCaptor<PollingPlace> createCaptor = ArgumentCaptor.forClass(PollingPlace.class);
        verify(getInjectMock(PollingPlaceDomainService.class)).create(any(UserData.class), createCaptor.capture());
        verify(createCaptor.getValue()).setInfoText("InfoText 1000");

        verify(dbPlace).setInfoText("InfoText 1001");
    }

    private ElectionCardConfig initSave() {
        ReportingUnit dbReportingUnit = stub_getReportingUnit(MUNICIPALITY);
        no.valg.eva.admin.common.configuration.model.local.ReportingUnit reportingUnit = reportingUnit(MUNICIPALITY);
        ElectionCardConfig result = new ElectionCardConfig(reportingUnit, 0);
        result.setInfoText("New info text");
        when(result.getReportingUnit().getAddress()).thenReturn("New address");
        when(result.getReportingUnit().getPostalCode()).thenReturn("0123");
        when(result.getReportingUnit().getPostTown()).thenReturn("By");
        stub_municipalityRepository_findByPk(dbReportingUnit.getMvArea().getMunicipality());
        // Places
        List<ElectionDayPollingPlace> places = new ArrayList<>();
        places.add(electionDayPollingPlace("1000"));
        when(places.get(0).getPk()).thenReturn(null);
        places.add(electionDayPollingPlace("1001"));
        result.setPlaces(places);
        return result;
    }

    private List<ElectionDayPollingPlace> stub_findElectionDayPollingPlacesByArea() {
        List<ElectionDayPollingPlace> list = singletonList(electionDayPollingPlace("1000"));
        when(getInjectMock(PollingPlaceDomainService.class).findElectionDayPollingPlacesByArea(any(UserData.class), eq(MUNICIPALITY)))
                .thenReturn(list);
        when(list.get(0).getPk()).thenReturn(null);
        return list;
    }

    private ReportingUnit stub_getReportingUnit(AreaPath path) {
        ReportingUnit reportingUnit = reportingUnitEntity(path);
        when(getInjectMock(ReportingUnitDomainService.class).getReportingUnit(any(UserData.class), any(AreaPath.class))).thenReturn(reportingUnit);
        return reportingUnit;
    }

}
