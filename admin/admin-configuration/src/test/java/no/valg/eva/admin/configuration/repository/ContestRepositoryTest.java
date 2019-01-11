package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ContestRepositoryTest extends AbstractJpaTestBase {

	private ContestRepository contestRepository;
	private MvAreaRepository mvAreaRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		contestRepository = new ContestRepository(getEntityManager());
		mvAreaRepository = new MvAreaRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void findBoroughContestsInMunicipality_whenInvokedWithOslo_shallReturnBoroughContests() {
		Municipality oslo = mvAreaRepository.findSingleByPath("200701.47.03.0301").getMunicipality();

		assertThat(contestRepository.findBoroughContestsInMunicipality(oslo)).isNotEmpty();
	}

	@Test
	public void findBoroughContestsInMunicipality_whenNotInvokedWithOslo_shallReturnNothing() {
		Municipality vefsn = mvAreaRepository.findSingleByPath("200701.47.18.1824").getMunicipality();

		assertThat(contestRepository.findBoroughContestsInMunicipality(vefsn)).isEmpty();
	}

	@Test
	public void findByPk_contestWithPkExists_returnsContest() {
		Contest contest = genericTestRepository.findEntitiesByProperty(Contest.class, "id", "000001").get(0);
		assertThat(contestRepository.findByPk(contest.getPk())).isEqualTo(contest);
	}

	@Test
	public void antallMultiomraadedistrikter_forEnValghendelse_returnererAntalletValgdistrikterSomIkkeErSingleArea() {
		ElectionEvent valghendelse2007 = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");
		ElectionEvent valghendelse2009sameting = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200901");

		assertThat(contestRepository.antallMultiomraadedistrikter(valghendelse2007)).isEqualTo(0);
		assertThat(contestRepository.antallMultiomraadedistrikter(valghendelse2009sameting)).isEqualTo(7);
	}
	
}
