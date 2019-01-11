package no.valg.eva.admin.settlement.domain.event.factory;

import java.math.BigDecimal;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;

public class CandidateRankEventsFromCandidates extends EventFactory<CandidateRankEventListener>implements ConfigurationVisitor {
	private BigDecimal candidateVotes;

	public void setCandidateVotes(BigDecimal candidateVotes) {
		this.candidateVotes = candidateVotes;
	}

	@Override
	public boolean include(Contest contest) {
		return false;
	}

	@Override
	public void visit(Contest contest) {
		// do nothing
	}

	@Override
	public boolean include(Ballot ballot) {
		return true;
	}

	@Override
	public void visit(Ballot ballot) {
		// do nothing
	}

	@Override
	public boolean include(Affiliation affiliation) {
		return affiliation.isApproved();
	}

	@Override
	public void visit(Affiliation affiliation) {
		// do nothing
	}

	@Override
	public boolean include(Candidate candidate) {
		return true;
	}

	@Override
	public void visit(Candidate candidate) {
		Affiliation affiliation = candidate.getAffiliation();
		for (int rankNumber = candidate.getDisplayOrder(); rankNumber < affiliation.getCandidates().size() + 1; rankNumber++) {
			fireEvent(candidate, affiliation, candidateVotes, rankNumber);
		}
	}

	private void fireEvent(Candidate candidate, Affiliation affiliation, BigDecimal candidateVotes, int rankNumber) {
		for (CandidateRankEventListener eventListener : eventListeners) {
			eventListener.candidateRankDelta(new CandidateRankEvent(candidate, affiliation, candidateVotes, rankNumber));
		}
	}
}
