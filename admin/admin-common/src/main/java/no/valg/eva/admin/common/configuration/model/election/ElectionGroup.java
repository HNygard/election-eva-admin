package no.valg.eva.admin.common.configuration.model.election;

import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;

public class ElectionGroup extends VersionedObject {

	@Getter private final ElectionPath parentElectionPath;
	@Getter @Setter private String electionEventName;
	@Getter @Setter private ElectionRef electionGroupRef;
	@Getter @Setter private String id;
	@Getter @Setter private String name;
	@Getter @Setter private boolean electronicMarkoffs;
	@Getter @Setter private boolean advanceVoteInBallotBox;
	@Getter @Setter private boolean scanningPermitted;
	@Getter @Setter private boolean validateRoleAndListProposal;
	@Getter @Setter private boolean validatePollingPlaceElectoralBoardAndListProposal;

	public ElectionGroup(ElectionPath parentElectionPath) {
		this(parentElectionPath, 0);
	}

	public ElectionGroup(ElectionPath parentElectionPath, int version) {
		super(version);
		this.parentElectionPath = parentElectionPath;
	}

	public ElectionPath getElectionGroupPath() {
		return parentElectionPath.add(id);
	}
}
