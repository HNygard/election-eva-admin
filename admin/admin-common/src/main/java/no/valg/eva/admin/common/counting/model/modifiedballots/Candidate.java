package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;

/**
 * Candidate on modified ballot.  May be given personal vote, or may be renumbered or striked out.
 */
public class Candidate implements Serializable, Comparable<Candidate> {

	private String name;
	private String partyName;
	private int displayOrder;
	private final CandidateRef candidateRef;
    private Integer renumberPosition;
    private boolean strikedOut;
    private boolean personalVote;

	public Candidate(String name, CandidateRef candidateRef, String partyName, int displayOrder) {
		this(candidateRef);
		this.name = name;
		this.partyName = partyName;
		this.displayOrder = displayOrder;
	}

	public Candidate(CandidateRef candidateRef) {
		this.candidateRef = candidateRef;
	}
	
	public String getName() {
		return name;
	}

	public String getPartyName() {
		return partyName;
	}

	public CandidateRef getCandidateRef() {
		return candidateRef;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	@Override
	public String toString() {
		return "Candidate{"
			+ "name='" + name + '\''
			+ ", partyName='" + partyName + '\''
			+ ", displayOrder=" + displayOrder
			+ ", candidateRef=" + candidateRef
			+ ", renumberPosition=" + renumberPosition
			+ ", strikedOut=" + strikedOut
			+ ", personalVote=" + personalVote
			+ '}';
	}

    @Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Candidate candidate = (Candidate) o;

		if (candidateRef != null ? !candidateRef.equals(candidate.candidateRef) : candidate.candidateRef != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return candidateRef != null ? candidateRef.hashCode() : 0;
	}

	/** NB! compares on display order.  This is important when adding candidates to linked sets. */
	@Override
	public int compareTo(Candidate other) {
		return this.getDisplayOrder() - other.getDisplayOrder();
	}

    public Integer getRenumberPosition() {
        return renumberPosition;
    }

    public void setRenumberPosition(Integer renumbering) {
        this.renumberPosition = renumbering;
    }

    public void setStrikedOut(boolean strikedOut) {
        this.strikedOut = strikedOut;
    }

    public boolean isPersonalVote() {
        return personalVote;
    }

    public void setPersonalVote(boolean personVote) {
        this.personalVote = personVote;
    }

    public boolean hasPersonalVote() {
        return personalVote;
    }

    public boolean isRenumbered() {
        return renumberPosition != null && renumberPosition > 0;
    }

    public boolean isStrikedOut() {
        return strikedOut;
    }

	String nameAndNumber() {
		return getDisplayOrder()
				+ ". " + getName();
	}
}
