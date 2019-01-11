package no.valg.eva.admin.rapport.application.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;
import no.valg.eva.admin.rapport.domain.model.Report;
import no.valg.eva.admin.rapport.repository.ElectionEventReportRepository;
import no.valg.eva.admin.rapport.repository.ReportRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.ElectionPath.from;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RapportApplicationServiceTest extends MockUtilsTestCase {

	private static final ElectionPath ELECTION_EVENT_PATH = from("200701");

	@Test
	public void lagre_withWithActiveNonExisting_createsNewElectionEventReport() throws Exception {
		RapportApplicationService service = initializeMocks(RapportApplicationService.class);
		stub_reportRepository_findAll(1);
		stub_electionEventReportRepository_findByElectionEventPath(new ArrayList<>());

        service.lagre(createMock(UserData.class), ELECTION_EVENT_PATH, singletonList(
                valghendelsesRapport(1, true)));

		verify(getInjectMock(ElectionEventReportRepository.class), times(1)).create(any(UserData.class), any(ElectionEventReport.class));
	}

	@Test
	public void lagre_withWithNonActiveExisting_deletesElectionEventReport() throws Exception {
		RapportApplicationService service = initializeMocks(RapportApplicationService.class);
        stub_electionEventReportRepository_findByElectionEventPath(singletonList(
                electionEventReport(stub_reportRepository_findAll(1).get(0))));

        service.lagre(createMock(UserData.class), ELECTION_EVENT_PATH, singletonList(
                valghendelsesRapport(1, false)));

		verify(getInjectMock(ElectionEventReportRepository.class), times(1)).deleteReports(any(UserData.class), anyList());
	}

	private List<Report> stub_reportRepository_findAll(int size) {
		List<Report> reports = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			reports.add(report(i + 1));
		}
		when(getInjectMock(ReportRepository.class).findAll()).thenReturn(reports);
		return reports;
	}

	private List<ElectionEventReport> stub_electionEventReportRepository_findByElectionEventPath(List<ElectionEventReport> list) {
		when(getInjectMock(ElectionEventReportRepository.class).findByElectionEventPath(ELECTION_EVENT_PATH)).thenReturn(list);
		return list;
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

	private ElectionEventReport electionEventReport(Report report) {
		return new ElectionEventReport(createMock(ElectionEvent.class), report);
	}

	private ValghendelsesRapport valghendelsesRapport(long id, boolean synlig) {
		ValghendelsesRapport result = new ValghendelsesRapport("report_" + id, GRUNNLAGSDATA, access());
		result.setSynlig(synlig);
		result.setTilgjengelig(synlig);
		return result;
	}

	private no.valg.eva.admin.common.rbac.Access access() {
		return Accesses.Rapport_Manntall_Avkrysningsmanntall.getAccess();
	}

}

