package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.ApprovedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.model.CastBallotBinaryData;
import no.valg.eva.admin.common.counting.model.RejectedCastBallotRefForApprovedFinalCount;

public interface CastBallotBinaryDataService extends Serializable {
	CastBallotBinaryData rejectedCastBallotBinaryData(UserData userData, RejectedCastBallotRefForApprovedFinalCount ref);

	CastBallotBinaryData approvedCastBallotBinaryData(UserData userData, ApprovedCastBallotRefForApprovedFinalCount ref);
}
