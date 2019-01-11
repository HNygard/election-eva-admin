package no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Stemmeseddelstatistikk;

import org.testng.annotations.Test;

public class ValgnattstatistikkDomainServiceTest {

	@Test
	public void createStatistikk_lagerStatistikk() {
		ValgnattstatistikkDomainService valgnattstatistikkDomainService = new ValgnattstatistikkDomainService(null, null);
		Stemmeseddelstatistikk stemmeseddelstatistikk = valgnattstatistikkDomainService.stemmeseddelstatistikk(makeBallotCounts());

		assertThat(stemmeseddelstatistikk.getForkastedeForhåndsstemmesedler()).isEqualTo(1);
		assertThat(stemmeseddelstatistikk.getForkastedeValgtingsstemmesedler()).isEqualTo(0);
		assertThat(stemmeseddelstatistikk.getGodkjenteForhåndsstemmesedler()).isEqualTo(2);
		assertThat(stemmeseddelstatistikk.getGodkjenteValgtingsstemmesedler()).isEqualTo(0);
	}

	private List<BallotCount> makeBallotCounts() {
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(makeBallotCount(new Ballot()));
		ballotCounts.add(makeBallotCount(new Ballot()));
		ballotCounts.add(makeBallotCount(null));
		return ballotCounts;
	}

	private BallotCount makeBallotCount(Ballot ballot) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setVoteCount(makeVoteCount());
		ballotCount.setBallot(ballot);
		ballotCount.setUnmodifiedBallots(0);
		ballotCount.setModifiedBallots(1);
		return ballotCount;
	}

	private VoteCount makeVoteCount() {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.isEarlyVoting()).thenReturn(true);
		return voteCount;
	}

}
