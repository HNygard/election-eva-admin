package no.valg.eva.admin.valgnatt.application.service;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEvent;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.counting.port.adapter.service.valgnatt.ValgnattApi;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportStatus;
import no.valg.eva.admin.valgnatt.domain.service.valgnattrapport.RapporteringsstatusDomainService;
import no.valg.eva.admin.valgnatt.domain.service.valgnattrapport.ValgnattrapportDomainService;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValgnattReportApplicationServiceTest extends MockUtilsTestCase {

	@Test
	public void exportGeographyAndVoters_noError_uploadsAndSetsStatusToNormal() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		Valgnattrapport report = getInjectMock(ValgnattrapportRepository.class).byElectionAndReportType(any(Election.class),
				eq(ReportType.GEOGRAFI_STEMMEBERETTIGEDE));

		service.exportGeographyAndVoters(mock(UserData.class), ElectionPath.from("150001.01.01"));

        verify(getInjectMock(ValgnattApi.class)).upload(any());
		verify(report).oppdaterTilStatusOk();
		verify(getInjectMock(AuditLogServiceBean.class)).addToAuditTrail(any(AbstractAuditEvent.class));
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void exportGeographyAndVoters_error_doesNotSetStatusToNormal() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
        doThrow(new RuntimeException()).when(getInjectMock(ValgnattApi.class)).upload(any());
		Valgnattrapport report = getInjectMock(ValgnattrapportRepository.class).byElectionAndReportType(any(Election.class),
				eq(ReportType.GEOGRAFI_STEMMEBERETTIGEDE));

		service.exportGeographyAndVoters(mock(UserData.class), ElectionPath.from("150001.01.01"));

        verify(getInjectMock(ValgnattApi.class)).upload(any());
		verify(report, never()).oppdaterTilStatusOk();
		verify(getInjectMock(AuditLogServiceBean.class)).addToAuditTrail(any(AbstractAuditEvent.class));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void skjemaForGrunnlagsdata_electionPathNotOnElectionLevel_throwsIllegalArgumentException() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		ElectionPath electionPath = ElectionPath.from("150001");
		service.rapporteringerForGrunnlagsdata(mock(UserData.class), electionPath);
	}

	@Test
	public void skjemaForGrunnlagsdata_returnsList() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		when(getInjectMock(ValgnattrapportDomainService.class).grunnlagsdataRapporter(any(Election.class)))
				.thenReturn(makeValgnattrapportList());

		ElectionPath electionPath = ElectionPath.from("150001.01.01");
		List<Valgnattrapportering> valgnattrapporteringList = service.rapporteringerForGrunnlagsdata(mock(UserData.class), electionPath);
		assertThat(valgnattrapporteringList.get(0).getReportType()).isEqualTo(ReportType.GEOGRAFI_STEMMEBERETTIGEDE);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void rapporterStemmeskjema_ikkeContestPath_exception() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		ElectionPath electionPath = ElectionPath.from("150001.01.01");
		AreaPath areaPath = AreaPath.from("150001.47.01.0101");

		service.rapporterStemmeskjema(mock(UserData.class), electionPath, areaPath, mock(Valgnattrapportering.class));
	}

	@Test
	public void rapporterStemmeskjema_reportsToValgnatt() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		ElectionPath electionPath = ElectionPath.from("150001.01.01.000001");
		AreaPath areaPath = AreaPath.from("150001.47.01.0101");

		service.rapporterStemmeskjema(mock(UserData.class), electionPath, areaPath, mock(Valgnattrapportering.class));
        verify(getInjectMock(ValgnattApi.class), times(1)).upload(any());
	}

	@Test
	public void kanFylketRapportere_returnererVerdiFraRepo() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		service.kanFylketRapportere(mock(UserData.class), mock(ElectionPath.class));
		verify(getInjectMock(RapporteringsstatusDomainService.class)).kanFylketRapportere(any(ElectionPath.class));
	}

	private List<Valgnattrapport> makeValgnattrapportList() {
		List<Valgnattrapport> valgnattrapportList = new ArrayList<>();
		valgnattrapportList.add(new Valgnattrapport(createMock(Election.class), ReportType.GEOGRAFI_STEMMEBERETTIGEDE));
		return valgnattrapportList;
	}

	@Test
	public void rapporteringerForOppgjorsskjema_returnerRapporteringer() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		when(getInjectMock(ValgnattrapportDomainService.class).oppgjorsskjemaRapporter(any(MvElection.class), any(MvArea.class)))
				.thenReturn(mock(Valgnattrapport.class));
		when(getInjectMock(ValgnattrapportDomainService.class).oppgjorsskjemaRapporter(any(MvElection.class), any(MvArea.class)).getStatus())
				.thenReturn(ValgnattrapportStatus.NOT_SENT);
		service.rapporteringerForOppgjorsskjema(mock(UserData.class), mock(ElectionPath.class), null);
	}

	@Test
	public void rapporteringerForStemmeskjema_returnerRapporteringer() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
		Valgnattrapport fakeRapport = mock(Valgnattrapport.class);
		List<Valgnattrapport> rapporter = valgnattrapporter(fakeRapport);
		when(fakeRapport.getStatus()).thenReturn(ValgnattrapportStatus.NOT_SENT);
		when(getInjectMock(ValgnattrapportDomainService.class).stemmeskjemaRapporter(any(MvElection.class), any(Municipality.class))).thenReturn(rapporter);
		service.rapporteringerForStemmeskjema(mock(UserData.class), mock(ElectionPath.class), mock(AreaPath.class));
	}

	private List<Valgnattrapport> valgnattrapporter(Valgnattrapport fakeRapport) {
		List<Valgnattrapport> rapporter = new ArrayList<>();
		rapporter.add(fakeRapport);
		return rapporter;
	}

	@Test
	public void rapporterOppgjorsskjema_oppdatererOgsaIkkeRapporterteStemmeskjema() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);

		service.rapporterOppgjørsskjema(mock(UserData.class), mock(ElectionPath.class), mock(AreaPath.class), mock(Valgnattrapportering.class));
		verify(getInjectMock(ValgnattrapportDomainService.class), times(1)).markerStemmeskjemaRapportert(any(MvElection.class));
	}

	@Test
	public void antallRapporterbare_kommuneMedEnValgnattrapporterSomKanRapporteres_returnererEn() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);

        when(getInjectMock(RapporteringsstatusDomainService.class).brukStatusForStemmeskjema(eq(AREA_PATH_MUNICIPALITY))).thenReturn(true);
		when(getInjectMock(ValgnattrapportDomainService.class).antallRapporterbareStemmeskjemaRapporter(any(MvElection.class), any(Municipality.class))).thenReturn(1L);

		assertThat(service.antallRapporterbare(mock(UserData.class), mock(ElectionPath.class), AREA_PATH_MUNICIPALITY)).isEqualTo(1L);
	}

	@Test
	public void antallRapporterbare_fylkeSomKanRapportere_returnererEn() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);

		when(getInjectMock(RapporteringsstatusDomainService.class).brukStatusForOppgjorsskjema(any(ElectionPath.class), eq(AREA_PATH_COUNTY))).thenReturn(true);
		when(getInjectMock(RapporteringsstatusDomainService.class).antallRapporterbareOppgjorsskjema()).thenReturn(1L);

		assertThat(service.antallRapporterbare(mock(UserData.class), mock(ElectionPath.class), AREA_PATH_COUNTY)).isEqualTo(1L);
	}

	@Test
	public void antallRapporterbare_bydelsvalg_returnerer0() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);
	
		ElectionPath electionPath = ElectionPath.from("150001.03.03");
		AreaPath areaPath = AreaPath.from("150001.47.03.0301");
		when(getInjectMock(RapporteringsstatusDomainService.class).erValgPaaBydelsnivaa(electionPath, areaPath)).thenReturn(true);

		assertThat(service.antallRapporterbare(mock(UserData.class), electionPath, areaPath)).isEqualTo(0L);
	}

	@Test
	public void altErRapportert_kommuneMedEnValgnattrapporterSomErRapportert_returnererTrue() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);

        when(getInjectMock(RapporteringsstatusDomainService.class).brukStatusForStemmeskjema(eq(AREA_PATH_MUNICIPALITY))).thenReturn(true);
		when(getInjectMock(ValgnattrapportDomainService.class).erAlleStemmeskjemaRapportert(any(MvElection.class), any(Municipality.class))).thenReturn(true);

		assertThat(service.altErRapportert(mock(UserData.class), mock(ElectionPath.class), AREA_PATH_MUNICIPALITY)).isTrue();
	}

	@Test
	public void altErRapportert_fylkeSomIkkeKanRapportere_returnererFalse() throws Exception {
		ValgnattReportApplicationService service = initializeMocks(ValgnattReportApplicationService.class);

		assertThat(service.altErRapportert(mock(UserData.class), mock(ElectionPath.class), AREA_PATH_COUNTY)).isFalse();
	}

}
