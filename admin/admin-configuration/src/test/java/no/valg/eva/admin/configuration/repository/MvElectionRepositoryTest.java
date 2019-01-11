package no.valg.eva.admin.configuration.repository;

import static org.testng.Assert.fail;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class MvElectionRepositoryTest extends AbstractJpaTestBase {
	
	@Test
	public void findContestsForElectionAndArea_always_returnsResultsSortedByElectionPath() {
		ElectionPath electionPath = ElectionPath.from("200701.01.01");
		AreaPath areaPath = AreaPath.from("200701");
		
		MvElectionRepository mvElectionRepository = new MvElectionRepository(getEntityManager());
		
		List<MvElection> contests = mvElectionRepository.findContestsForElectionAndArea(electionPath, areaPath);

		assertContestsSortedByElectionPath(contests);
	}

	private void assertContestsSortedByElectionPath(List<MvElection> contests) {
		contests.stream().reduce(this::checkSorting);
	}

	private MvElection checkSorting(MvElection contest1, MvElection contest2) {
		if (contest1.getElectionPath().compareTo(contest2.getElectionPath()) > 0) {
			fail("Sorting of contests is incorrect! Contest " + toString(contest2) + " should be before contest " + toString(contest1));
		}
		return contest2;
	}

	private String toString(MvElection contest) {
		return contest.getContestName() + " with id " + contest.getContestId();
	}
}
