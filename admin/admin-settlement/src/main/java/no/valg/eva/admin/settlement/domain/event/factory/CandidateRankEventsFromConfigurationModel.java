package no.valg.eva.admin.settlement.domain.event.factory;

import static java.math.BigDecimal.ZERO;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.settlement.domain.event.CandidateRankEvent;
import no.valg.eva.admin.settlement.domain.event.listener.CandidateRankEventListener;

public class CandidateRankEventsFromConfigurationModel extends EventFactory<CandidateRankEventListener>implements ConfigurationVisitor {
	@Override
	public boolean include(Contest contest) {
		// include all
		return true;
	}

	@Override
	public void visit(Contest contest) {
		// do nothing
	}

	@Override
	public boolean include(Ballot ballot) {
		// include all
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
		// include all
		return true;
	}

	@Override
	public void visit(Candidate candidate) {
		CandidateRankEvent event = new CandidateRankEvent(candidate, candidate.getAffiliation(), ZERO, null);
		for (CandidateRankEventListener eventListener : eventListeners) {
			eventListener.candidateRankDelta(event);
		}
	}
}
