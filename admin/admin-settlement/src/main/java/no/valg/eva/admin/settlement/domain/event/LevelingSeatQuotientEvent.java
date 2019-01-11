package no.valg.eva.admin.settlement.domain.event;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;

public class LevelingSeatQuotientEvent implements Event {
	private final Contest contest;
	private final Party party;
	private final int partyVotes;
	private final int partySeats;

	public LevelingSeatQuotientEvent(Contest contest, Party party, int partyVotes, int partySeats) {
		this.contest = contest;
		this.party = party;
		this.partyVotes = partyVotes;
		this.partySeats = partySeats;
	}

	public Contest getContest() {
		return contest;
	}

	public Party getParty() {
		return party;
	}

	public int getPartyVotes() {
		return partyVotes;
	}

	public int getPartySeats() {
		return partySeats;
	}
}
