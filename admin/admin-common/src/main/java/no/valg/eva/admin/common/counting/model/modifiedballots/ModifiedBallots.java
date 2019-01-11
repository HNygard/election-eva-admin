package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Contains a list of modified ballots (annotations) for a given ballot.
 */
public class ModifiedBallots implements Serializable {
	private static final boolean NON_BATCH_IS_ALWAYS_DONE = true;
	private List<ModifiedBallot> modifiedBallots;
	private Ballot ballot;

	public ModifiedBallots(List<ModifiedBallot> modifiedBallots, Ballot ballot) {
		Collections.sort(modifiedBallots, new Comparator<ModifiedBallot>() {
			@Override
			public int compare(ModifiedBallot o1, ModifiedBallot o2) {
				return o1.getSerialNumber() - o2.getSerialNumber();
			}
		});
		this.modifiedBallots = modifiedBallots;
		this.ballot = ballot;
	}

	public List<ModifiedBallot> getModifiedBallots() {
		return modifiedBallots;
	}

	public Collection<Candidate> getWriteInCandidates() {
		return ballot.getCandidatesForWriteIn();
	}

	public void addPersonalVoteCandidateFor(Candidate candidate) {
		ballot.addForPersonalVote(candidate);
	}

	public void addWriteInCandidateFor(Candidate candidate) {
		ballot.getCandidatesForWriteIn().add(candidate);
	}

	public Ballot getBallot() {
		return ballot;
	}

	public boolean isDone() {
		return NON_BATCH_IS_ALWAYS_DONE;
	}
}
