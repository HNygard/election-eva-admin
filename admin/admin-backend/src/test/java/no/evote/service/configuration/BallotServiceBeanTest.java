package no.evote.service.configuration;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.REPOSITORY })
public class BallotServiceBeanTest extends ListProposalBaseTest {

	private BallotServiceBean ballotService;
	private BallotRepository ballotRepository;
	private BallotStatus ballotStatus1;
	private BallotStatus ballotStatus2;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		super.init();
		ballotService = backend.getBallotService();
		ballotRepository = backend.getBallotRepository();
		ballotStatus1 = ballotRepository.findBallotStatusById(BallotStatus.BallotStatusValue.PENDING.getId());
		ballotStatus2 = ballotRepository.findBallotStatusById(BallotStatus.BallotStatusValue.REJECTED.getId());
	}

	@Test
	public void testUpdateBallotStatus() {
		Assert.assertEquals(ballotStatus1.getPk(), getTestAffiliation().getBallot().getBallotStatus().getPk());

		ballotService.updateBallotStatus(rbacTestFixture.getUserData(), getTestAffiliation(), ballotStatus2);
		updateTestAffiliation();
		Ballot ballot = getTestAffiliation().getBallot();

		Assert.assertEquals(ballotStatus2.getPk(), ballot.getBallotStatus().getPk());
		Assert.assertEquals(ballotStatus2.getPk(), ballotRepository.findByPk(getTestAffiliation().getBallot().getPk())
				.getBallotStatus().getPk());

		updateTestAffiliation();
		ballotService.updateBallotStatus(rbacTestFixture.getUserData(), getTestAffiliation(), ballotStatus1);
		updateTestAffiliation();
		ballot = getTestAffiliation().getBallot();
		Assert.assertEquals(ballotStatus1.getPk(), ballot.getBallotStatus().getPk());
		Assert.assertEquals(ballotStatus1.getPk(), ballotRepository.findByPk(getTestAffiliation().getBallot().getPk())
				.getBallotStatus().getPk());
		resetBallotStatus();
	}

	@Test
	public void testBallotStatusForProposer() {
		Assert.assertEquals(ballotStatus1.getPk(), getTestAffiliation().getBallot().getBallotStatus().getPk());

		ballotService.updateBallotStatusPending(rbacTestFixture.getUserData(), getTestAffiliation().getBallot());

		Ballot ballot = ballotRepository.findByPk(getTestAffiliation().getBallot().getPk());

		Assert.assertEquals(BallotStatus.BallotStatusValue.PENDING.getId(), ballot.getBallotStatus().getId());

		ballotService.updateBallotStatusWithdrawn(rbacTestFixture.getUserData(), ballot);

		ballot = ballotRepository.findByPk(getTestAffiliation().getBallot().getPk());

		Assert.assertEquals(BallotStatus.BallotStatusValue.WITHDRAWN.getId(), ballot.getBallotStatus().getId());

		updateTestAffiliation();
		ballotService.updateBallotStatus(rbacTestFixture.getUserData(), getTestAffiliation(), ballotStatus1);
		updateTestAffiliation();
		// ballot = getTestAffiliation().getBallot();
		updateTestAffiliation();
		Assert.assertEquals(ballotStatus1.getPk(), getTestAffiliation().getBallot().getBallotStatus().getPk());
		resetBallotStatus();
	}

	@Test
	public void testBallotStatusApprove() {

		ballotService.updateBallotStatus(rbacTestFixture.getUserData(), getTestAffiliation(),
				ballotRepository.findBallotStatusById(BallotStatus.BallotStatusValue.APPROVED.getId()));
		updateTestAffiliation();
		Ballot ballot = getTestAffiliation().getBallot();
		Assert.assertEquals(ballot.getDisplayOrder(), Integer.valueOf(1));
		Assert.assertTrue(getTestAffiliation().isApproved());
		Assert.assertTrue(ballot.isApproved());

		ballotService.updateBallotStatus(rbacTestFixture.getUserData(), getTestAffiliation(),
				ballotRepository.findBallotStatusById(BallotStatus.BallotStatusValue.PENDING.getId()));
		updateTestAffiliation();
		ballot = getTestAffiliation().getBallot();
		Assert.assertNull(ballot.getDisplayOrder());
		Assert.assertNull(getTestAffiliation().getDisplayOrder());
		Assert.assertFalse(getTestAffiliation().isApproved());
		Assert.assertFalse(ballot.isApproved());

		resetBallotStatus();
	}

	private void resetBallotStatus() {
		updateTestAffiliation();
		ballotService.updateBallotStatus(rbacTestFixture.getUserData(), getTestAffiliation(), ballotStatus1);
		updateTestAffiliation();
		Ballot ballot = getTestAffiliation().getBallot();
		Assert.assertEquals(ballotStatus1.getPk(), ballot.getBallotStatus().getPk());
		Assert.assertEquals(ballotStatus1.getPk(), ballotRepository.findByPk(getTestAffiliation().getBallot().getPk())
				.getBallotStatus().getPk());
		updateTestAffiliation();
	}
}
