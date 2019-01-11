package no.valg.eva.admin.counting.builder;

import static no.valg.eva.admin.util.LangUtil.zeroIfNull;

import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

public class VoteCountBuilder {
	private static final boolean MODIFIED_BALLOTS_PROCESSED_FALSE = false;
	private static final boolean REJECTED_BALLOTS_PROCESSED_FALSE = false;

	private final VoteCount voteCount;

	public VoteCountBuilder() {
		voteCount = new VoteCount();

		voteCount.setModifiedBallotsProcessed(MODIFIED_BALLOTS_PROCESSED_FALSE);
		voteCount.setRejectedBallotsProcessed(REJECTED_BALLOTS_PROCESSED_FALSE);
	}

	public VoteCount build() {
		return voteCount;
	}

	public VoteCountBuilder applyArea(MvArea mvArea) {
		voteCount.setMvArea(mvArea);
		// setter også polling district for å være bakoverkompatibel med f.eks. funksjon for å slette telledata
		// mvArea er pt. alltid polling district
		voteCount.setPollingDistrict(mvArea.getPollingDistrict());
		return this;
	}

	public VoteCountBuilder applyVoteCountCategory(VoteCountCategory voteCountCategory) {
		voteCount.setVoteCountCategory(voteCountCategory);
		return this;
	}

	public VoteCountBuilder applyVoteCountStatus(VoteCountStatus voteCountStatus) {
		voteCount.setVoteCountStatus(voteCountStatus);
		return this;
	}

	public VoteCountBuilder applyCountQualifier(CountQualifier countQualifier) {
		voteCount.setCountQualifier(countQualifier);
		return this;
	}

	public VoteCountBuilder applyProtocolCount(ProtocolCount protocolCount) {
		voteCount.setApprovedBallots(protocolCount.getOrdinaryBallotCount() + zeroIfNull(protocolCount.getBlankBallotCount()));
		voteCount.setRejectedBallots(protocolCount.getQuestionableBallotCount());
		voteCount.setManualCount(protocolCount.isManualCount());
		voteCount.setInfoText(protocolCount.getComment());
		voteCount.setForeignSpecialCovers(protocolCount.getForeignSpecialCovers());
		voteCount.setSpecialCovers(zeroIfNull(protocolCount.getSpecialCovers()));
		voteCount.setEmergencySpecialCovers(protocolCount.getEmergencySpecialCovers());
		voteCount.setBallotsForOtherContests(protocolCount.getBallotCountForOtherContests());
		return this;
	}

	public VoteCountBuilder applyPreliminaryCount(PreliminaryCount preliminaryCount) {
		voteCount.setApprovedBallots(preliminaryCount.getOrdinaryBallotCount() + preliminaryCount.getBlankBallotCount());
		voteCount.setRejectedBallots(preliminaryCount.getQuestionableBallotCount());
		voteCount.setManualCount(preliminaryCount.isManualCount());
		voteCount.setInfoText(preliminaryCount.getComment());
		voteCount.setTechnicalVotings(preliminaryCount.getExpectedBallotCount());
		return this;
	}

	public VoteCountBuilder applyFinalCount(FinalCount finalCount) {
		voteCount.setApprovedBallots(finalCount.getOrdinaryBallotCount());
		voteCount.setRejectedBallots(finalCount.getTotalRejectedBallotCount());
		voteCount.setId(finalCount.getId());
		voteCount.setInfoText(finalCount.getComment());
		voteCount.setManualCount(finalCount.isManualCount());
		return this;
	}
}
