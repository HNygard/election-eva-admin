package no.valg.eva.admin.settlement.domain.model.factory;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.ElectionSettlementConsumer;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatConsumer;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatQuotientConsumer;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionSettlement;
import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LevelingSeatFactory extends EntityFactory<LevelingSeatFactory, LevelingSeatConsumer>
		implements SettlementVisitor, ElectionSettlementConsumer, LevelingSeatQuotientConsumer {
	private final Map<CandidateSeatsKey, SortedSet<CandidateSeat>> candidateSeatsMap = new HashMap<>();
	private final List<LevelingSeatQuotient> levelingSeatQuotients = new ArrayList<>();
	private ElectionSettlement electionSettlement;
	private Map<Party, LevelingSeatSummary> levelingSeatSummaryMap;
	private Set<Contest> leveledContests = new HashSet<>();
	private Map<Party, Integer> partySeatsMap = new HashMap<>();
	private int lastRankNumber;
	private int lastSeatNumber;
	private List<LevelingSeat> levelingSeats;

	@Override
	public void visit(Settlement settlement) {
		// do nothing
	}

	@Override
	public void visit(AffiliationVoteCount affiliationVoteCount) {
		// do nothing
	}

	@Override
	public void visit(CandidateSeat candidateSeat) {
		if (candidateSeat.isElected()) {
			return;
		}
		CandidateSeatsKey candidateSeatsKey = new CandidateSeatsKey(candidateSeat.getContest(), candidateSeat.getParty());
		if (!candidateSeatsMap.containsKey(candidateSeatsKey)) {
			candidateSeatsMap.put(candidateSeatsKey, new TreeSet<>(this::orderCandidateSeatsBySeatNumber));
		}
		candidateSeatsMap.get(candidateSeatsKey).add(candidateSeat);
	}

	private int orderCandidateSeatsBySeatNumber(CandidateSeat candidateSeat1, CandidateSeat candidateSeat2) {
		return candidateSeat1.getSeatNumber() - candidateSeat2.getSeatNumber();
	}

	@Override
	public void consume(LevelingSeatQuotient levelingSeatQuotient) {
		levelingSeatQuotients.add(levelingSeatQuotient);
	}

	@Override
	public void consume(ElectionSettlement electionSettlement) {
		this.electionSettlement = electionSettlement;
	}

	@Override
	protected void updateConsumer(LevelingSeatConsumer consumer) {
		levelingSeats.forEach(consumer::consume);
	}

	@Override
	protected LevelingSeatFactory self() {
		return this;
	}

	public void buildLevelingSeats() {
		levelingSeatSummaryMap = levelingSeatSummaryMap();
		int levelingSeatCount = electionSettlement.getLevelingSeatSummaries().stream().mapToInt(LevelingSeatSummary::getLevelingSeats).sum();
		levelingSeats = new ArrayList<>();
		List<LevelingSeatQuotient> filteredAndSortedLevelingSeatQuotients = filteredAndSortedLevelingSeatQuotients();
		for (int i = 0; i < filteredAndSortedLevelingSeatQuotients.size() && lastSeatNumber < levelingSeatCount; i++) {
			levelingSeats.add(levelingSeat(filteredAndSortedLevelingSeatQuotients.get(i)));
		}
		for (int i = 0; i < levelingSeats.size() - 1; i++) {
			LevelingSeat currentLevelingSeat = levelingSeats.get(i);
			LevelingSeat nextLevelingSeat = levelingSeats.get(i + 1);
			currentLevelingSeat.setSameQuotientAsNext(sameQuotientAsNext(currentLevelingSeat, nextLevelingSeat));
			currentLevelingSeat.setSameVotesAsNext(sameVotesAsNext(currentLevelingSeat, nextLevelingSeat));
		}
		updateConsumers();
	}

	private Map<Party, LevelingSeatSummary> levelingSeatSummaryMap() {
		return electionSettlement.getLevelingSeatSummaries()
				.stream()
				.collect(toMap(LevelingSeatSummary::getParty, levelingSeatSummary -> levelingSeatSummary));
	}

	private List<LevelingSeatQuotient> filteredAndSortedLevelingSeatQuotients() {
		return levelingSeatQuotients
				.stream()
				.filter(levelingSeatQuotient -> levelingSeatSummaryMap.containsKey(levelingSeatQuotient.getParty()))
				.sorted(this::orderByDescendingQuotientsAndDescendingPartyVotes)
				.collect(toList());
	}

	private int orderByDescendingQuotientsAndDescendingPartyVotes(LevelingSeatQuotient levelingSeatQuotient1, LevelingSeatQuotient levelingSeatQuotient2) {
		if (levelingSeatQuotient2.getQuotient().compareTo(levelingSeatQuotient1.getQuotient()) == 0) {
			return levelingSeatQuotient2.getPartyVotes() - levelingSeatQuotient1.getPartyVotes();
		}
		return levelingSeatQuotient2.getQuotient().compareTo(levelingSeatQuotient1.getQuotient());
	}

	private LevelingSeat levelingSeat(LevelingSeatQuotient levelingSeatQuotient) {
		Contest contest = levelingSeatQuotient.getContest();
		Party party = levelingSeatQuotient.getParty();
		boolean contestLeveled = leveledContests.contains(contest);
		boolean partyLeveled = partyLeveled(party);

		LevelingSeat levelingSeat = new LevelingSeat();
		levelingSeat.setRankNumber(++lastRankNumber);
		levelingSeat.setLevelingSeatQuotient(levelingSeatQuotient);
		levelingSeat.setContestLeveled(contestLeveled);
		levelingSeat.setPartyLeveled(partyLeveled);

		if (contestLeveled || partyLeveled) {
			return levelingSeat;
		}

		leveledContests.add(contest);
		incrementPartySeats(party);

		levelingSeat.setSeatNumber(++lastSeatNumber);
		levelingSeat.setCandidateSeat(nextCandidateSeat(contest, party));

		return levelingSeat;
	}

	private boolean partyLeveled(Party party) {
		return partySeats(party) >= levelingSeatSummaryMap.get(party).getLevelingSeats();
	}

	private int partySeats(Party party) {
		return optional(partySeatsMap.get(party)).orElse(0);
	}

	private <T> Optional<T> optional(T t) {
		return Optional.ofNullable(t);
	}

	private void incrementPartySeats(Party party) {
		partySeatsMap.put(party, partySeats(party) + 1);
	}

	private CandidateSeat nextCandidateSeat(Contest contest, Party party) {
		SortedSet<CandidateSeat> candidateSeats = candidateSeatsMap.get(new CandidateSeatsKey(contest, party));
		CandidateSeat candidateSeat = candidateSeats.first();
		candidateSeats.remove(candidateSeat);
		return candidateSeat;
	}

	private boolean sameQuotientAsNext(LevelingSeat currentLevelingSeat, LevelingSeat nextLevelingSeat) {
		LevelingSeatQuotient currentLevelingSeatQuotient = currentLevelingSeat.getLevelingSeatQuotient();
		LevelingSeatQuotient nextLevelingSeatQuotient = nextLevelingSeat.getLevelingSeatQuotient();
		return currentLevelingSeatQuotient.getQuotient().compareTo(nextLevelingSeatQuotient.getQuotient()) == 0;
	}

	private boolean sameVotesAsNext(LevelingSeat currentLevelingSeat, LevelingSeat nextLevelingSeat) {
		LevelingSeatQuotient currentLevelingSeatQuotient = currentLevelingSeat.getLevelingSeatQuotient();
		LevelingSeatQuotient nextLevelingSeatQuotient = nextLevelingSeat.getLevelingSeatQuotient();
		return currentLevelingSeatQuotient.getPartyVotes() == nextLevelingSeatQuotient.getPartyVotes();
	}

	private static class CandidateSeatsKey {
		private final Contest contest;
		private final Party party;

		CandidateSeatsKey(Contest contest, Party party) {
			this.contest = contest;
			this.party = party;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CandidateSeatsKey)) {
				return false;
			}
			CandidateSeatsKey that = (CandidateSeatsKey) o;
			return new EqualsBuilder()
					.append(contest, that.contest)
					.append(party, that.party)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(contest)
					.append(party)
					.toHashCode();
		}
	}
}
