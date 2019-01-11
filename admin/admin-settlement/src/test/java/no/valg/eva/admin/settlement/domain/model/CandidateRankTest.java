package no.valg.eva.admin.settlement.domain.model;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class CandidateRankTest {

	@Test
	public void incrementVotes() throws Exception {
		CandidateRank candidateRank = new CandidateRank();
		candidateRank.setVotes(TEN);
		candidateRank.incrementVotes(TEN);
		assertThat(candidateRank.getVotes()).isEqualTo(TEN.add(TEN));
	}
}
