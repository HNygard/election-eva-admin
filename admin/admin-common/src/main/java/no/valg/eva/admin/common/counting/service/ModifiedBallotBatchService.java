package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotBatch;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;

/**
 * Operations related to batch handling of modified ballots
 */
public interface ModifiedBallotBatchService extends Serializable {

	/**
	 * @return The identifier of the newly created modified ballot batch
	 */
	ModifiedBallotBatch createModifiedBallotBatch(UserData userData, BallotCount ballotCount, int noOfModifiedBallotsInBatch,
			ModifiedBallotBatchProcess process);

	ModifiedBallotBatch findActiveBatchByBatchId(UserData userData, BatchId modifiedBallotBatchId);

	boolean hasModifiedBallotBatchForBallotCountPks(UserData userData, List<BallotCountRef> ballotCountPks);

	List<ModifiedBallotsStatus> buildModifiedBallotStatuses(UserData userData, FinalCount finalCount, ModifiedBallotBatchProcess process);
}
