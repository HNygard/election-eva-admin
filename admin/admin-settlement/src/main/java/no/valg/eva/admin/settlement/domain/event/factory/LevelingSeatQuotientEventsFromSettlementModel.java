package no.valg.eva.admin.settlement.domain.event.factory;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.event.LevelingSeatQuotientEvent;
import no.valg.eva.admin.settlement.domain.event.listener.LevelingSeatQuotientEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

public class LevelingSeatQuotientEventsFromSettlementModel extends EventFactory<LevelingSeatQuotientEventListener> implements SettlementVisitor {
	@Override
	public void visit(Settlement settlement) {
		// do nothing
	}

	@Override
	public void visit(AffiliationVoteCount affiliationVoteCount) {
		Party party = affiliationVoteCount.getParty();
		if (party.isBlank()) {
			return;
		}
		LevelingSeatQuotientEvent event = new LevelingSeatQuotientEvent(affiliationVoteCount.getContest(), party, affiliationVoteCount.getVotes(), 0);
		fireEvents(listener -> listener.levelingSeatQuotientDelta(event));
	}

	@Override
	public void visit(CandidateSeat candidateSeat) {
		if (!candidateSeat.isElected()) {
			return;
		}
		LevelingSeatQuotientEvent event = new LevelingSeatQuotientEvent(candidateSeat.getContest(), candidateSeat.getParty(), 0, 1);
		fireEvents(listener -> listener.levelingSeatQuotientDelta(event));
	}
}
