package no.valg.eva.admin.settlement.domain.event.factory;

import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.event.ElectionVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.ElectionVoteCountEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

public class ElectionVoteCountEventsFromSettlementModel extends EventFactory<ElectionVoteCountEventListener>implements SettlementVisitor {
	@Override
	public void visit(Settlement settlement) {
		// do nothing
	}

	@Override
	public void visit(AffiliationVoteCount affiliationVoteCount) {
		Party party = affiliationVoteCount.getAffiliation().getParty();
		if (party.isBlank()) {
			return;
		}
		int earlyVotingBallots = affiliationVoteCount.getEarlyVotingBallots();
		int earlyVotingModifiedBallots = affiliationVoteCount.getEarlyVotingModifiedBallots();
		int electionDayBallots = affiliationVoteCount.getElectionDayBallots();
		int electionDayModifiedBallots = affiliationVoteCount.getElectionDayModifiedBallots();
		int baselineVotes = affiliationVoteCount.getBaselineVotes();
		int addedVotes = affiliationVoteCount.getAddedVotes();
		int subtractedVotes = affiliationVoteCount.getSubtractedVotes();
		fireBallotsEvent(party, earlyVotingBallots, earlyVotingModifiedBallots, electionDayBallots, electionDayModifiedBallots, baselineVotes, addedVotes,
				subtractedVotes);
	}

	@Override
	public void visit(CandidateSeat candidateSeat) {
		if (candidateSeat.isElected()) {
			Party party = candidateSeat.getAffiliation().getParty();
			fireContestSeatsEvent(party);
		}
	}

	private void fireBallotsEvent(Party party, int earlyVotingBallots, int earlyVotingModifiedBallots, int electionDayBallots,
			int electionDayModifiedBallots, int baselineVotes, int addedVotes, int subtractedVotes) {
		fireEvent(new ElectionVoteCountEvent(party, earlyVotingBallots, earlyVotingModifiedBallots, electionDayBallots,
				electionDayModifiedBallots, baselineVotes, addedVotes, subtractedVotes, 0));
	}

	private void fireContestSeatsEvent(Party party) {
		fireEvent(new ElectionVoteCountEvent(party, 0, 0, 0, 0, 0, 0, 0, 1));
	}

	private void fireEvent(ElectionVoteCountEvent event) {
		for (ElectionVoteCountEventListener eventListener : eventListeners) {
			eventListener.electionVoteCountDelta(event);
		}
	}
}
