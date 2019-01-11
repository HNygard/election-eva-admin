package no.valg.eva.admin.rapport.repository;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.ElectionPath.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;
import no.valg.eva.admin.rapport.domain.model.Report;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class ElectionEventReportRepositoryTest extends AbstractJpaTestBase {

	private static final ElectionPath ELECTION_EVENT_PATH = from("200701");

	private ReportRepository reportRepository;
	private ElectionEventRepository electionEventRepository;
	private ElectionEventReportRepository electionEventReportRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		reportRepository = new ReportRepository(getEntityManager());
		electionEventRepository = new ElectionEventRepository(getEntityManager());
		electionEventReportRepository = new ElectionEventReportRepository(getEntityManager());
	}

	@Test
	public void findByElectionEventPath_withNoReports_returnsEmptyList() {
		List<ElectionEventReport> reports = electionEventReportRepository.findByElectionEventPath(ELECTION_EVENT_PATH);

		assertThat(reports).isEmpty();
	}

	@Test
	public void findByElectionEventPath_withOneReport_returnsTheOneReport() {
		// Create a report
		ElectionEventReport newReport = create();

		List<ElectionEventReport> reports = electionEventReportRepository.findByElectionEventPath(from("200701"));

		assertThat(reports).hasSize(1);
		assertThat(newReport).isEqualTo(reports.get(0));
	}

	@Test
	public void deleteReports_withOneReport_deletesReport() {
		// Create a report
		ElectionEventReport newReport = create();

		assertThat(electionEventReportRepository.findByElectionEventPath(from("200701"))).hasSize(1);

		electionEventReportRepository.deleteReports(createMock(UserData.class), asList(newReport));

		assertThat(electionEventReportRepository.findByElectionEventPath(from("200701"))).isEmpty();
	}

	private ElectionEventReport create() {
		ElectionEvent electionEvent = electionEventRepository.findById(ELECTION_EVENT_PATH.path());
		Report report = reportRepository.findAll().get(0);
		ElectionEventReport newReport = new ElectionEventReport(electionEvent, report);
		return electionEventReportRepository.create(createMock(UserData.class), newReport);
	}

}

