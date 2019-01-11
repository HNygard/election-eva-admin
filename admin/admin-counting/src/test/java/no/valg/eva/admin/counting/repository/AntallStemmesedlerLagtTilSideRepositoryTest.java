package no.valg.eva.admin.counting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class AntallStemmesedlerLagtTilSideRepositoryTest extends AbstractJpaTestBase {
	private AntallStemmesedlerLagtTilSideRepository repository;

	private Municipality municipality1;
	private Municipality municipality2;
	private ElectionGroup electionGroup1;
	private ElectionGroup electionGroup2;
	private Contest contest1;
	private Contest contest2;
	private UserData userData;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		repository = new AntallStemmesedlerLagtTilSideRepository(getEntityManager());
		MvAreaRepository mvAreaRepository = new MvAreaRepository(getEntityManager());
		municipality1 = mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.01.0101")).getMunicipality();
		municipality2 = mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.01.0104")).getMunicipality();
		MvElectionRepository mvElectionRepository = new MvElectionRepository(getEntityManager());
		electionGroup1 = mvElectionRepository.finnEnkeltMedSti(new ValggruppeSti("200701", "01")).getElectionGroup();
		electionGroup2 = mvElectionRepository.finnEnkeltMedSti(new ValggruppeSti("200701", "02")).getElectionGroup();
		contest1 = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("200701", "01", "01", "000001")).getContest();
		contest2 = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("200701", "01", "01", "000002")).getContest();
		userData = new UserData();
		OperatorRole operatorRole = new OperatorRole();
		Role udRole = new Role();
		udRole.setUserSupport(false);
		udRole.setElectionEvent(new ElectionEvent());
		operatorRole.setRole(udRole);
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());
		operatorRole.setOperator(operator);
		userData.setOperatorRole(operatorRole);
		setupTransactionSynchronizationRegistry();
	}

	@Test
	public void create_givenEntity_createsEntity() throws Exception {
		AntallStemmesedlerLagtTilSide entity = repository.create(userData, new AntallStemmesedlerLagtTilSide(municipality1, electionGroup1, null, 1));
		assertThat(entity).isNotNull();
		assertThat(entity.getPk()).isNotNull();
		assertThat(entity.getMunicipality()).isEqualTo(municipality1);
		assertThat(entity.getElectionGroup()).isEqualTo(electionGroup1);
		assertThat(entity.getContest()).isNull();
		assertThat(entity.getAntallStemmesedler()).isEqualTo(1);
	}

	@Test
	public void findByMunicipalityAndElectionGroup_givenMunicipalityAndElectionGroup_returnsEntity() throws Exception {
		repository.create(userData, new AntallStemmesedlerLagtTilSide(municipality1, electionGroup1, null, 1));
		AntallStemmesedlerLagtTilSide entity = repository.findByMunicipalityAndElectionGroup(municipality1, electionGroup1);
		assertThat(entity.getMunicipality()).isEqualTo(municipality1);
		assertThat(entity.getElectionGroup()).isEqualTo(electionGroup1);
		assertThat(entity.getContest()).isNull();
		assertThat(entity.getAntallStemmesedler()).isEqualTo(1);
	}

	@Test(dataProvider = "findByMunicipalityAndElectionGroupMissingTestData")
	public void findByMunicipalityAndElectionGroup_givenMunicipalityAndElectionGroup_returnsNullIfMissing(
			Municipality municipality, ElectionGroup electionGroup) throws Exception {
		AntallStemmesedlerLagtTilSide entity = repository.findByMunicipalityAndElectionGroup(municipality, electionGroup);
		assertThat(entity).isNull();
	}

	@DataProvider
	public Object[][] findByMunicipalityAndElectionGroupMissingTestData() {
		return new Object[][] {
			new Object[] {municipality1, electionGroup2},
			new Object[] {municipality2, electionGroup1}
		};
	}

	@Test
	public void findByMunicipalityAndContest_givenMunicipalityAndContest_returnsEntity() throws Exception {
		repository.create(userData, new AntallStemmesedlerLagtTilSide(municipality1, electionGroup1, contest1, 1));
		AntallStemmesedlerLagtTilSide entity = repository.findByMunicipalityAndContest(municipality1, contest1);
		assertThat(entity.getMunicipality()).isEqualTo(municipality1);
		assertThat(entity.getElectionGroup()).isEqualTo(electionGroup1);
		assertThat(entity.getContest()).isEqualTo(contest1);
		assertThat(entity.getAntallStemmesedler()).isEqualTo(1);
	}

	@Test(dataProvider = "findByMunicipalityAndContestMissingTestData")
	public void findByMunicipalityAndContest_givenMunicipalityAndContest_returnsNullIfMissing(
			Municipality municipality, Contest contest) throws Exception {
		AntallStemmesedlerLagtTilSide entity = repository.findByMunicipalityAndContest(municipality, contest);
		assertThat(entity).isNull();
	}

	@DataProvider
	public Object[][] findByMunicipalityAndContestMissingTestData() {
		return new Object[][] {
				new Object[] {municipality1, contest2},
				new Object[] {municipality2, contest1}
		};
	}

	@Test
	public void isAntallStemmerLagtTilSide_givenHaveNotCreatedAntallStemmesedlerLagtTilSide_returnsFalse() throws Exception {
		boolean isStemmerLagtTilSide = repository.isAntallStemmerLagtTilSide(municipality1, electionGroup1);
		assertThat(isStemmerLagtTilSide).isEqualTo(false);
	}

	@Test
	public void isAntallStemmerLagtTilSide_givenHavingCreatedAntallStemmesedlerLagtTilSide_returnsTrue() throws Exception {
		repository.create(userData, new AntallStemmesedlerLagtTilSide(municipality1, electionGroup1, null, 1));
		boolean isStemmerLagtTilSide = repository.isAntallStemmerLagtTilSide(municipality1, electionGroup1);
		assertThat(isStemmerLagtTilSide).isEqualTo(true);
	}
}
