package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ElectionGroupRepositoryTest extends AbstractJpaTestBase {

	private ElectionGroupRepository electionGroupRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		electionGroupRepository = new ElectionGroupRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void getElectionGroupsSortedShouldReturnElectionSortedByElectionGroupId() throws Exception {
		// Test data requirements:
		// ee id 200701 - has two election groups
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");

		List<ElectionGroup> electionGroupList = electionGroupRepository.getElectionGroupsSorted(electionEvent.getPk());

		assertThat(electionGroupList.size()).isEqualTo(2);
		String idElection1 = electionGroupList.get(0).getId();
		String idElection2 = electionGroupList.get(1).getId();
		assertThat(idElection1.compareTo(idElection2) < 0).isTrue();
	}
	
	@Test
	public void findElectionGroupById() {
		// Test data requirements:
		// ee id 200701
		// eg id 01
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");
		assertThat(electionGroupRepository.findElectionGroupById(electionEvent.getPk(), "01")).isNotNull();
	}
}
