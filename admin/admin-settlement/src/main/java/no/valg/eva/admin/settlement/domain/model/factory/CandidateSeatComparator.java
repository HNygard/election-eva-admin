package no.valg.eva.admin.settlement.domain.model.factory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import no.valg.eva.admin.common.Randomizer;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;

public class CandidateSeatComparator implements Comparator<CandidateSeat>, Serializable {
	private final Randomizer randomizer;
	private final Map<CandidateSeat, Integer> randomTickets = new HashMap<>();

	public CandidateSeatComparator(Randomizer randomizer) {
		this.randomizer = randomizer;
	}

	@Override
	public int compare(CandidateSeat candidateSeat1, CandidateSeat candidateSeat2) {
		if (candidateSeat1 == candidateSeat2) {
			return 0;
		}
		if (!candidateSeat2.getQuotient().equals(candidateSeat1.getQuotient())) {
			return candidateSeat2.getQuotient().compareTo(candidateSeat1.getQuotient());
		}
		if (candidateSeat2.getDividend() != candidateSeat1.getDividend()) {
			return candidateSeat2.getDividend() - candidateSeat1.getDividend();
		}
		return randomCompare(candidateSeat1, candidateSeat2);
	}

	private int randomCompare(CandidateSeat candidateSeat1, CandidateSeat candidateSeat2) {
		return randomTicket(candidateSeat1) - randomTicket(candidateSeat2);
	}

	private int randomTicket(CandidateSeat candidateSeat) {
		if (!randomTickets.containsKey(candidateSeat)) {
			randomTickets.put(candidateSeat, randomizer.nextInt());
		}
		return randomTickets.get(candidateSeat);
	}
}
