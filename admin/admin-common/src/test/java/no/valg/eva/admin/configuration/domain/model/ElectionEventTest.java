package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ElectionEventTest {

	@Test
	public void elections_returnsElectionsInHierarchy() throws Exception {
		ElectionEvent electionEvent = makeElectionEvent();
		assertThat(electionEvent.elections()).hasSize(2);
	}

	private ElectionEvent makeElectionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.getElectionGroups().add(makeElectionGroup());
		return electionEvent;
	}

	private ElectionGroup makeElectionGroup() {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.getElections().add(makeElection(1L));
		electionGroup.getElections().add(makeElection(2L));
		return electionGroup;
	}

	private Election makeElection(Long pk) {
		Election election = new Election();
		election.setPk(pk);
		return election;
	}
}
