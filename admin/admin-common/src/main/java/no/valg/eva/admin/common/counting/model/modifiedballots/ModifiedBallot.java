package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.counting.model.BatchId;

/**
 * Annotates a given Ballot instance with personal votes (personstemmer), write ins (slengere), strike outs (strykninger) and renumberings.
 */
public class ModifiedBallot implements Serializable {
	
	private BatchId batchId;
	private int serialNumber;
	private String affiliation;
	private boolean done;

	private BallotId ballotId;
	private Set<Candidate> writeIns = new LinkedHashSet<>();
    
    /* candidates applicable for personal vote, renumbering or strike out */
    private Set<Candidate> personVotes = new TreeSet<>();
	
    private CastBallotRef castBallotRef;

	/**
	 * @param serialNumber position in batch
	 * @param affiliation party id
	 * @param ballotId ref to ballot this annotates
	 */
	public ModifiedBallot(BatchId batchId, int serialNumber, String affiliation, BallotId ballotId, boolean done) {
		this.batchId = batchId;
		this.serialNumber = serialNumber;
		this.affiliation = affiliation;
		this.ballotId = ballotId;
		this.done = done;
	}

	public ModifiedBallot(int serialNumber, String partyName) {
		this.serialNumber = serialNumber;
		this.affiliation = partyName;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void validate(Ballot ballot) {
		if (!ballot.getBallotId().equals(ballotId)) {
			throw new ValidateException("@modified_ballot.validation.illegal_ballot_id");
		}
		if (!ballot.personalVoteCandidates().containsAll(personVotes)) {
			throw new ValidateException("@modified_ballot.validation.candidate_not_on_ballot");
		}
		if (!ballot.getCandidatesForWriteIn().containsAll(writeIns)) {
			throw new ValidateException("@modified_ballot.validation.unknown_write_in_candidate");
		}
		if (writeIns.size() > ballot.getMaxWriteIns()) {
			throw new ValidateException("@modified_ballot.validation.too_many_write_ins", asString(writeIns.size()), asString(ballot.getMaxWriteIns()));
		}
		validateRenumberings(ballot);
	}

	private void validateRenumberings(Ballot ballot) {
		int maxCandidateNo = personVotes.size();
		Map<Integer, Candidate> renumberCandidateMap = new HashMap<>();
		for (Candidate renumberedCandidate : renumberings()) {
			if (renumberedCandidate.getRenumberPosition() > maxCandidateNo) {
				throw new ValidateException("@modified_ballot.validation.renumber_unknown_candidate", renumberedCandidate.nameAndNumber());
			}
			if (ballot.getMaxRenumber() != null && renumberedCandidate.getRenumberPosition() > ballot.getMaxRenumber()) {
				throw new ValidateException("@modified_ballot.validation.max_renumber_exceeded", asString(ballot.getMaxRenumber()));
			}
			if (renumberedCandidate.hasPersonalVote()) {
				throw new ValidateException("@modified_ballot.validation.renumber_and_person_vote");
			}
			if (renumberedCandidate.isStrikedOut()) {
				throw new ValidateException("@modified_ballot.validation.renumber_and_strike_out", renumberedCandidate.nameAndNumber());
			}
			if (ballot.getMaxRenumber() != null && renumberedCandidate.getDisplayOrder() > ballot.getMaxRenumber()) {
				throw new ValidateException("@modified_ballot.validation.max_renumber_exceeded", asString(ballot.getMaxRenumber()));
			}
			if (renumberCandidateMap.containsKey(renumberedCandidate.getRenumberPosition())) {
				throw new ValidateException("@modified_ballot.validation.renumber_to_same_candidate", renumberedCandidate.nameAndNumber(),
						renumberCandidateMap.get(renumberedCandidate.getRenumberPosition()).nameAndNumber());
			} else {
				renumberCandidateMap.put(renumberedCandidate.getRenumberPosition(), renumberedCandidate);
			}
		}
	}

	private String asString(int number) {
		return "" + number;
	}

	public void addPersonVotesFor(Candidate candidate) {
        personVotes.add(candidate);
	}

	public void addWriteInFor(Candidate candidate) {
		writeIns.add(candidate);
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public Set<Candidate> getWriteIns() {
		return writeIns;
	}

	public void setWriteIns(Set<Candidate> writeIns) {
		this.writeIns = writeIns;
	}

	public void addCandidatesForPersonVotes(Set<Candidate> candidates) {
		this.personVotes.addAll(candidates);
	}

	public BallotId getBallotId() {
		return ballotId;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isModified() {
		return personVotes.size() > 0 || writeIns.size() > 0;
	}

	public Long getBinaryDataPk() {
		return castBallotRef.getBinaryDataPk();
	}

	/* Brukes blant annet i registerModifiedBallotTemplate.xhtml */
	@SuppressWarnings("unused")
	public boolean hasImage() {
		return castBallotRef != null && castBallotRef.getBinaryDataPk() != null;
	}

	public String getCastBallotId() {
		return castBallotRef.getCastBallotId();
	}

    public void setCastBallotRef(CastBallotRef castBallotRef) {
        this.castBallotRef = castBallotRef;
    }

	/* The order of the candidates is important, thus returning sorted set */
	public SortedSet<Candidate> renumberings() {
        TreeSet<Candidate> renumberings = new TreeSet<>();
        for (Candidate candidate : personVotes) {
            if (candidate.isRenumbered()) {
                renumberings.add(candidate);
            }
        }
        return renumberings;
    }

    public Set<Candidate> personalVotes() {
        Set<Candidate> personalVotes = new TreeSet<>();
        for (Candidate candidate : personVotes) {
            if (candidate.hasPersonalVote()) {
                personalVotes.add(candidate);
            }
        }
        return personalVotes;
    }

    public Set<Candidate> strikeOuts() {
        Set<Candidate> strikeOuts = new TreeSet<>();
        for (Candidate candidate : personVotes) {
            if (candidate.isStrikedOut()) {
                strikeOuts.add(candidate);
            }
        }
        return strikeOuts;
    }

    public Set<Candidate> getPersonVotes() {
        return personVotes;
    }
}
