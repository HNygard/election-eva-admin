package no.valg.eva.admin.common.configuration.model.central;

import java.io.Serializable;

import no.valg.eva.admin.common.configuration.model.election.Election;

public class ContestListProposalData implements Serializable {
	private static final double WRITEIN_FACTOR = 4.0;
	private static final int MIN_WRITEIN = 5;

	private final Election election;
	private Integer minProposersNewParty;
	private Integer minProposersOldParty;
	private Integer minCandidates;
	private Integer maxCandidates;
	private Integer numberOfPositions;
	private Integer maxWriteIn;
	private Integer maxRenumber;

	public ContestListProposalData(Election election) {
		this.election = election;
	}

	public ContestListProposalData(Election election, Integer minProposersNewParty, Integer minProposersOldParty, Integer minCandidates, Integer maxCandidates,
			Integer numberOfPositions, Integer maxWriteIn, Integer maxRenumber) {
		this.election = election;
		this.minProposersNewParty = minProposersNewParty;
		this.minProposersOldParty = minProposersOldParty;
		this.minCandidates = minCandidates;
		this.maxCandidates = maxCandidates;
		this.numberOfPositions = numberOfPositions;
		this.maxWriteIn = maxWriteIn;
		this.maxRenumber = maxRenumber;
	}

	public boolean isMinCandidatesInput() {
		return election.getMinCandidates() == null && election.getMinCandidatesAddition() == null;
	}

	public boolean isMaxCandidatesInput() {
		return election.getMaxCandidates() == null && election.getMaxCandidatesAddition() == null;
	}

	public boolean isMaxWriteInInput() {
		return election.isWriteinLocalOverride();
	}

	public boolean isHasNoDependentInputs() {
		return isMinCandidatesInput() && isMaxCandidatesInput() && isMaxWriteInInput();
	}

	public boolean isValid() {
		boolean valid = numberOfPositions != null;
		valid = valid && minProposersNewParty != null && minProposersOldParty != null;
		valid = valid && minCandidates != null && maxCandidates != null;
		if (election.isWritein()) {
			valid = valid && maxWriteIn != null;
		}
		if (election.isRenumberLimit()) {
			valid = valid && maxRenumber != null;
		}
		return valid;
	}

	public Integer getMinProposersNewParty() {
		return minProposersNewParty;
	}

	public void setMinProposersNewParty(Integer minProposersNewParty) {
		this.minProposersNewParty = minProposersNewParty;
		recalculate();
	}

	public Integer getMinProposersOldParty() {
		return minProposersOldParty;
	}

	public void setMinProposersOldParty(Integer minProposersOldParty) {
		this.minProposersOldParty = minProposersOldParty;
		recalculate();
	}

	public Integer getMinCandidates() {
		return minCandidates;
	}

	public void setMinCandidates(Integer minCandidates) {
		this.minCandidates = minCandidates;
		recalculate();
	}

	public Integer getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(Integer maxCandidates) {
		this.maxCandidates = maxCandidates;
		recalculate();
	}

	public Integer getNumberOfPositions() {
		return numberOfPositions;
	}

	public void setNumberOfPositions(Integer numberOfPositions) {
		this.numberOfPositions = numberOfPositions;
		recalculate();
	}

	public Integer getMaxWriteIn() {
		return maxWriteIn;
	}

	public void setMaxWriteIn(Integer maxWriteIn) {
		this.maxWriteIn = maxWriteIn;
		recalculate();
	}

	public Integer getMaxRenumber() {
		return maxRenumber;
	}

	public void setMaxRenumber(Integer maxRenumber) {
		this.maxRenumber = maxRenumber;
		recalculate();
	}

	public Election getElection() {
		return election;
	}

	public void recalculate() {
		if (getNumberOfPositions() != null) {
			// Calculate min candidates?
			if (election.getMinCandidatesAddition() != null) {
				minCandidates = getNumberOfPositions() + election.getMinCandidatesAddition();
			} else if (election.getMinCandidates() != null) {
				minCandidates = election.getMinCandidates();
			}
			// Calculate max candidates?
			if (election.getMaxCandidatesAddition() != null) {
				maxCandidates = getNumberOfPositions() + election.getMaxCandidatesAddition();
			} else if (election.getMaxCandidates() != null) {
				maxCandidates = election.getMaxCandidates();
			}
			// Calculate writeins?
			if (election.isWritein() && !election.isWriteinLocalOverride()) {
				maxWriteIn = (int) Math.floor(getNumberOfPositions() / WRITEIN_FACTOR);
				if (maxWriteIn < MIN_WRITEIN) {
					maxWriteIn = MIN_WRITEIN;
				}
			}
		}
	}
}
