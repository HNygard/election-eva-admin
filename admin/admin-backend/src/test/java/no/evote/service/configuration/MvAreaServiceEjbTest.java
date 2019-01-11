package no.evote.service.configuration;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MvAreaServiceEjbTest extends MockUtilsTestCase {

	@Test
	public void findValgdistriktStierByValgStiWhereAllListProposalsAreApproved_returnererValgdistrikter() throws Exception {
		MvAreaServiceEjb ejb = initializeMocks(MvAreaServiceEjb.class);
		stub_findContestsByElectionWhereAllBallotsAreProcessed();

		List<ValgdistriktSti> result = ejb.findValgdistriktStierByValgStiWhereAllListProposalsAreApproved(
				createMock(UserData.class), ValghierarkiSti.valgSti(ELECTION_PATH_ELECTION));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).electionPath()).isEqualTo(ELECTION_PATH_CONTEST);
	}

	private void stub_findContestsByElectionWhereAllBallotsAreProcessed() {
		List<Contest> contests = singletonList(
				contest(ELECTION_PATH_CONTEST));
		when(getInjectMock(MvAreaRepository.class).findContestsByElectionWhereAllBallotsAreProcessed(any(ElectionRef.class)))
				.thenReturn(contests);
	}

	private Contest contest(ElectionPath electionPath) {
		Contest contest = createMock(Contest.class);
		when(contest.electionPath()).thenReturn(electionPath);
		return contest;
	}
}
