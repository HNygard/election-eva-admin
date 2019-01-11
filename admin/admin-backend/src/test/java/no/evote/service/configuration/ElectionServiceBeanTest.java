package no.evote.service.configuration;

import static org.testng.Assert.assertEquals;

import javax.persistence.PersistenceException;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class ElectionServiceBeanTest extends ElectionBaseTest {

	@BeforeMethod
	public void setUp() {
		super.init();
	}

	@Test
	public void testCreateElection() {
		assertEquals(getElection(), getElectionRepository().findByPk(getElection().getPk()));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCreateElectionIdTooLong() {
		Election election = buildCommonElection(getElectionGroup());
		election.setId("123456789");
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateElectionDuplicateId() {
		setElection(buildElection(getElectionGroup()));
		createElection(getElection());

		Election newElection = buildCommonElection(getElectionGroup());
		newElection.setId(getElection().getId());
		newElection.setAutoGenerateContests(false);

		getElectionServiceBean().create(rbacTestFixture.getUserData(), newElection);
	}

	// @Test
	// public void testUpdateElection() {
	// // Get election from database again, since the one we created lacks default election status
	// no.valg.eva.admin.configuration.domain.model.Election election = getElectionRepository().findByPk(getElection().getPk());
	//
	// election.setName("Updated name");
	// setElection(getElectionServiceBean().update(rbacTestFixture.getUserData(), election));
	//
	// Election electionAfterUpdate = getElectionRepository().findByPk(election.getPk());
	// Assert.assertNotNull(electionAfterUpdate);
	// assertEquals(electionAfterUpdate.getName(), "Updated name");
	// }
	//
	// @Test
	// public void testFindElectionById() {
	// Election retrievedElection = getElectionRepository().findElectionByElectionGroupAndId(getElectionGroup().getPk(), getElection().getId());
	// Assert.assertNotNull(retrievedElection);
	// }
	//
	// @Test
	// public void testFindElectionInElectionEvent() {
	//
	// Election retrievedElection = getElectionRepository().findElectionInEvent(getElection().getId(), getElectionEvent().getId());
	// Assert.assertTrue(retrievedElection != null);
	//
	// retrievedElection = getElectionRepository().findElectionInEvent("11", "123456");
	// Assert.assertTrue(retrievedElection == null);
	// }

	@Test
	public void testFindElectionTypeById() {
		ElectionType electionType = getElectionRepository().findElectionTypeById(EvoteConstants.ELECTION_TYPE_REFERENDUM);
		assertEquals(EvoteConstants.ELECTION_TYPE_REFERENDUM, electionType.getId());
	}

}
