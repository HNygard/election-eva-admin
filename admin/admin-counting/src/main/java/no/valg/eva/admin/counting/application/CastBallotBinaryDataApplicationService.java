package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forkastelser_Skannet;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.ApprovedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.model.CastBallotBinaryData;
import no.valg.eva.admin.common.counting.model.CastBallotId;
import no.valg.eva.admin.common.counting.model.RejectedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.service.CastBallotBinaryDataService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.VoteCountService;

@Stateless(name = "CastBallotBinaryDataService")
@Remote(CastBallotBinaryDataService.class)
public class CastBallotBinaryDataApplicationService implements CastBallotBinaryDataService {
	@Inject
	private VoteCountService voteCountService;

	@Override
	@Security(accesses = Opptelling_Forkastelser_Skannet, type = READ)
	public CastBallotBinaryData rejectedCastBallotBinaryData(UserData userData, RejectedCastBallotRefForApprovedFinalCount ref) {
		VoteCount approvedFinalVoteCount = voteCountService
				.findApprovedFinalVoteCount(ref.reportingUnitTypeId(), ref.countContext(), ref.countingAreaPath(), userData.getOperatorAreaPath());
		BallotCount rejectedBallotCount = approvedFinalVoteCount.getRejectedBallotCount(ref.ballotRejectionId());
		return castBallotBinaryData(binaryData(rejectedBallotCount, ref.castBallotId()));
	}

	@Override
	@Security(accesses = Opptelling_Forkastelser_Skannet, type = READ)
	public CastBallotBinaryData approvedCastBallotBinaryData(UserData userData, ApprovedCastBallotRefForApprovedFinalCount ref) {
		VoteCount approvedFinalVoteCount = voteCountService
				.findApprovedFinalVoteCount(ref.reportingUnitTypeId(), ref.countContext(), ref.countingAreaPath(), userData.getOperatorAreaPath());
		BallotCount ballotCount = approvedFinalVoteCount.getBallotCount(ref.ballotId());
		return castBallotBinaryData(binaryData(ballotCount, ref.castBallotId()));
	}

	private BinaryData binaryData(BallotCount ballotCount, CastBallotId castBallotId) {
		return ballotCount.getCastBallot(castBallotId).getBinaryData();
	}

	private CastBallotBinaryData castBallotBinaryData(BinaryData binaryData) {
		String fileName = binaryData.getFileName();
		String mimeType = binaryData.getMimeType();
		byte[] binaryDataBytes = binaryData.getBinaryData();
		return new CastBallotBinaryData(fileName, mimeType, binaryDataBytes);
	}
}
