package no.evote.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.Proposer;

public class ListProposalValidationData implements Serializable {

	private List<Candidate> candidateList;
	private List<Proposer> proposerList;
	private Affiliation affiliation;
	private boolean isApproved;
	private boolean sufficientNumberOfCandidates;

	public ListProposalValidationData(final List<Candidate> candidateList, final List<Proposer> proposerList, final Affiliation affiliation) {
		this.candidateList = candidateList;
		this.proposerList = proposerList;
		this.affiliation = affiliation;
		this.isApproved = true;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public List<Candidate> getCandidateList() {
		return candidateList;
	}

	public List<Proposer> getProposerList() {
		return proposerList;
	}

	public Collection<Proposer> getProposerListMedKunUtfylteUnderskrifter() {
		return proposerList.stream().filter(Proposer::isUtfyltUnderskrift).collect(Collectors.toList());
	}

	public void setCandidateList(final List<Candidate> candidateList) {
		this.candidateList = candidateList;
	}

	public void setProposerList(final List<Proposer> proposerList) {
		this.proposerList = proposerList;
	}

	public boolean isApproved() {
		return isApproved;
	}

	public void setApproved(final boolean isApproved) {
		this.isApproved = isApproved;
	}

	public boolean isSufficientNumberOfCandidates() {
		return sufficientNumberOfCandidates;
	}

	public void setSufficientNumberOfCandidates(boolean sufficientNumberOfCandidates) {
		this.sufficientNumberOfCandidates = sufficientNumberOfCandidates;
	}

	public int getMinProposers() {
		Contest contest = getAffiliation().getBallot().getContest();
		Party party = getAffiliation().getParty();
		return contest.minNumberOfProposersFor(party);
	}

	public boolean isPartyReadyForApproval() {
		return !hasInvalidCandidates() && isSufficientNumberOfCandidates()
				&& getProposerListMedKunUtfylteUnderskrifter().size() >= getMinProposers();
	}

	private boolean hasInvalidCandidates() {
		for (Candidate candidate : candidateList) {
			if (candidate.isInvalid()) {
				return true;
			}
		}
		return false;
	}
}
