package no.valg.eva.admin.configuration.repository;

import static org.testng.Assert.assertEquals;

import java.util.List;

import no.evote.model.views.ContestRelArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ContestRelAreaRepositoryTest extends AbstractJpaTestBase {

	private ContestRelAreaRepository contestRelAreaRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		contestRelAreaRepository = new ContestRelAreaRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void noArgConstructor_isPresent() {
		new ContestRelArea();
	}

	@Test
	public void findAllAllowedShouldReturnThreeSpecificContestForASpecificMvAreaAndMvElection() throws Exception {
		MvElection mvElection = genericTestRepository.findEntityByProperty(MvElection.class, "electionPath", "200701");
		MvArea mvArea = genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", "200701.47.17.1749");

		List<ContestRelArea> contestRelAreaList = contestRelAreaRepository.findAllAllowed(mvElection, mvArea);

		
		assertEquals(contestRelAreaList.size(), 3);
		
	}
}
