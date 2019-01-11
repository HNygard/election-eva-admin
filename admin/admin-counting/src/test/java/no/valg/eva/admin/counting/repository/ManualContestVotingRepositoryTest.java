package no.valg.eva.admin.counting.repository;

import static no.valg.eva.admin.test.VersionedEntityAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class ManualContestVotingRepositoryTest extends AbstractJpaTestBase {

	private ManualContestVotingRepository manualContestVotingRepository;
	private MvArea mvArea;
	private Contest contest;
	private ElectionDay electionDay1, electionDay2;
	private VotingCategory votingCategory;
	private UserData userData;

	@Test
	public void zeroTest() {
		assertThat(new ManualContestVotingRepository()).isNotNull();
	}

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		manualContestVotingRepository = new ManualContestVotingRepository(getEntityManager());
		MvAreaRepository mvAreaRepository = new MvAreaRepository(getEntityManager());
		VotingRepository votingRepository = new VotingRepository(getEntityManager());
		userData = new UserData();
		OperatorRole operatorRole = new OperatorRole();
		Role udRole = new Role();
		udRole.setUserSupport(false);
		udRole.setElectionEvent(new ElectionEvent());
		operatorRole.setRole(udRole);
		Operator operator = new Operator();
		operator.setElectionEvent(new ElectionEvent());
		operatorRole.setOperator(operator);
		userData.setOperatorRole(operatorRole);

		mvArea = mvAreaRepository.findSingleByPath("000000.47.03.0301.030101.0101");
		contest = (Contest) getEntityManager()
				.createQuery("select c from Contest c where c.election.electionGroup.electionEvent.id = '200701' and c.id = '000001'").getSingleResult();
		electionDay1 = (ElectionDay) getEntityManager()
			.createQuery("select ed from ElectionDay ed where ed.electionEvent.id = '200701' and ed.date = '2007-09-09'").getSingleResult();
		electionDay2 = (ElectionDay) getEntityManager()
			.createQuery("select ed from ElectionDay ed where ed.electionEvent.id = '200701' and ed.date = '2007-09-10'").getSingleResult();
		votingCategory = votingRepository.findVotingCategoryById(no.valg.eva.admin.common.voting.VotingCategory.VO.getId());
	}

	@Test
	public void findForVoByContestAndArea() {
		getEntityManager().persist(manualContestVoting1());
		getEntityManager().persist(manualContestVoting2());

		List<ManualContestVoting> manualContestVotings = manualContestVotingRepository.findForVoByContestAndArea(contest.getPk(), mvArea.getPk());

		assertThat(manualContestVotings).hasSize(2);
		ManualContestVoting manualContestVoting1 = manualContestVotings.get(0);
		assertThat(manualContestVoting1.getVotings()).isEqualTo(55);
		ManualContestVoting manualContestVoting2 = manualContestVotings.get(1);
		assertThat(manualContestVoting2.getVotings()).isEqualTo(33);
	}

	@Test
	public void createMany() {
		List<ManualContestVoting> manualContestVotings = new ArrayList<>();
		manualContestVotings.add(manualContestVoting1());
		manualContestVotings.add(manualContestVoting2());

		List<ManualContestVoting> createdManualContestVotings = manualContestVotingRepository.createMany(userData, manualContestVotings);

		assertThat(createdManualContestVotings).hasSize(2);
		ManualContestVoting createdManualContestVoting1 = createdManualContestVotings.get(0);
		assertThat(createdManualContestVoting1)
				.hasPk()
				.hasAuditTimestamp()
				.hasAuditOperationEqualTo("I")
				.hasAuditOplockEqualTo(0);
		assertThat(createdManualContestVoting1.getVotings()).isEqualTo(55);
		ManualContestVoting createdManualContestVoting2 = createdManualContestVotings.get(1);
		assertThat(createdManualContestVoting2)
				.hasPk()
				.hasAuditTimestamp()
				.hasAuditOplockEqualTo(0)
				.hasAuditOperationEqualTo("I");
		assertThat(createdManualContestVoting2.getVotings()).isEqualTo(33);
	}

	private ManualContestVoting manualContestVoting2() {
		ManualContestVoting manualContestVoting = new ManualContestVoting();
		manualContestVoting.setMvArea(mvArea);
		manualContestVoting.setContest(contest);
		manualContestVoting.setElectionDay(electionDay2);
		manualContestVoting.setVotingCategory(votingCategory);
		manualContestVoting.setVotings(33);
		return manualContestVoting;
	}

	private ManualContestVoting manualContestVoting1() {
		ManualContestVoting manualContestVoting = new ManualContestVoting();
		manualContestVoting.setMvArea(mvArea);
		manualContestVoting.setContest(contest);
		manualContestVoting.setElectionDay(electionDay1);
		manualContestVoting.setVotingCategory(votingCategory);
		manualContestVoting.setVotings(55);
		return manualContestVoting;
	}

	@Test
	public void updateMany() {
		ManualContestVoting manualContestVoting1 = manualContestVoting1();
		ManualContestVoting manualContestVoting2 = manualContestVoting2();
		getEntityManager().persist(manualContestVoting1);
		getEntityManager().persist(manualContestVoting2);
		manualContestVoting1 = getEntityManager().find(ManualContestVoting.class, manualContestVoting1.getPk());
		getEntityManager().detach(manualContestVoting1);
		manualContestVoting2 = getEntityManager().find(ManualContestVoting.class, manualContestVoting2.getPk());
		getEntityManager().detach(manualContestVoting2);
		List<ManualContestVoting> manualContestVotings = new ArrayList<>();
		manualContestVotings.add(manualContestVoting1);
		manualContestVotings.add(manualContestVoting2);

		manualContestVoting1.setVotings(44);
		manualContestVoting2.setVotings(22);
		List<ManualContestVoting> updatedManualContestVotings = manualContestVotingRepository.updateMany(userData, manualContestVotings);

		assertThat(updatedManualContestVotings).hasSize(2);
		ManualContestVoting updatedManualContestVoting1 = updatedManualContestVotings.get(0);
		assertThat(updatedManualContestVoting1)
				.hasAuditTimestamp()
				.hasAuditOplockEqualTo(1)
				.hasAuditOperationEqualTo("U");
		assertThat(updatedManualContestVoting1.getVotings()).isEqualTo(44);
		ManualContestVoting updatedManualContestVoting2 = updatedManualContestVotings.get(1);
		assertThat(updatedManualContestVoting2)
				.hasAuditTimestamp()
				.hasAuditOplockEqualTo(1)
				.hasAuditOperationEqualTo("U");
		assertThat(updatedManualContestVoting2.getVotings()).isEqualTo(22);
	}
}

