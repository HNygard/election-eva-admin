package no.valg.eva.admin.settlement.domain.event.factory;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.ModifiedSainteLague;
import no.valg.eva.admin.settlement.domain.consumer.CandidateRankConsumer;
import no.valg.eva.admin.settlement.domain.event.CandidateSeatEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateSeatEventListener;
import no.valg.eva.admin.settlement.domain.model.CandidateRank;

public class CandidateSeatEventsFromCandidateRanks extends EventFactory<CandidateSeatEventListener>implements CandidateRankConsumer {
	private final ModifiedSainteLague modifiedSainteLague;

	public CandidateSeatEventsFromCandidateRanks(BigDecimal firstDivisor) {
		this.modifiedSainteLague = new ModifiedSainteLague(firstDivisor);
	}

	@Override
	public void consume(CandidateRank candidateRank) {
		fireEvent(candidateRank.getAffiliation(), candidateRank.getCandidate(), modifiedSainteLague.saintLagueDivisor(candidateRank.getRankNumber()));
	}

	private void fireEvent(Affiliation affiliation, Candidate candidate, BigDecimal divisor) {
		CandidateSeatEvent candidateSeatEvent = new CandidateSeatEvent(affiliation, candidate, divisor);
		for (CandidateSeatEventListener eventListener : eventListeners) {
			eventListener.candidateSeatDelta(candidateSeatEvent);
		}
	}
}
