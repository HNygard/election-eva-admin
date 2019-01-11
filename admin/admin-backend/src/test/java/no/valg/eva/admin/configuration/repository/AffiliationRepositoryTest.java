package no.valg.eva.admin.configuration.repository;

import static org.mockito.Mockito.mock;

import java.util.List;

import javax.enterprise.event.Event;

import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ListProposalTestFixture;
import no.evote.service.backendmock.RepositoryBackedRBACTestFixture;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.ObjectAssert;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class AffiliationRepositoryTest extends AbstractJpaTestBase {

	private AffiliationRepository affiliationRepository;
	private ListProposalTestFixture listProposalTestFixture;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		affiliationRepository = new AffiliationRepository(getEntityManager());
		BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
		backend.initServices();
		listProposalTestFixture = new ListProposalTestFixture(backend);
		listProposalTestFixture.init();

		RepositoryBackedRBACTestFixture rbacTestFixture = new RepositoryBackedRBACTestFixture(getEntityManager());
		rbacTestFixture.init();
	}

	@Test
	public void testGetAffiliationByName() {
		Affiliation affiliation = affiliationRepository.getAffiliationById(listProposalTestFixture.getTestAffiliation().getParty()
				.getId(), listProposalTestFixture.getContest().getPk());
		Assert.assertEquals(affiliation.getPk(), listProposalTestFixture.getTestAffiliation().getPk());
	}

	@Test
	public void testGetAffiliationByIdNull() {
		Assert.assertNull(affiliationRepository.getAffiliationById("noID", -1L));
	}

	@Test
	public void testFindByBallotStatusAndContest() {
		Assert.assertEquals(2, affiliationRepository.findByBallotStatusAndContest(listProposalTestFixture.getContest().getPk(),
						BallotStatus.BallotStatusValue.PENDING.getId()).size());
	}

	@Test
	public void updateCandidates_moveTopToBottom_success() throws Exception {
		List<Affiliation> list = affiliationRepository.findAffiliationByContestAndDisplayOrderRange(20L, 1, 3);
		// Move top to bottom and reorder
		list.add(list.remove((0)));
		int count = 1;
		for (Affiliation c : list) {
			c.setDisplayOrder(count++);
		}
		List<Affiliation> result = affiliationRepository.updateAffiliations(list);
		getEntityManager().flush();

		ObjectAssert.assertThat(result.size()).isEqualTo(list.size());
	}
}

