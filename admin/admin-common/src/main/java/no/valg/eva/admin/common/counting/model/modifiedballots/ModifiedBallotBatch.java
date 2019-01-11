package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;
import java.util.List;

import no.valg.eva.admin.common.counting.model.BatchId;

/**
 * Contains a batch of modified ballots (annotations) for a given ballot.
 */
public class ModifiedBallotBatch extends ModifiedBallots implements Serializable {
	private BatchId batchId;

	public ModifiedBallotBatch(BatchId batchId, List<ModifiedBallot> modifiedBallots, Ballot ballot) {
		super(modifiedBallots, ballot);
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	/**
	 * Intended to be used to figure out if handling of all ballots in a batch has been completed (so that the "Done" button can be enabled)
	 */
	public boolean isDone() {
		for (ModifiedBallot modifiedBallot : getModifiedBallots()) {
			if (!modifiedBallot.isDone() && !modifiedBallot.isModified()) {
				return false;
			}
		}

		return true;
	}
}
