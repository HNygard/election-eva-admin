package no.valg.eva.admin.frontend.listproposal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * Class to hold data for a list proposal.
 */
public class ListProposalData implements Serializable {

	private Contest currentContest;
	private final Set<MvArea> currentMvArea = new HashSet<>();
	private Affiliation currentAffiliation;
	private boolean contestLocked;
	private ElectionPath pathToCurrentContest;

	public Contest getCurrentContest() {
		return currentContest;
	}

	public void setCurrentContest(final Contest contest) {
		currentContest = contest;
	}

	public Affiliation getCurrentAffiliation() {
		return currentAffiliation;
	}

	public Long getBallotPk() {
		return currentAffiliation.getBallot().getPk();
	}

	public void setCurrentAffiliation(final Affiliation affiliation) {
		currentAffiliation = affiliation;
	}

	public boolean isContestLocked() {
		return contestLocked;
	}

	public void setContestLocked(final boolean contestLocked) {
		this.contestLocked = contestLocked;
	}

	public String getElectionId() {
		return getCurrentContest().getElection().getElectionGroup().getElectionEvent().getId();
	}

	public Set<MvArea> getCurrentMvAreas() {
		return currentMvArea;
	}

	public void setPathToCurrentContest(ElectionPath pathToCurrentContest) {
		this.pathToCurrentContest = pathToCurrentContest;
	}

	public ElectionPath getPathToCurrentContest() {
		return pathToCurrentContest;
	}
}
