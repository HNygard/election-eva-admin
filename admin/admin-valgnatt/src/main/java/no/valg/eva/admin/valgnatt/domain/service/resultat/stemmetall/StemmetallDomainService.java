package no.valg.eva.admin.valgnatt.domain.service.resultat.stemmetall;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall.Stemmetall;

/**
 * Henter ut Stemmetall fra BallotCount.
 */
public class StemmetallDomainService {

	public List<Stemmetall> hentStemmetall(List<BallotCount> ballotCounts) {
		return hentStemmetall(ballotCounts, Collections.emptySet());
	}

	public List<Stemmetall> hentStemmetall(List<BallotCount> ballotCounts, Set<AffiliationVoteCount> affiliationVoteCounts) {
		Map<String, List<BallotCount>> ballotCountPerId = ballotCountsPerParty(ballotCounts);

		return ballotCountPerId.entrySet().stream()
				.map(e -> new Stemmetall(
						e.getKey(),
						summerStemmesedler(e.getValue(), foreløpigeForhåndsstemmer()),
						summerStemmesedler(e.getValue(), foreløpigeValgtingsstemmer()),
						hasForeløpigeForhåndsstemmer(ballotCounts),
						hasForeløpigeValgtingsstemmer(ballotCounts),
						summerStemmesedler(e.getValue(), endeligeForhåndsstemmer()),
						summerStemmesedler(e.getValue(), endeligeValgtingsstemmer()),
						hasEndeligeForhåndsstemmer(ballotCounts),
						hasEndeligeValgtingsstemmer(ballotCounts),
						listestemmer(e.getValue(), affiliationVoteCounts)
				))
				.collect(Collectors.toList());

	}

	private Integer summerStemmesedler(List<BallotCount> ballotCounts, Predicate<BallotCount> predicate) {
		return ballotCounts
				.stream()
				.filter(predicate)
				.mapToInt(ballotCount -> ballotCount.getUnmodifiedBallots() + ballotCount.getModifiedBallots())
				.sum();
	}

	private Map<String, List<BallotCount>> ballotCountsPerParty(List<BallotCount> ballotCounts) {
		return ballotCounts.stream()
				.filter(BallotCount::hasBallot)
				.collect(groupingBy(BallotCount::getBallotId));
	}

	/**
	 * @param ballotCountsForBallotId tellinger for en gitt liste. Antar alle er for samme liste/ballotId.
	 * @param affiliationVoteCounts partistemmer (affiliation vote counts) for valgoppgjøret
	 * @return listestemmer
	 */
	private Integer listestemmer(List<BallotCount> ballotCountsForBallotId, Set<AffiliationVoteCount> affiliationVoteCounts) {

		if (ballotCountsForBallotId.isEmpty() || affiliationVoteCounts.isEmpty()) {
			return null;
		}

		Affiliation affiliation = ballotCountsForBallotId.get(0).getBallot().getAffiliation();
		if (affiliation.getParty().getId().equals(EvoteConstants.PARTY_ID_BLANK)) {
			return null;
		}

		return affiliationVoteCounts.stream()
				.filter(affiliationVoteCount -> affiliationVoteCount.getAffiliation().equals(affiliation))
				.findFirst()
				.map(AffiliationVoteCount::getVotes)
				.orElse(null);
	}

	private boolean hasForeløpigeForhåndsstemmer(List<BallotCount> ballotCounts) {
		return ballotCounts.stream().anyMatch(foreløpigeForhåndsstemmer());
	}

	private boolean hasForeløpigeValgtingsstemmer(List<BallotCount> ballotCounts) {
		return ballotCounts.stream().anyMatch(foreløpigeValgtingsstemmer());
	}

	private boolean hasEndeligeForhåndsstemmer(List<BallotCount> ballotCounts) {
		return ballotCounts.stream().anyMatch(endeligeForhåndsstemmer());
	}

	private boolean hasEndeligeValgtingsstemmer(List<BallotCount> ballotCounts) {
		return ballotCounts.stream().anyMatch(endeligeValgtingsstemmer());
	}

	private Predicate<BallotCount> foreløpigeForhåndsstemmer() {
		return BallotCount::isForeløpigForhånd;
	}

	private Predicate<BallotCount> endeligeForhåndsstemmer() {
		return BallotCount::isEndeligForhånd;
	}

	private Predicate<BallotCount> foreløpigeValgtingsstemmer() {
		return BallotCount::isForeløpigValgting;
	}

	private Predicate<BallotCount> endeligeValgtingsstemmer() {
		return BallotCount::isEndeligValgting;
	}

}
