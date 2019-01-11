package no.valg.eva.admin.settlement.domain.model.strategy;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ProcessCandidateRanksForElectionWithPersonalVotesTest {
	@DataProvider
	public static Object[][] candidateRanksWithDifferentOrdering() {
		long c1 = 1; // candidate 1
		long c2 = 2; // candidate 2
		long a1 = 1; // affiliation 1
		long a2 = 2; // affiliation 2
		int do1 = 1; // display order 1
		int do2 = 2; // display order 2
		BigDecimal cv0 = ZERO; // candidate votes zero
		BigDecimal cv1 = ONE; // candidate votes 1
		BigDecimal cv2 = ONE.add(ONE); // candidate votes 2
		BigDecimal cv10 = TEN; // candidate votes 10
		BigDecimal cv11 = TEN.add(ONE); // candidate votes 11
		int av100 = 100; // affiliation votes 100

		BigDecimal cv11point0 = new BigDecimal("11.0");
		
		return new Object[][] {
				// ordering candidates with different candidate votes and display order
				{ c1, a1, do1, cv10, av100, c2, a1, do2, cv10, av100, c1, c2 },
				{ c1, a1, do1, cv10, av100, c2, a1, do2, cv11, av100, c2, c1 },
				{ c1, a1, do1, cv11, av100, c2, a1, do2, cv10, av100, c1, c2 },
				{ c1, a1, do2, cv10, av100, c2, a1, do1, cv10, av100, c2, c1 },
				{ c1, a1, do2, cv10, av100, c2, a1, do1, cv11, av100, c2, c1 },
				{ c1, a1, do2, cv11, av100, c2, a1, do1, cv10, av100, c1, c2 },

				// ordering candidates from different affiliations (a1 before a2)
				{ c1, a2, do1, cv0, av100, c2, a1, do1, cv0, av100, c2, c1 },
				{ c1, a1, do1, cv0, av100, c2, a2, do1, cv0, av100, c1, c2 },

				// ordering candidates below candidate rank vote threshold (10% of 100 in the test below)
				{ c1, a1, do1, cv1, av100, c2, a1, do2, cv2, av100, c1, c2 },
				{ c1, a1, do2, cv1, av100, c2, a1, do1, cv2, av100, c2, c1 },
				
				// test for 1.0 vs 1 equality vs compare to on BigDecimal
				{ c1, a1, do2, cv11point0, av100, c2, a1, do1, cv11, av100, c2, c1 }
		};
	}

	@Test(dataProvider = "candidateRanksWithDifferentOrdering")
	public void processCandidateRanks_givenCandidateRanksAndAffiliationVoteCounts_returnsOrderedCandidateRanks(
			long candidatePk1, long affiliationPk1, int displayOrder1, BigDecimal candidateVotes1, int affiliationVotes1,
			long candidatePk2, long affiliationPk2, int displayOrder2, BigDecimal candidateVotes2, int affiliationVotes2,
			long expectedCandidatePk1, long expectedCandidatePk2) throws Exception {
		BigDecimal candidateRankVoteShareThreshold = new BigDecimal("0.10");
		Affiliation affiliation1 = affiliation(affiliationPk1);
		Affiliation affiliation2 = affiliation(affiliationPk2);
		Collection<CandidateRank> candidateRanks = candidateRanks(
				candidateRank(candidate(candidatePk1, displayOrder1), affiliation1, candidateVotes1),
				candidateRank(candidate(candidatePk2, displayOrder2), affiliation2, candidateVotes2));

		ProcessCandidateRanksForElectionWithPersonalVotes processCandidateRanksForElectionWithPersonalVotes = new ProcessCandidateRanksForElectionWithPersonalVotes(
				candidateRankVoteShareThreshold);
		processCandidateRanksForElectionWithPersonalVotes.consume(affiliationVoteCount(affiliationVotes1, affiliation1));
		processCandidateRanksForElectionWithPersonalVotes.consume(affiliationVoteCount(affiliationVotes2, affiliation2));
		List<CandidateRank> result = processCandidateRanksForElectionWithPersonalVotes.processCandidateRanks(candidateRanks);

		assertOrderCandidateRank(expectedCandidatePk1, result.get(0), 1);
		if (affiliationPk1 == affiliationPk2) {
			assertOrderCandidateRank(expectedCandidatePk2, result.get(1), 2);
		} else {
			assertOrderCandidateRank(expectedCandidatePk2, result.get(1), 1);
		}
	}

	private Affiliation affiliation(long affiliationPk) {
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.getPk()).thenReturn(affiliationPk);
		return affiliation;
	}

	private Collection<CandidateRank> candidateRanks(CandidateRank... candidateRanks) {
		return asList(candidateRanks);
	}

	private CandidateRank candidateRank(Candidate candidate, Affiliation affiliation, BigDecimal candidateVotes) {
		CandidateRank candidateRank = new CandidateRank();
		candidateRank.setCandidate(candidate);
		candidateRank.setAffiliation(affiliation);
		candidateRank.setVotes(candidateVotes);
		return candidateRank;
	}

	private Candidate candidate(long candidatePk1, int displayOrder1) {
		Candidate candidate = mock(Candidate.class);
		when(candidate.getPk()).thenReturn(candidatePk1);
		when(candidate.getDisplayOrder()).thenReturn(displayOrder1);
		return candidate;
	}

	private AffiliationVoteCount affiliationVoteCount(int affiliationVotes, Affiliation affiliation) {
		AffiliationVoteCount affiliationVoteCount = mock(AffiliationVoteCount.class);
		when(affiliationVoteCount.getAffiliation()).thenReturn(affiliation);
		when(affiliationVoteCount.getVotes()).thenReturn(affiliationVotes);
		return affiliationVoteCount;
	}

	private void assertOrderCandidateRank(long expectedCandidatePk, CandidateRank candidateRank, int rankNumber) {
		assertThat(candidateRank.getCandidate().getPk()).isEqualTo(expectedCandidatePk);
		assertThat(candidateRank.getRankNumber()).isEqualTo(rankNumber);
	}
}

