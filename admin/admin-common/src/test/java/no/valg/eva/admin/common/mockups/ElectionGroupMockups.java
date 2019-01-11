package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.ELECTION_GROUP_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;

public final class ElectionGroupMockups {

	public static final long ELECTION_GROUP_PK = ELECTION_GROUP_PK_SERIES + 1;

	private ElectionGroupMockups() {
		// no instances allowed
	}

	public static ElectionGroup electionGroup(final ElectionEvent electionEvent) {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setPk(ELECTION_GROUP_PK);
		electionGroup.setId("11");
		electionGroup.setElectionEvent(electionEvent);
		return electionGroup;
	}
}
