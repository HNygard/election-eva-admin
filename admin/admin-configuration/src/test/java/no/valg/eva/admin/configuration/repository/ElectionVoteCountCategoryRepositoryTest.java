package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.UserDataMockups;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ElectionVoteCountCategoryRepositoryTest extends AbstractJpaTestBase {

	private ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository;
	private ElectionGroup electionGroup;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
		electionGroup = genericTestRepository.findEntitiesByProperty(ElectionGroup.class, "id", "01").get(0);
		electionVoteCountCategoryRepository = new ElectionVoteCountCategoryRepository(getEntityManager());
		VoteCountCategoryRepository voteCountCategoryRepository = new VoteCountCategoryRepository(getEntityManager());
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		electionVoteCountCategory.setElectionGroup(electionGroup);
		electionVoteCountCategory.setVoteCountCategory(voteCountCategoryRepository.findById("VO"));
		UserData userData = UserDataMockups.userOnMunicipalityLevel();

		electionVoteCountCategoryRepository.create(userData, electionVoteCountCategory);
	}

	@Test
	public void findElectionVoteCountCategories() throws Exception {
		List<ElectionVoteCountCategory> electionVoteCountCategories = electionVoteCountCategoryRepository.findElectionVoteCountCategories(electionGroup);
		assertThat(electionVoteCountCategories.size()).isEqualTo(1);
	}
}
