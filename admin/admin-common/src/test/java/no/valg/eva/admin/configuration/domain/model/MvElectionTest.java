package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import no.valg.eva.admin.common.AreaPath;

public class MvElectionTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
	public void contestAreaPath_notContestLevel_throwsException() {
		MvElection mvElection = new MvElection();
		mvElection.setElectionPath("773400.01.01");

		AreaPath areaPath = new AreaPath("773400.47.20.2025");
		mvElection.contestAreaPath(areaPath);
	}

    @Test
	public void contestAreaPath_contestExistsOnAreaPath_givenAreaPathIsReturned() {
		MvElection mvElection = new MvElection();
		mvElection.setElectionPath("773400.01.01.000001");
		AreaPath areaPath = new AreaPath("773400.47.20.2025");
		mvElection.setContest(makeContest(areaPath.path()));

		assertThat(mvElection.contestAreaPath(areaPath)).isEqualTo(areaPath);
	}

    @Test
	public void contestAreaPath_contestDoesNotExistsOnAreaPath_defaultAreaPathIsReturned() {
		MvElection mvElection = new MvElection();
		mvElection.setElectionPath("773400.01.01.000001");
		AreaPath defaultAreaPath = new AreaPath("773400.47.00.0001");
		mvElection.setContest(makeContest(defaultAreaPath.path()));
		
		AreaPath areaPath = new AreaPath("773400.47.20.2025");

		assertThat(mvElection.contestAreaPath(areaPath)).isEqualTo(defaultAreaPath);
	}

	private Contest makeContest(String areaPath) {
		Contest contest = new Contest();
		Set<ContestArea> contestAreaSet = new HashSet<>();
		contestAreaSet.add(makeContestArea(areaPath));
		contest.setContestAreaSet(contestAreaSet);
		return contest;
	}

	private ContestArea makeContestArea(String areaPath) {
		ContestArea contestArea = new ContestArea();
		contestArea.setMvArea(makeMvArea(areaPath));
		return contestArea;
	}

	private MvArea makeMvArea(String areaPath) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath);
		return mvArea;
	}

}
