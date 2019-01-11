package no.valg.eva.admin.valgnatt.domain.service.resultat.stemmetall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall.Stemmetall;

import org.testng.annotations.Test;

public class StemmetallDomainServiceTest {

	private static final Map<String, Long> AFFILIATION_ID_TO_PK_MAP;

	static {
		AFFILIATION_ID_TO_PK_MAP = new HashMap<>();
		AFFILIATION_ID_TO_PK_MAP.put("A", 1L);
		AFFILIATION_ID_TO_PK_MAP.put("H", 2L);
	}

	@Test
	public void createFrom_returnsStemmetall() {
		StemmetallDomainService stemmetallDomainService = new StemmetallDomainService();

		List<BallotCount> allBallotCounts = makeBallotCounts(true);
		allBallotCounts.addAll(makeBallotCounts(false));
		List<Stemmetall> stemmetallList = stemmetallDomainService.hentStemmetall(allBallotCounts);

		Stemmetall stemmetallA = stemmetallList.stream().filter(stemmetall -> "A".equals(stemmetall.getPartiId())).collect(Collectors.toList()).get(0);
		Stemmetall stemmetallH = stemmetallList.stream().filter(stemmetall -> "H".equals(stemmetall.getPartiId())).collect(Collectors.toList()).get(0);
		assertThat(stemmetallA.getFhsForeløpig()).isEqualTo(2);

		assertThat(stemmetallA.getVtsForeløpig()).isEqualTo(6);
		assertThat(stemmetallH.getFhsForeløpig()).isEqualTo(4);
		assertThat(stemmetallH.getVtsForeløpig()).isEqualTo(10);

		assertThat(stemmetallA.getFhsEndelig()).isEqualTo(2);

		assertThat(stemmetallA.getVtsEndelig()).isEqualTo(6);
		assertThat(stemmetallH.getFhsEndelig()).isEqualTo(4);
		assertThat(stemmetallH.getVtsEndelig()).isEqualTo(10);

	}

	private List<BallotCount> makeBallotCounts(boolean endelig) {
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(makeBallotCount("A", true, 1, 1, "FO", endelig));
		ballotCounts.add(makeBallotCount("H", true, 2, 2, "FO", endelig));

		ballotCounts.add(makeBallotCount("A", false, 3, 3, "VO", endelig));
		ballotCounts.add(makeBallotCount("H", false, 5, 5, "VO", endelig));

		ballotCounts.add(makeBallotCount(EvoteConstants.PARTY_ID_BLANK, true, 1, 1, "FO", endelig));
		ballotCounts.add(makeBallotCount(EvoteConstants.PARTY_ID_BLANK, false, 3, 3, "VO", endelig));

		ballotCounts.add(mock(BallotCount.class, RETURNS_DEEP_STUBS));
		return ballotCounts;
	}

	private BallotCount makeBallotCount(String id, boolean forhåndsstemmer, int rettede, int urettede, String voteCountCategoryId, boolean endelig) {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.getBallotId()).thenReturn(id);
		when(ballotCount.isForhånd()).thenReturn(forhåndsstemmer);
		when(ballotCount.isValgting()).thenReturn(!forhåndsstemmer);
		when(ballotCount.getVoteCount().isEarlyVoting()).thenReturn(forhåndsstemmer);
		when(ballotCount.getVoteCount().getVoteCountCategoryId()).thenReturn(voteCountCategoryId);
		when(ballotCount.getModifiedBallots()).thenReturn(rettede);
		when(ballotCount.getUnmodifiedBallots()).thenReturn(urettede);
		when(ballotCount.hasBallot()).thenReturn(true);
		when(ballotCount.isForeløpigForhånd()).thenReturn(forhåndsstemmer && !endelig);
		when(ballotCount.isForeløpigValgting()).thenReturn(!forhåndsstemmer && !endelig);
		when(ballotCount.isEndeligForhånd()).thenReturn(forhåndsstemmer && endelig);
		when(ballotCount.isEndeligValgting()).thenReturn(!forhåndsstemmer && endelig);

		Affiliation affiliation = makeAffiliation(id);
		when(ballotCount.getBallot().getAffiliation()).thenReturn(affiliation);
		return ballotCount;
	}

	private Affiliation makeAffiliation(String id) {
		Affiliation affiliation = new Affiliation();
		affiliation.setBallot(makeBallot(id));
		affiliation.setPk(AFFILIATION_ID_TO_PK_MAP.get(id));
		affiliation.setParty(new Party(id, 0, null, null));
		return affiliation;
	}

	private Ballot makeBallot(String id) {
		Ballot ballot = new Ballot();
		ballot.setId(id);
		return ballot;
	}

	@Test
	public void createFrom_finalBallotCounts_returnsStemmetallForFinal() {
		StemmetallDomainService stemmetallDomainService = new StemmetallDomainService();

		List<Stemmetall> stemmetallList = stemmetallDomainService.hentStemmetall(makeBallotCounts(true));
		Stemmetall stemmetallA = stemmetallList.stream().filter(stemmetall -> "A".equals(stemmetall.getPartiId())).collect(Collectors.toList()).get(0);
		Stemmetall stemmetallH = stemmetallList.stream().filter(stemmetall -> "H".equals(stemmetall.getPartiId())).collect(Collectors.toList()).get(0);
		assertThat(stemmetallA.getFhsForeløpig()).isEqualTo(0);
		assertThat(stemmetallA.getVtsForeløpig()).isEqualTo(0);
		assertThat(stemmetallH.getFhsForeløpig()).isEqualTo(0);
		assertThat(stemmetallH.getVtsForeløpig()).isEqualTo(0);
		assertThat(stemmetallA.getFhsEndelig()).isEqualTo(2);

		assertThat(stemmetallA.getVtsEndelig()).isEqualTo(6);
		assertThat(stemmetallH.getFhsEndelig()).isEqualTo(4);
		assertThat(stemmetallH.getVtsEndelig()).isEqualTo(10);

	}

	@Test
	public void createFrom_returnsStemmetallWithListestemmer() {
		StemmetallDomainService stemmetallDomainService = new StemmetallDomainService();

		List<Stemmetall> stemmetallList = stemmetallDomainService.hentStemmetall(makeBallotCounts(true), makeAffiliationVoteCounts());

		Stemmetall stemmetallA = stemmetallList.stream().filter(stemmetall -> "A".equals(stemmetall.getPartiId())).collect(Collectors.toList()).get(0);
		assertThat(stemmetallA.getLis()).isEqualTo(1);
		Stemmetall stemmetallH = stemmetallList.stream().filter(stemmetall -> "H".equals(stemmetall.getPartiId())).collect(Collectors.toList()).get(0);
		assertThat(stemmetallH.getLis()).isEqualTo(2);
		// Blanke stemmer skal ikke få listestemmer
		List<Stemmetall> stemmetallBLANK = stemmetallList.stream().filter(stemmetall -> EvoteConstants.PARTY_ID_BLANK.equals(stemmetall.getPartiId())).collect(Collectors.toList());
		assertThat(stemmetallBLANK.size() == 0);
	}

	private Set<AffiliationVoteCount> makeAffiliationVoteCounts() {
		Set<AffiliationVoteCount> affiliationVoteCounts = new HashSet<>();
		affiliationVoteCounts.add(makeAffiliationVoteCount("A", 1));
		affiliationVoteCounts.add(makeAffiliationVoteCount("H", 2));
		return affiliationVoteCounts;
	}

	private AffiliationVoteCount makeAffiliationVoteCount(String id, int votes) {
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount();
		affiliationVoteCount.setAffiliation(makeAffiliation(id));
		affiliationVoteCount.setVotes(votes);
		return affiliationVoteCount;
	}
}
