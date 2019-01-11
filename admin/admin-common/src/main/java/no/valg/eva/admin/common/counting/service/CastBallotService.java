package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.ApprovedBallot;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.RejectedBallot;

public interface CastBallotService extends Serializable {
	/**
	 * @return rejected ballots for an approved final count
	 */
	List<RejectedBallot> rejectedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef);

	/**
	 * @return approved ballots for an approved final count
	 */
	List<ApprovedBallot> approvedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef);

	/**
	 * Process proposed rejected ballots and update approved final count.
	 */
	void processRejectedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef, List<RejectedBallot> rejectedBallots);
}
