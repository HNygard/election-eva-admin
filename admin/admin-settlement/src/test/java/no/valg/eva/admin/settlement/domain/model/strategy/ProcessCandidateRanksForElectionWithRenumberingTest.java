package no.valg.eva.admin.settlement.domain.model.strategy;

import static java.math.BigDecimal.ONE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ProcessCandidateRanksForElectionWithRenumberingTest {
	private static final ThreadLocal<Map<CandidateKey, Candidate>> CANDIDATE_MAP_THREAD_LOCAL = new ThreadLocal<>();
	private static final ThreadLocal<Map<Long, Affiliation>> AFFILIATION_MAP_THREAD_LOCAL = new ThreadLocal<>();

	@DataProvider
	public static Object[][] processCandidateRanks() {
		long a1 = 1; // affiliation 1
		long a2 = 2; // affiliation 2
		long a3 = 3; // affiliation 3
		long c1 = 1; // candidate 1
		long c2 = 2; // candidate 2
		long c3 = 3; // candidate 2
		int rn1 = 1; // rank number 1
		int rn2 = 2; // rank number 2
		int rn3 = 3; // rank number 3
		BigDecimal v1 = ONE;
		BigDecimal v2 = v1.add(ONE);
		BigDecimal v3 = v2.add(ONE);
		int do1 = 1; // display order 1
		int do2 = 2; // display order 2
		int do3 = 3; // display order 3
		int cr1 = 1; // candidate rank 1
		int cr2 = 2; // candidate rank 2
		int cr3 = 3; // candidate rank 3

		// groups of test data with three candidate ranks and expected ranks and ordering in the result
		return new Object[][] {
				// given three candidate ranks: three different affiliations
				// => three ranks ordered by affiliation pk
				{ candidateRank(1, a1, c3, rn1, v1, do1), candidateRank(2, a2, c2, rn1, v1, do1), candidateRank(3, a3, c1, rn1, v1, do1),
						array(cr1, cr2, cr3) },
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a3, c2, rn1, v1, do1), candidateRank(3, a2, c3, rn1, v1, do1),
						array(cr1, cr3, cr2) },
				{ candidateRank(1, a2, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a3, c3, rn1, v1, do1),
						array(cr2, cr1, cr3) },
				{ candidateRank(1, a2, c1, rn1, v1, do1), candidateRank(2, a3, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn1, v1, do1),
						array(cr3, cr1, cr2) },
				{ candidateRank(1, a3, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a2, c3, rn1, v1, do1),
						array(cr2, cr3, cr1) },
				{ candidateRank(1, a3, c1, rn1, v1, do1), candidateRank(2, a2, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn1, v1, do1),
						array(cr3, cr2, cr1) },

				// given three candidate ranks: same affiliation, different candidates, different ranks
				// => three ranks ordered by rank number
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn2, v1, do1), candidateRank(3, a1, c3, rn3, v1, do1),
						array(cr1, cr2, cr3) },
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn3, v1, do1), candidateRank(3, a1, c3, rn2, v1, do1),
						array(cr1, cr3, cr2) },
				{ candidateRank(1, a1, c1, rn2, v1, do1), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn3, v1, do1),
						array(cr2, cr1, cr3) },
				{ candidateRank(1, a1, c1, rn2, v1, do1), candidateRank(2, a1, c2, rn3, v1, do1), candidateRank(3, a1, c3, rn1, v1, do1),
						array(cr3, cr1, cr2) },
				{ candidateRank(1, a1, c1, rn3, v1, do1), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn2, v1, do1),
						array(cr2, cr3, cr1) },
				{ candidateRank(1, a1, c1, rn3, v1, do1), candidateRank(2, a1, c2, rn2, v1, do1), candidateRank(3, a1, c3, rn1, v1, do1),
						array(cr3, cr2, cr1) },

				// given three candidate ranks: same affiliation, same candidate, different ranks
				// => one candidate rank with the highest rank number
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c1, rn2, v1, do1), candidateRank(3, a1, c1, rn3, v1, do1), array(cr1) },
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c1, rn3, v1, do1), candidateRank(3, a1, c1, rn2, v1, do1), array(cr1) },
				{ candidateRank(1, a1, c1, rn2, v1, do1), candidateRank(2, a1, c1, rn1, v1, do1), candidateRank(3, a1, c1, rn3, v1, do1), array(cr2) },
				{ candidateRank(1, a1, c1, rn2, v1, do1), candidateRank(2, a1, c1, rn3, v1, do1), candidateRank(3, a1, c1, rn1, v1, do1), array(cr3) },
				{ candidateRank(1, a1, c1, rn3, v1, do1), candidateRank(2, a1, c1, rn1, v1, do1), candidateRank(3, a1, c1, rn2, v1, do1), array(cr2) },
				{ candidateRank(1, a1, c1, rn3, v1, do1), candidateRank(2, a1, c1, rn2, v1, do1), candidateRank(3, a1, c1, rn1, v1, do1), array(cr3) },

				// given three candidate ranks: same affiliation, different candidates, same rank
				// => one candidate rank with the highest votes
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn1, v2, do1), candidateRank(3, a1, c3, rn1, v3, do1), array(cr3) },
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn1, v3, do1), candidateRank(3, a1, c3, rn1, v2, do1), array(cr2) },
				{ candidateRank(1, a1, c1, rn1, v2, do1), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn1, v3, do1), array(cr3) },
				{ candidateRank(1, a1, c1, rn1, v2, do1), candidateRank(2, a1, c2, rn1, v3, do1), candidateRank(3, a1, c3, rn1, v1, do1), array(cr2) },
				{ candidateRank(1, a1, c1, rn1, v3, do1), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn1, v2, do1), array(cr1) },
				{ candidateRank(1, a1, c1, rn1, v3, do1), candidateRank(2, a1, c2, rn1, v2, do1), candidateRank(3, a1, c3, rn1, v1, do1), array(cr1) },

				// given three candidate ranks: same affiliation, different candidate, same rank, same vote
				// => three ranks ordered by candidate display order
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn1, v1, do2), candidateRank(3, a1, c3, rn1, v1, do3), array(cr1) },
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a1, c2, rn1, v1, do3), candidateRank(3, a1, c3, rn1, v1, do2), array(cr1) },
				{ candidateRank(1, a1, c1, rn1, v1, do2), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn1, v1, do3), array(cr2) },
				{ candidateRank(1, a1, c1, rn1, v1, do2), candidateRank(2, a1, c2, rn1, v1, do3), candidateRank(3, a1, c3, rn1, v1, do1), array(cr3) },
				{ candidateRank(1, a1, c1, rn1, v1, do3), candidateRank(2, a1, c2, rn1, v1, do1), candidateRank(3, a1, c3, rn1, v1, do2), array(cr2) },
				{ candidateRank(1, a1, c1, rn1, v1, do3), candidateRank(2, a1, c2, rn1, v1, do2), candidateRank(3, a1, c3, rn1, v1, do1), array(cr3) },

				//given three candidate ranks: two having same affiliation, rank and vote, and on candidate rank with another affiliation in between
				{ candidateRank(1, a1, c1, rn1, v1, do1), candidateRank(2, a2, c2, rn1, v1, do2), candidateRank(3, a1, c3, rn1, v1, do2), array(cr1, cr2) }
		};
	}

	private static int[] array(int... ints) {
		return ints;
	}

	private static Candidate candidate(long candidatePk, int displayOrder) {
		Map<CandidateKey, Candidate> candidateMap = CANDIDATE_MAP_THREAD_LOCAL.get();
		if (candidateMap == null) {
			candidateMap = new HashMap<>();
			CANDIDATE_MAP_THREAD_LOCAL.set(candidateMap);
		}
		CandidateKey key = new CandidateKey(candidatePk, displayOrder);
		if (!candidateMap.containsKey(key)) {
			Candidate candidate = mock(Candidate.class);
			when(candidate.getPk()).thenReturn(candidatePk);
			when(candidate.getDisplayOrder()).thenReturn(displayOrder);
			candidateMap.put(key, candidate);
		}
		return candidateMap.get(key);
	}

	private static Affiliation affiliation(long affiliationPk) {
		Map<Long, Affiliation> affiliationMap = AFFILIATION_MAP_THREAD_LOCAL.get();
		if (affiliationMap == null) {
			affiliationMap = new HashMap<>();
			AFFILIATION_MAP_THREAD_LOCAL.set(affiliationMap);
		}
		if (!affiliationMap.containsKey(affiliationPk)) {
			Affiliation affiliation = mock(Affiliation.class);
			when(affiliation.getPk()).thenReturn(affiliationPk);
			affiliationMap.put(affiliationPk, affiliation);
		}
		return affiliationMap.get(affiliationPk);
	}

	private static CandidateRank candidateRank(long pk, long affiliationPk, long candidatePk, int rankNumber, BigDecimal votes, int displayOrder) {
		CandidateRank candidateRank = new CandidateRank(candidate(candidatePk, displayOrder), affiliation(affiliationPk), votes, rankNumber);
		candidateRank.setPk(pk);
		return candidateRank;
	}

	@Test(dataProvider = "processCandidateRanks")
	public void testProcessCandidateRanks(
			CandidateRank candidateRank1, CandidateRank candidateRank2, CandidateRank candidateRank3, int[] expectedCandidateRanks) throws Exception {
		List<CandidateRank> candidateRanks = asList(candidateRank1, candidateRank2, candidateRank3);
		List<CandidateRank> result = new ProcessCandidateRanksForElectionWithRenumbering().processCandidateRanks(candidateRanks);
		assertThat(result).hasSize(expectedCandidateRanks.length);
		for (int expectedCandidateRank : expectedCandidateRanks) {
			assertThat(result).contains(candidateRanks.get(expectedCandidateRank - 1));
		}
	}

	private static class CandidateKey {
		private long candidatePk;
		private int displayOrder;

		public CandidateKey(long candidatePk, int displayOrder) {
			this.candidatePk = candidatePk;
			this.displayOrder = displayOrder;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CandidateKey)) {
				return false;
			}
			CandidateKey that = (CandidateKey) o;
			return new EqualsBuilder()
					.append(candidatePk, that.candidatePk)
					.append(displayOrder, that.displayOrder)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(candidatePk)
					.append(displayOrder)
					.toHashCode();
		}
	}
}

