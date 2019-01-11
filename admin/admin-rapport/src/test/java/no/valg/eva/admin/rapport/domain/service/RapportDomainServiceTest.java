package no.valg.eva.admin.rapport.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.service.BoroughElectionDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rapport.application.ValghendelsesRapportMapper;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;
import no.valg.eva.admin.rapport.domain.model.Report;
import no.valg.eva.admin.rapport.repository.ElectionEventReportRepository;
import no.valg.eva.admin.rapport.repository.ReportRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.evote.util.MockUtils.setPrivateField;
import static no.valg.eva.admin.common.ElectionPath.from;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111113;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class RapportDomainServiceTest extends MockUtilsTestCase {

    private static final ElectionPath ELECTION_EVENT_PATH = from("200701");

    @Test
    public void rapporterForValghendelse_withReportAndNoElectionEventReports_returnsSynligValghendelsesRapport() throws Exception {
        RapportDomainService service = initializeMocks(RapportDomainService.class);
        setPrivateField(service, "valghendelsesRapportMapper", new ValghendelsesRapportMapper(), true);
        stub_reportRepository_findAll(1);
        stub_electionEventReportRepository_findByElectionEventPath(new ArrayList<>());

        List<ValghendelsesRapport> reports = service.rapporterForValghendelse(ELECTION_EVENT_PATH);

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).isSynlig()).isTrue();
    }

    @Test
    public void rapporterForValghendelse_withOneElectionEventReports_returnsTwoValghendelsesRapport() throws Exception {
        RapportDomainService service = initializeMocks(RapportDomainService.class);
        setPrivateField(service, "valghendelsesRapportMapper", new ValghendelsesRapportMapper(), true);
        stub_electionEventReportRepository_findByElectionEventPath(singletonList(electionEventReport(stub_reportRepository_findAll(2).get(0))));

        List<ValghendelsesRapport> reports = service.rapporterForValghendelse(ELECTION_EVENT_PATH);

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).isSynlig()).isTrue();
        assertThat(reports.get(1).isSynlig()).isFalse();
    }

    @Test
    public void rapporterForBruker_withTenReports_returnsTenReports() throws Exception {
        RapportDomainService service = initializeMocks(RapportDomainService.class);
        setPrivateField(service, "valghendelsesRapportMapper", new ValghendelsesRapportMapper(), true);
        stub_reportRepository_findAll(10);
        stub_electionEventReportRepository_findByElectionEventPath(new ArrayList<>());

        UserData userData = createMock(UserData.class);
        when(userData.hasAccess(any(Accesses.class))).thenReturn(true);
        when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);

        ElectionPath operatorElectionPath = ELECTION_EVENT_PATH;
        when(userData.getOperatorElectionPath()).thenReturn(operatorElectionPath);

        AreaPath operatorMvAreaPath = AREA_PATH_111111_11_11_1111_111111_1111_1111_11;
        when(userData.getOperatorMvArea().areaPath()).thenReturn(operatorMvAreaPath);

        when(getInjectMock(BoroughElectionDomainService.class)
                .electionPathAndMvAreaHasAccessToBoroughs(operatorElectionPath, operatorMvAreaPath)).thenReturn(true);

        List<ValghendelsesRapport> reports = service.rapporterForBruker(userData, ELECTION_EVENT_PATH);

        assertThat(reports).hasSize(10);
    }

    @Test
    public void isAvkryssingsMantallRapportDisabled_withNonXiM_returnsFalse() throws Exception {
        RapportDomainService service = initializeMocks(RapportDomainService.class);
        UserData userData = createMock(UserData.class);
        when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
        stub_municipalityByElectionEventAndId(false);

        assertThat(service.isAvkryssingsMantallRapportDisabled(userData)).isFalse();
    }

    @Test(dataProvider = "isAvkryssingsMantallRapportDisabled")
    public void isAvkryssingsMantallRapportDisabled_withDataProvider_verifyExpected(boolean settled, boolean expected) throws Exception {
        RapportDomainService service = initializeMocks(RapportDomainService.class);
        UserData userData = createMock(UserData.class);
        when(userData.getOperatorAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
        stub_municipalityByElectionEventAndId(true);
        stub_findContestsByElectionEventAndAreas();
        stub_isSettlementDone(settled);

        assertThat(service.isAvkryssingsMantallRapportDisabled(userData)).isEqualTo(expected);
    }

    @Test(dataProvider = "forTestUserHasAccessToBoroughProtocols")
    public void testUserHasAccessToBoroughProtocols(no.valg.eva.admin.common.rbac.Access access, boolean hasAccessToBoroughs, boolean expectingAccess) throws Exception {
        RapportDomainService service = initializeMocks(RapportDomainService.class);
        UserData userData = createMock(UserData.class);

        ElectionPath operatorElectionPath = ELECTION_PATH_111111_11_11_111113;
        when(userData.getOperatorElectionPath()).thenReturn(operatorElectionPath);

        AreaPath operatorMvAreaPath = AREA_PATH_111111_11_11_1111_111111_1111_1111_11;
        when(userData.getOperatorMvArea().areaPath()).thenReturn(operatorMvAreaPath);

        ValghendelsesRapport valghendelsesRapport = mock(ValghendelsesRapport.class);
        when(valghendelsesRapport.getAccess()).thenReturn(access);

        when(getInjectMock(BoroughElectionDomainService.class)
                .electionPathAndMvAreaHasAccessToBoroughs(operatorElectionPath, operatorMvAreaPath)).thenReturn(hasAccessToBoroughs);

        assertEquals(service.userHasAccessToBoroughProtocols(userData, valghendelsesRapport), expectingAccess);
    }

    @DataProvider
    private Object[][] forTestUserHasAccessToBoroughProtocols() {
        return new Object[][]{
                {Accesses.Rapport_Møtebøker_Stemmestyre.getAccess(), true, false},
                {Accesses.Rapport_Møtebøker_Stemmestyre.getAccess(), false, false},
                {Accesses.Rapport_Møtebøker_Bydelsutvalg.getAccess(), false, false},
                {Accesses.Rapport_Møtebøker_Bydelsutvalg.getAccess(), true, true}
        };
    }
    
    @DataProvider
    public Object[][] isAvkryssingsMantallRapportDisabled() {
        return new Object[][]{
                {true, false},
                {false, true}
        };
    }

    private List<Report> stub_reportRepository_findAll(int size) {
        List<Report> reports = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            reports.add(report(i + 1));
        }
        when(getInjectMock(ReportRepository.class).findAll()).thenReturn(reports);
        return reports;
    }

    private Report report(long id) {
        Report result = new Report();
        result.setPk(id);
        result.setId("report_" + id);
        result.setCategory(GRUNNLAGSDATA);
        result.setAccess(createMock(Access.class));
        when(result.getAccess().toViewObject()).thenReturn(access());
        return result;
    }

    private List<ElectionEventReport> stub_electionEventReportRepository_findByElectionEventPath(List<ElectionEventReport> list) {
        when(getInjectMock(ElectionEventReportRepository.class).findByElectionEventPath(ELECTION_EVENT_PATH)).thenReturn(list);
        return list;
    }

    private no.valg.eva.admin.common.rbac.Access access() {
        return Accesses.Rapport_Manntall_Avkrysningsmanntall.getAccess();
    }

    private ElectionEventReport electionEventReport(Report report) {
        return new ElectionEventReport(createMock(ElectionEvent.class), report);
    }

    @SuppressWarnings("unchecked")
    private void stub_findContestsByElectionEventAndAreas() {
        when(getInjectMock(MvElectionRepository.class)
                .findContestsByElectionEventAndAreas(any(ElectionEvent.class), anyList()))
                .thenReturn(singletonList(createMock(MvElection.class)));
    }

    private void stub_isSettlementDone(boolean isSettlementDone) {
        when(getInjectMock(SettlementRepository.class).erValgoppgjørKjørt(any(Contest.class))).thenReturn(isSettlementDone);
    }

    private Municipality stub_municipalityByElectionEventAndId(boolean xim) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.isElectronicMarkoffs()).thenReturn(xim);
        when(getInjectMock(MunicipalityRepository.class).findByPk(anyLong())).thenReturn(municipality);
        return municipality;
    }

}

