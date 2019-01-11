package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.NoResultException;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.model.views.ContestRelArea;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ReportingUnitRepositoryTest extends AbstractJpaTestBase {

	private ReportingUnitRepository repository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		repository = new ReportingUnitRepository(getEntityManager());
	}

	@Test
	public void zeroTest() {
		assertThat(new ReportingUnitRepository()).isNotNull();
	}

	@Test
	public void findByAreaPathAndType() {
		AreaPath areaPath = new AreaPath("200701.47.01.0101.010100.0001");

		ReportingUnit reportingUnit = repository.findByAreaPathAndType(areaPath, ReportingUnitTypeId.STEMMESTYRET);

		assertThat(reportingUnit).isNotNull();
		assertThat(reportingUnit.getMvArea().getAreaPath()).isEqualTo(areaPath.path());
		assertThat(reportingUnit.getReportingUnitType().getId()).isEqualTo(ReportingUnitTypeId.STEMMESTYRET.getId());
	}

	@Test
	public void byAreaPathElectionPathAndType_returnsReportingUnit() {
		AreaPath areaPath = new AreaPath("200901.47.00.0001");
		ElectionPath electionPath = new ElectionPath("200901.02.02.000001");

		ReportingUnit reportingUnit = repository.byAreaPathElectionPathAndType(areaPath, electionPath, ReportingUnitTypeId.OPPTELLINGSVALGSTYRET);

		assertThat(reportingUnit).isNotNull();
		assertThat(reportingUnit.getMvArea().getAreaPath()).isEqualTo(areaPath.path());
		assertThat(reportingUnit.getReportingUnitType().getId()).isEqualTo(ReportingUnitTypeId.OPPTELLINGSVALGSTYRET.getId());
	}

	@Test
	public void existsForAreaPathAndType() {
		AreaPath areaPath = new AreaPath("200701.47.01.0101.010100.0001");
		assertThat(repository.existsFor(areaPath, ReportingUnitTypeId.STEMMESTYRET)).isTrue();
	}

	@Test
	public void doesNotExistForAreaPathAndType() {
		AreaPath areaPath = new AreaPath("200701.47.01.0101.010100.0001");
		assertThat(repository.existsFor(areaPath, ReportingUnitTypeId.VALGSTYRET)).isFalse();
	}

	@Test
	public void getReportingUnit_givenContestRelArea_returnsReportingUnit() throws Exception {
		ContestRelArea contestRelArea = new ContestRelArea();
		contestRelArea.setElectionPath("200701.01.01.000001");
		contestRelArea.setAreaPath("200701.47.01.0101");
		ReportingUnit reportingUnit = repository.getReportingUnit(contestRelArea);
		assertThat(reportingUnit).isNotNull();
	}

	@Test(expectedExceptions = EvoteNoRollbackException.class, expectedExceptionsMessageRegExp = "@count.error.missing_reporting_unit")
	public void getReportingUnit_givenInvalidContestRelArea_throwsEvoteNoRollbackException() throws Exception {
		ContestRelArea contestRelArea = new ContestRelArea();
		contestRelArea.setElectionPath("200701.01.01.999999");
		contestRelArea.setAreaPath("200701.47.01.0101");
		repository.getReportingUnit(contestRelArea);
	}

	@Test
	public void getReportingUnit_withContestInfoOnBoroughLevel_returnsReportingUnit() throws Exception {
		ReportingUnit reportingUnit = repository.getReportingUnit(ElectionPath.from("200701.01.01.000001"), AreaPath.from("200701.47.01.0101.010100"));

		assertThat(reportingUnit).isNotNull();
		assertThat(reportingUnit.getActualAreaLevel()).isEqualTo(AreaLevelEnum.MUNICIPALITY);
		assertThat(reportingUnit.getActualElectionLevel()).isEqualTo(ElectionLevelEnum.ELECTION_GROUP);
	}

	@Test
	public void getReportingUnit_withContestInfoOnMunicipalityLevel_returnsReportingUnit() throws Exception {
		ReportingUnit reportingUnit = repository.getReportingUnit(ElectionPath.from("200701.01.01.000001"), AreaPath.from("200701.47.01.0101"));

		assertThat(reportingUnit).isNotNull();
		assertThat(reportingUnit.getActualAreaLevel()).isEqualTo(AreaLevelEnum.MUNICIPALITY);
		assertThat(reportingUnit.getActualElectionLevel()).isEqualTo(ElectionLevelEnum.ELECTION_GROUP);
	}

	@Test(expectedExceptions = EvoteNoRollbackException.class, expectedExceptionsMessageRegExp = "@count.error.missing_reporting_unit")
	public void getReportingUnit_withInvalidContestInfo_throwsEvoteNoRollbackException() throws Exception {
		repository.getReportingUnit(ElectionPath.from("200701.01.01.000001"), AreaPath.from("200701.47.01.9999"));
	}

	@Test(dataProvider = "findReportingUnitByAreaLevel")
	public void findReportingUnitByAreaLevel_withDataProvider_verifyExpected(String path, ReportingUnitTypeId type) {
		AreaPath areaPath = new AreaPath(path);

		ReportingUnit reportingUnit = repository.findReportingUnitByAreaLevel(areaPath);

		assertThat(reportingUnit).isNotNull();
		assertThat(reportingUnit.getReportingUnitType().reportingUnitTypeId()).isSameAs(type);
	}

	@DataProvider(name = "findReportingUnitByAreaLevel")
	public Object[][] findReportingUnitByAreaLevel() {
		return new Object[][] {
				{ "200701.47", ReportingUnitTypeId.RIKSVALGSTYRET },
				{ "200701.47.01", ReportingUnitTypeId.FYLKESVALGSTYRET },
				{ "200701.47.01.0101", ReportingUnitTypeId.VALGSTYRET },
				{ "200701.47.01.0101.010100.0001", ReportingUnitTypeId.STEMMESTYRET }
		};
	}

	@Test(expectedExceptions = NoResultException.class)
	public void findReportingUnitByAreaLevel_withElectionEventLevel_throwsException() {
		AreaPath areaPath = new AreaPath("200701");

		repository.findReportingUnitByAreaLevel(areaPath);
	}
	
	@Test
	public void findAlleValgstyrerIValghendelse_gittEnValghendelse_henterUtAlleStyrene() {
		GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
		ElectionEvent valghendelse2007 = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");

		List<ReportingUnit> valgstyrer = repository.findAlleValgstyrerIValghendelse(valghendelse2007);
		
		assertThat(valgstyrer.size()).isEqualTo(434);
	}

	@Test
	public void finnOpptellingsvalgstyrer_gittEnValghendelse_henterUtAlleStyreneTilknyttetOpptellingskommuner() {
		GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
		ElectionEvent valghendelse2007 = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");
		ElectionEvent valghendelse2009sameting = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200901");

		assertThat(repository.finnOpptellingsvalgstyrer(valghendelse2007).size()).isEqualTo(0);
		assertThat(repository.finnOpptellingsvalgstyrer(valghendelse2009sameting).size()).isEqualTo(1);
	}

}
