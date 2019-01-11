package no.evote.service.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;

import no.evote.constants.AreaLevelEnum;
import no.evote.model.ModelTestConstants;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class ContestServiceBeanTest extends ElectionBaseTest {

	@BeforeMethod
	public void setUp() {
		super.init();
	}

	@Test
	public void testCreateContest() {
		setContest(buildContest(getElection()));
		Contest contest = createContest(getContest());

		assertEquals(contest, contestRepository.findByPk(contest.getPk()));
	}

	@Test
	public void testCreateContestIdTooLong() {
		Contest contest = buildContest(getElection());
		contest.setId("123456789");

		Set<ConstraintViolation<Contest>> constraintViolations = getValidator().validate(contest);

		assertEquals(constraintViolations.size(), 1);
		assertEquals(constraintViolations.iterator().next().getMessage(), ModelTestConstants.MESSAGE_ID_VALIDATION_FAILED);
	}

	@Test
	public void testCreateContestDuplicateId() {
		try {
			setContest(buildContest(getElection()));
			createContest(getContest());

			Contest newContest = buildContest(getElection());
			newContest.setId(getContest().getId());

			contestRepository.create(rbacTestFixture.getUserData(), newContest);

			Assert.fail("PersistenceException should be thrown");
		} catch (PersistenceException e) {
		}
	}

	@Test
	public void create_withDifferentElectionAndMvAreaLevels_updatesMvElection() {
		setContest(buildContest(getElectionRepository().findElectionInEvent("01", "200701")));
		Contest contest = getContest();

		MvArea mvArea = getMvAreaRepository().findSingleByPath(AreaPath.from("200701.47.18.1853"));

		contest = getContestService().create(rbacTestFixture.getUserData(), contest, mvArea, true, false);

		// Originally the MvElection is created with areaLevel = 0
		assertThat(getMvElectionRepository().findByContest(contest).getAreaLevel()).isEqualTo(AreaLevelEnum.MUNICIPALITY.getLevel());
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "Invalid areaLevel for contest. ElectionAreaLevel=ROOT, areaLevel=MUNICIPALITY")
	public void create_withInvalidMvAreaLevel_throwsException() {
		setContest(buildContest(getElection()));
		Contest contest = getContest();

		MvArea mvArea = getMvAreaRepository().findSingleByPath(AreaPath.from("200701.47.18.1853"));

		getContestService().create(rbacTestFixture.getUserData(), contest, mvArea, true, false);
	}

	@Test
	public void testUpdateContest() {
		Contest contest = buildContest(getElection());
		setContest(contest);
		createContest(getContest());

		contest = contestRepository.findByPk(contest.getPk());
		contest.setName("Updated name");

		Contest contestAfterUpdate = getContestService().update(rbacTestFixture.getUserData(), contest,
				rbacTestFixture.getTransactionSynchronizationRegistry());

		Assert.assertNotNull(contestAfterUpdate);
		assertEquals(contestAfterUpdate.getName(), "Updated name");
	}

	@Test
	public void testFindContestById() {
		setContest(buildContest(getElection()));
		createContest(getContest());

		Contest retrievedContest = getContestRepository().findContestById(getElection().getPk(), getContest().getId());
		Assert.assertNotNull(retrievedContest);
	}

	public ContestRepository getContestRepository() {
		return contestRepository;
	}
}
