package no.valg.eva.admin.valgnatt.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ValgnattrapportRepositoryTest extends AbstractJpaTestBase {

	@Test
	public void byContestAndMunicipality_noRows_returnsEmptyList() {
		ValgnattrapportRepository valgnattrapportRepository = new ValgnattrapportRepository(getEntityManager());

		Contest contest = makeContest();
		Municipality municipality = makeMunicipality();
		assertThat(valgnattrapportRepository.byContestAndMunicipality(contest, municipality)).isEmpty();
	}

	@Test
	public void byContestAndReportType_noRows_returnsEmptyList() {
		ValgnattrapportRepository valgnattrapportRepository = new ValgnattrapportRepository(getEntityManager());

		Contest contest = makeContest();
		assertThat(valgnattrapportRepository.byContestAndReportType(contest, ReportType.VALGOPPGJOR)).isEmpty();
	}

	@Test
	public void byContestReportTypeAndMvArea_noRows_returnsNull() {
		ValgnattrapportRepository valgnattrapportRepository = new ValgnattrapportRepository(getEntityManager());

		Contest contest = makeContest();
		MvArea mvArea = makeMvArea();
		assertThat(valgnattrapportRepository.byContestReportTypeAndMvArea(contest, ReportType.STEMMESKJEMA_VF, mvArea)).isNull();
	}

	@Test
	public void byElectionAndReportType_noRows_returnsNull() {
		ValgnattrapportRepository valgnattrapportRepository = new ValgnattrapportRepository(getEntityManager());

		Election election = makeElection();
		assertThat(valgnattrapportRepository.byElectionAndReportType(election, ReportType.GEOGRAFI_STEMMEBERETTIGEDE)).isNull();
	}

	@Test(dataProvider = "rapportTyper")
	public void finnFor_rapportType_henterAlleRapporterMedRiktigTypeForEtValg(ReportType testRapportType) {
		GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
		ValgnattrapportRepository valgnattrapportRepository = new ValgnattrapportRepository(getEntityManager());
		Election valg = genericTestRepository.findEntityByProperty(Election.class, "pk", 1L);
		Valgnattrapport rapport = genericTestRepository.createEntity(new Valgnattrapport(valg, testRapportType));
		getRapportTyper().forEach(rapportType -> genericTestRepository.createEntity(new Valgnattrapport(valg, rapportType)));

		List<Valgnattrapport> rapporter = valgnattrapportRepository.finnFor(valg, testRapportType);

		assertThat(rapporter.size()).isEqualTo(2); // 2 fordi man legger inn en ekstra rapport av typen man tester for
		assertThat(rapport).isIn(rapporter);
	}

	@DataProvider
	public static Object[][] rapportTyper() {
		return new Object[][]{
				{ReportType.GEOGRAFI_STEMMEBERETTIGEDE},
				{ReportType.PARTIER_OG_KANDIDATER},
				{ReportType.STEMMESKJEMA_FE},
				{ReportType.STEMMESKJEMA_FF},
				{ReportType.STEMMESKJEMA_VE},
				{ReportType.STEMMESKJEMA_VF},
				{ReportType.VALGOPPGJOR}
		};
	}

	private List<ReportType> getRapportTyper() {
		return asList(ReportType.GEOGRAFI_STEMMEBERETTIGEDE, ReportType.PARTIER_OG_KANDIDATER, ReportType.STEMMESKJEMA_FE, ReportType.STEMMESKJEMA_FF,
				ReportType.STEMMESKJEMA_VE, ReportType.STEMMESKJEMA_VF, ReportType.VALGOPPGJOR);
	}

	@Test
	public void create_lagerInstans() {
		ValgnattrapportRepository valgnattrapportRepository = new ValgnattrapportRepository(getEntityManager());

		// finn hvilken som helst Election
		Election election = new GenericTestRepository(getEntityManager()).findEntityByProperty(Election.class, "pk", 1L);
		Valgnattrapport rapport = valgnattrapportRepository.create(new Valgnattrapport(election, ReportType.GEOGRAFI_STEMMEBERETTIGEDE));
		assertThat(rapport.getPk()).isNotNull();
	}

	private Election makeElection() {
		Election election = new Election();
		election.setPk(1L);
		return election;
	}

	private Municipality makeMunicipality() {
		Municipality municipality = new Municipality();
		municipality.setPk(1L);
		return municipality;
	}

	private MvArea makeMvArea() {
		MvArea mvArea = new MvArea();
		mvArea.setPk(1L);
		return mvArea;
	}

	private Contest makeContest() {
		Contest contest = new Contest();
		contest.setPk(1L);
		return contest;
	}
}
