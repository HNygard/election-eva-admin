package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.RollbackException;

import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.ObjectAssert;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class ProposerRepositoryTest extends AbstractJpaTestBase {

	private ProposerRepository candidateRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		candidateRepository = new ProposerRepository(getEntityManager());
	}

	@Test
	public void updateProposers_moveTopToBottom_success() throws Exception {
		List<Proposer> list = candidateRepository.findProposerByBallotAndDisplayOrderRange(5L, 1, 2);
		// Move top to bottom and reorder
		list.add(list.remove((0)));
		int count = 1;
		for (Proposer c : list) {
			c.setDisplayOrder(count++);
		}

		List<Proposer> result = candidateRepository.updateProposers(list);
		getEntityManager().flush();

		ObjectAssert.assertThat(result.size()).isEqualTo(list.size());
	}

	@Test(expectedExceptions = RollbackException.class)
	public void updateProposers_withInvalidDisplayOrder_shouldFailWithConstraintException() throws Exception {
		List<Proposer> list = candidateRepository.findProposerByBallotAndDisplayOrderRange(5L, 1, 2);
		for (Proposer c : list) {
			c.setDisplayOrder(1);
		}

		candidateRepository.updateProposers(list);
		getEntityManager().getTransaction().commit(); // Try to commit. Should fail.
	}
}

