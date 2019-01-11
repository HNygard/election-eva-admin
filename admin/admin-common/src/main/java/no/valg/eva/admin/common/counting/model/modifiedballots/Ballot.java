package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;

/**
 * Model for the statical content of the mofided ballot process. ModifiedBallot will be an annotation on a Ballot.
 */
public class Ballot implements Serializable {

	private final BallotId ballotId;
	private final ModifiedBallotConfiguration modifiedBallotConfiguration;

	private Set<Candidate> candidatesForPersonalVotes = new TreeSet<>();
	private Set<Candidate> candidatesForWriteIn = new LinkedHashSet<>();

	public Ballot(BallotId ballotId, ModifiedBallotConfiguration modifiedBallotConfiguration) {
		this.ballotId = ballotId;
		this.modifiedBallotConfiguration = modifiedBallotConfiguration;
	}

	/**
	 * @return unmodified candidates for personal votes, renumberings, or strikeouts as listed on an unmodified ballot.
	 */
	public Set<Candidate> personalVoteCandidates() {
		Set<Candidate> candidates = new TreeSet<>();
		for (Candidate candidate : candidatesForPersonalVotes) {
			candidates.add(new Candidate(candidate.getName(), candidate.getCandidateRef(), candidate.getPartyName(), candidate.getDisplayOrder()));
		}
		return candidates;
	}

	public void addForPersonalVote(Candidate candidate) {
		candidatesForPersonalVotes.add(candidate);
	}

	public Set<Candidate> getCandidatesForWriteIn() {
		return candidatesForWriteIn;
	}

	public BallotId getBallotId() {
		return ballotId;
	}

	public int getMaxWriteIns() {
		return modifiedBallotConfiguration.getMaxWriteIn() != null ? modifiedBallotConfiguration.getMaxWriteIn() : 0;
	}

	public boolean usePersonalVotes() {
		return modifiedBallotConfiguration.isPersonal();
	}

	public boolean useWriteIns() {
		return modifiedBallotConfiguration.isWritein();
	}

	public boolean useRenumbering() {
		return modifiedBallotConfiguration.isRenumber();
	}

	public boolean useStrikeOuts() {
		return modifiedBallotConfiguration.isStrikeout();
	}
    
    public Integer getMaxRenumber() {
        return modifiedBallotConfiguration.getMaxRenumber();
    }
}
