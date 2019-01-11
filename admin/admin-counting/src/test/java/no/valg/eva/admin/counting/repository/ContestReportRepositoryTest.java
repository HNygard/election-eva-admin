package no.valg.eva.admin.counting.repository;

import static no.valg.eva.admin.test.VersionedEntityAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.security.UserDataBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)

public class ContestReportRepositoryTest extends AbstractJpaTestBase {

	public static final int NO_OF_CONTEST_REPORTS_IN_OSTFOLD = 19;
	private ContestReportRepository contestReportRepository;
	private GenericTestRepository genericRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		contestReportRepository = new ContestReportRepository(getEntityManager());
		genericRepository = new GenericTestRepository(getEntityManager());
		setupTransactionSynchronizationRegistry();
	}

	@Test
	public void zeroTest() {
		assertThat(new ContestReportRepository()).isNotNull();
	}

	@Test
	public void findByPkReturnsCorrectContestReport() {
		int contestReportPk = (int) getEntityManager().createNativeQuery("select min(contest_report_pk) from contest_report").getSingleResult();

		ContestReport contestReport = contestReportRepository.findByPk(contestReportPk);

		assertThat(contestReport).isNotNull();
		assertThat(contestReport.getPk()).isEqualTo(contestReportPk);
	}

	@Test
	public void findByPkReturnsNullForUnknownPk() {
		long contestReportPk = 999;

		ContestReport contestReport = contestReportRepository.findByPk(contestReportPk);

		assertThat(contestReport).isNull();
	}

	@Test
	public void delete_contestReportWithVoteCounts_givenContestReportAndItsVoteCountsAreRemoved() {
		long contestReportPk = (int) getEntityManager().createNativeQuery("SELECT MAX(contest_report_pk) FROM vote_count").getSingleResult();

		contestReportRepository.delete(UserDataBuilder.create().withRootLevelAsSelectedArea().build(), contestReportPk);

		assertThat(getEntityManager().createNativeQuery("SELECT * FROM vote_count WHERE contest_report_pk = " + contestReportPk).getResultList().isEmpty());
	}

	@Test
	public void hasContestReport_withValidPks_shouldReturnTrue() throws Exception {
		Contest contest = makeContest(1L);
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setPk(2L);
		assertThat(contestReportRepository.hasContestReport(contest, reportingUnit)).isTrue();
	}

	@Test
	public void hasContestReport_withInvalidPks_shouldReturnFalse() throws Exception {
		Contest contest = makeContest(999L);
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setPk(999L);
		assertThat(contestReportRepository.hasContestReport(contest, reportingUnit)).isFalse();
	}

	@Test
	public void findByContest_withContestWithContestReports_shouldReturnContestReports() throws Exception {
		Contest contest = makeContest(1L);
		assertThat(contestReportRepository.findByContest(contest)).hasSize(19);
	}

	@Test
	public void findByContest_withContestWithoutContestReports_shouldNotReturnContestReports() throws Exception {
		Contest contest = makeContest(999L);
		assertThat(contestReportRepository.findByContest(contest)).isEmpty();
	}

	private Contest makeContest(long pk) {
		Contest contest = new Contest();
		contest.setPk(pk);
		return contest;
	}

	@Test
	public void findByContestAndMvArea_returnsContestReports() {
		Contest contestOstfold = makeContest(1L);
		MvArea mvAreaOstfold = getMvArea();
		assertThat(contestReportRepository.findByContestAndMvArea(contestOstfold, mvAreaOstfold)).hasSize(1);
	}

	private MvArea getMvArea() {
		return genericRepository.findEntityByProperty(MvArea.class, "areaPath", "200701.47.01");
	}

	@Test
	public void byContestInArea_returnsContestReports() {
		Contest contestOstfold = makeContest(1L);
		List<ContestReport> contestReports = contestReportRepository.byContestInArea(contestOstfold, AreaPath.from("200701.47.01"));
		assertThat(contestReports).hasSize(NO_OF_CONTEST_REPORTS_IN_OSTFOLD);
	}
}

