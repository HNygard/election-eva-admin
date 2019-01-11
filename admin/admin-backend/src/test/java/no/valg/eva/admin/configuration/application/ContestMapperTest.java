package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class ContestMapperTest extends MockUtilsTestCase {

	@Test
	public void toListProposalConfig_withContest_shouldMapToListProposalConfig() throws Exception {
		ContestMapper mapper = initializeMocks(ContestMapper.class);
		Contest contest = new Contest();
		contest.setPk(1L);
		contest.setElection(createMock(Election.class));
		contest.setName("Name");
		contest.setMinCandidates(1);
		contest.setMaxCandidates(2);
		contest.setMinProposersNewParty(4);
		contest.setMinProposersOldParty(5);
		contest.setNumberOfPositions(8);

		Contest newContest = new Contest();
		newContest.setPk(contest.getPk());
		newContest.setElection(contest.getElection());
		newContest.setName(contest.getName());

		ListProposalConfig config = mapper.toListProposalConfig(AreaPath.from("111111.22.33"), contest);
		mapper.updateEntityFromListProposalData(newContest, config.getContestListProposalData());

		assertThat(newContest).isEqualToComparingFieldByField(contest);
	}

}

