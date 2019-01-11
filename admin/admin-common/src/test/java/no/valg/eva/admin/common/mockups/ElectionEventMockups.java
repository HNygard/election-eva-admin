package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.ELECTION_EVENT_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public final class ElectionEventMockups {

	public static final long ELECTION_EVENT_PK = ELECTION_EVENT_PK_SERIES + 1;

	private ElectionEventMockups() {
		// no instances allowed
	}

	public static ElectionEvent electionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(ELECTION_EVENT_PK);
		electionEvent.setId("111111");
		return electionEvent;
	}
}
