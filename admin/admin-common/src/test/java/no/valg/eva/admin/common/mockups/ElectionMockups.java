package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.ElectionGroupMockups.electionGroup;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.ELECTION_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;

public final class ElectionMockups {

	public static final long ELECTION_PK = ELECTION_PK_SERIES + 1;

	private ElectionMockups() {
		// no instances allowed
	}

	public static Election election(final ElectionGroup electionGroup) {
		Election election = new Election();
		election.setPk(ELECTION_PK);
		election.setId("11");
		election.setElectionGroup(electionGroup);
		election.setSingleArea(true);
		return election;
	}

	public static Election defaultElection(final ElectionEvent electionEvent) {
		return election(electionGroup(electionEvent));
	}
}
