package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallots;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;

/**
 * Operations for storing and fetching ModifiedBallot.
 */
public interface ModifiedBallotService extends Serializable {

	/**
	 * Updates modifiedBallot
	 */
	void update(UserData userData, ModifiedBallot modifiedBallot);

	/**
	 * Loads modified ballot
	 */
	ModifiedBallot load(UserData userData, ModifiedBallot modifiedBallot);

	/**
	 * @param ballotCountRef
	 *            identifies ballot count
	 * @return modifiedBallots with modifiedBallot view object instances for ballot
	 */
	ModifiedBallots modifiedBallotsFor(UserData userData, BallotCountRef ballotCountRef, ModifiedBallotBatchProcess processFilter);
}
