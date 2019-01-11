package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ElectionRepositoryTest extends AbstractJpaTestBase {

	private ElectionRepository electionRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		electionRepository = new ElectionRepository(getEntityManager());
	}

	@Test
	public void findElectionInEvent_electionEvent200701existsInDatabaseAndHasRelatedElection03_electionWithId03IsReturned() {
		assertThat(electionRepository.findElectionInEvent("03", "200701")).isNotNull();
	}
}
