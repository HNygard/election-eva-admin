package no.valg.eva.admin.settlement.domain.model.factory;

import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.ModifiedSainteLague;
import no.valg.eva.admin.settlement.domain.consumer.ElectionSeatConsumer;
import no.valg.eva.admin.settlement.domain.consumer.ElectionVoteCountConsumer;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatSummaryConsumer;
import no.valg.eva.admin.settlement.domain.model.ElectionSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionVoteCount;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;

public class ElectionSeatFactory extends EntityFactory<ElectionSeatFactory, ElectionSeatConsumer>
		implements ElectionVoteCountConsumer, LevelingSeatSummaryConsumer {
	private static final int QUOTIENT_SCALE = 14;
	private ModifiedSainteLague modifiedSainteLague;
	private int levelingSeatCount;
	private List<ElectionVoteCount> electionVoteCounts = new ArrayList<>();
	private List<ElectionSeat> electionSeats;
	private boolean moreElectionSeatsNeeded;

	public ElectionSeatFactory(BigDecimal firstDivisor, int levelingSeatCount) {
		this.modifiedSainteLague = new ModifiedSainteLague(firstDivisor);
		this.levelingSeatCount = levelingSeatCount;
	}

	@Override
	protected void updateConsumer(ElectionSeatConsumer electionSeatConsumer) {
		electionSeats.forEach(electionSeatConsumer::consume);
	}

	@Override
	protected ElectionSeatFactory self() {
		return this;
	}

	@Override
	public void consume(ElectionVoteCount electionVoteCount) {
		if (electionVoteCount.isEligibleForLevelingSeats()) {
			electionVoteCounts.add(electionVoteCount);
		}
	}

	@Override
	public void consume(LevelingSeatSummary levelingSeatSummary) {
		if (levelingSeatSummary.hasMoreContestSeatsThanElectionSeats()) {
			moreElectionSeatsNeeded = true;
			removeElectionVoteCountForParty(levelingSeatSummary.getParty());
		}
	}

	private void removeElectionVoteCountForParty(Party party) {
		Iterator<ElectionVoteCount> electionVoteCountIterator = electionVoteCounts.iterator();
		while (electionVoteCountIterator.hasNext()) {
			ElectionVoteCount electionVoteCount = electionVoteCountIterator.next();
			if (electionVoteCount.getParty().equals(party)) {
				electionVoteCountIterator.remove();
				break;
			}
		}
	}

	public void buildElectionSeats() {
		int electionSeatCount = levelingSeatCount + electionVoteCounts.stream().mapToInt(ElectionVoteCount::getContestSeats).sum();
		buildElectionSeatsFromElectionVoteCounts(electionSeatCount);
		updateElectionSeats(electionSeatCount);
		updateConsumers();
		moreElectionSeatsNeeded = false;
		electionSeats = null;
	}

	private void buildElectionSeatsFromElectionVoteCounts(int electionSeatCount) {
		electionSeats = electionVoteCounts.stream()
				.map(electionVoteCount -> buildElectionSeatsFromElectionVoteCount(electionVoteCount, electionSeatCount))
				.flatMap(s -> s)
				.sorted(this::orderByQuotientAndPartyVotes)
				.collect(toList());
	}

	private Stream<ElectionSeat> buildElectionSeatsFromElectionVoteCount(ElectionVoteCount electionVoteCount, int electionSeatCount) {
		return IntStream.rangeClosed(1, electionSeatCount).mapToObj(rankNumber -> buildElectionSeatFromElectionVoteCount(electionVoteCount, rankNumber));
	}

	private ElectionSeat buildElectionSeatFromElectionVoteCount(ElectionVoteCount electionVoteCount, int rankNumber) {
		BigDecimal divisor = modifiedSainteLague.saintLagueDivisor(rankNumber);
		ElectionSeat electionSeat = new ElectionSeat();
		electionSeat.setParty(electionVoteCount.getParty());
		electionSeat.setQuotient(BigDecimal.valueOf(electionVoteCount.getVotes()).divide(divisor, QUOTIENT_SCALE, HALF_UP));
		electionSeat.setDividend(electionVoteCount.getVotes());
		electionSeat.setDivisor(divisor);
		return electionSeat;
	}

	private int orderByQuotientAndPartyVotes(ElectionSeat electionSeat1, ElectionSeat electionSeat2) {
		if (electionSeat2.getQuotient().equals(electionSeat1.getQuotient())) {
			return electionSeat2.getDividend() - electionSeat1.getDividend();
		}
		return electionSeat2.getQuotient().compareTo(electionSeat1.getQuotient());
	}

	private void updateElectionSeats(int electionSeatCount) {
		for (int i = 0; i < electionSeats.size(); i++) {
			ElectionSeat electionSeat = electionSeats.get(i);
			int seatNumber = i + 1;
			electionSeat.setSeatNumber(seatNumber);
			electionSeat.setElected(seatNumber <= electionSeatCount);
			if (i + 1 < electionSeats.size()) {
				ElectionSeat nextElectionSeat = electionSeats.get(i + 1);
				electionSeat.setSameQuotientAsNext(nextElectionSeat.getQuotient().equals(electionSeat.getQuotient()));
				electionSeat.setSameVotesAsNext(nextElectionSeat.getDividend() == electionSeat.getDividend());
			}
		}
	}

	public boolean isMoreElectionSeatsNeeded() {
		return moreElectionSeatsNeeded;
	}
}
