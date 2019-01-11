package no.valg.eva.admin.counting.repository;

import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.personal;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class CountingCodeValueRepositoryTest extends AbstractJpaTestBase {

	private CountingCodeValueRepository repository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		new CountingCodeValueRepository();
		repository = new CountingCodeValueRepository(getEntityManager());
	}

	@Test
	public void findVoteCountStatus() {
		assertThat(repository.findVoteCountStatusById(CountStatus.APPROVED.getId()).getId()).isEqualTo(CountStatus.APPROVED.getId());
	}

	@Test
	public void findVoteCountCategory() {
		assertThat(repository.findVoteCountCategoryById(CountCategory.BF.getId()).getId()).isEqualTo(CountCategory.BF.getId());
	}

	@Test
	public void findCountQualifier() {
		assertThat(repository.findCountQualifierById(CountQualifier.PROTOCOL.getId()).getId()).isEqualTo(CountQualifier.PROTOCOL.getId());
	}

	@Test
	public void findVoteCategoryById_idThatExistsInDatabase_voteCategoryInstanceWithGivenIdIsReturned() {
		VoteCategory.VoteCategoryValues voteCategoryId = personal;
		assertThat(repository.findVoteCategoryById(voteCategoryId).getId()).isEqualTo(personal.name());
	}
}
