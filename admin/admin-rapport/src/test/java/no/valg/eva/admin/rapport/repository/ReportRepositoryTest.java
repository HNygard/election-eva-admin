package no.valg.eva.admin.rapport.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.rapport.domain.model.Report;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class ReportRepositoryTest extends AbstractJpaTestBase {

	private ReportRepository reportRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		reportRepository = new ReportRepository(getEntityManager());
	}

	@Test
	public void findAll_returnsAtLeastOneReport() {
		List<Report> reports = reportRepository.findAll();

		assertThat(reports.size() >= 1).isTrue();
	}

}

