package no.valg.eva.admin.settlement.domain.model.factory;

import static com.codepoetics.protonpack.StreamUtils.zipWithIndex;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.Randomizer;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.consumer.CandidateSeatConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateSeatEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;

import com.codepoetics.protonpack.Indexed;

public class CandidateSeatFactory extends EntityFactory<CandidateSeatFactory, CandidateSeatConsumer> implements CandidateSeatEventListener {
	private final int numberOfPositionsInContest;
	private final Map<Candidate, CandidateSeat> candidateSeatMap = new LinkedHashMap<>();
	private final Randomizer randomizer;
	private List<CandidateSeat> updatedCandidateSeats;

	public CandidateSeatFactory(int numberOfPositionsInContest) {
		this.numberOfPositionsInContest = numberOfPositionsInContest;
		this.randomizer = Randomizer.INSTANCE;
	}

	@Override
	public void candidateSeatDelta(CandidateSeatEvent event) {
		Candidate candidate = event.getCandidate();
		Integer dividend = event.getDividend();
		BigDecimal divisor = event.getDivisor();
		if (!candidateSeatMap.containsKey(candidate)) {
			Affiliation affiliation = event.getAffiliation();
			CandidateSeat candidateSeat;
			if (dividend != null) {
				candidateSeat = new CandidateSeat(candidate, affiliation, 0, dividend, null, false);
			} else {
				candidateSeat = new CandidateSeat(candidate, affiliation, 0, 0, divisor, false);
			}
			candidateSeatMap.put(candidate, candidateSeat);
		} else {
			CandidateSeat candidateSeat = candidateSeatMap.get(candidate);
			if (dividend != null) {
				candidateSeat.setDividend(dividend);
			} else {
				candidateSeat.setDivisor(divisor);
			}
		}
	}

	public void buildCandidateSeats() {
		CandidateSeatComparator candidateSeatComparator = new CandidateSeatComparator(randomizer);
		updatedCandidateSeats = zipWithIndex(candidateSeatMap
				.values()
				.stream()
				.sorted(candidateSeatComparator))
						.map(this::updateCandidateSeat)
						.collect(toList());
		updateConsumers();
		updatedCandidateSeats = null;
	}

	private CandidateSeat updateCandidateSeat(Indexed<CandidateSeat> indexedCandidateSeat) {
		long index = indexedCandidateSeat.getIndex();
		CandidateSeat candidateSeat = indexedCandidateSeat.getValue();
		candidateSeat.updateSeatNumberAndElectedState((int) index + 1, numberOfPositionsInContest);
		return candidateSeat;
	}

	@Override
	protected void updateConsumer(CandidateSeatConsumer candidateSeatConsumer) {
		updatedCandidateSeats.forEach(candidateSeatConsumer::consume);
	}

	@Override
	protected CandidateSeatFactory self() {
		return this;
	}
}
