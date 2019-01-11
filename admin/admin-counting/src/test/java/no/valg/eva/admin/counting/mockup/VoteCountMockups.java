package no.valg.eva.admin.counting.mockup;

import static no.valg.eva.admin.common.mockups.ContestMockups.contest;
import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.ElectionGroupMockups.electionGroup;
import static no.valg.eva.admin.common.mockups.ElectionMockups.election;
import static no.valg.eva.admin.common.mockups.GeneralMockups.COUNT_CATEGORY_ID_FO;
import static no.valg.eva.admin.common.mockups.GeneralMockups.COUNT_CATEGORY_ID_VF;
import static no.valg.eva.admin.common.mockups.GeneralMockups.COUNT_CATEGORY_ID_VO;
import static no.valg.eva.admin.common.mockups.GeneralMockups.ELECTRONIC_MARK_OFFS_TRUE;
import static no.valg.eva.admin.common.mockups.MunicipalityMockups.municipality;
import static no.valg.eva.admin.common.mockups.MvAreaMockups.pollingDistrictMvArea;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.COUNT_QUALIFIER_PK_SERIES;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.VOTE_COUNT_CATEGORY_PK_SERIES;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.VOTE_COUNT_PK_SERIES;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.VOTE_COUNT_STATUS_PK_SERIES;
import static no.valg.eva.admin.common.mockups.ReportingUnitMockups.reportingUnit;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.BALLOT_COUNT_DEM;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.BALLOT_COUNT_KYST;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.BALLOT_COUNT_NKP;
import static no.valg.eva.admin.counting.mockup.ContestReportMockups.contestReport;

import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;


public final class VoteCountMockups {

	public static final long VOTE_COUNT_PK_PROTOCOL = VOTE_COUNT_PK_SERIES + 1;
	public static final long VOTE_COUNT_PK_PRELIMINARY = VOTE_COUNT_PK_SERIES + 2;
	
	public static final long VOTE_COUNT_PK_FINAL = VOTE_COUNT_PK_SERIES + 3;

	public static final String VOTE_COUNT_ID_PROTOCOL = "PVO1";
	public static final String VOTE_COUNT_ID_PRELIMINARY = "FVO1";
	public static final String VOTE_COUNT_ID_FINAL = "EVO1";

	public static final long VOTE_COUNT_STATUS_PK_COUNTING = VOTE_COUNT_STATUS_PK_SERIES + 1;
	public static final long VOTE_COUNT_STATUS_PK_APPROVED = VOTE_COUNT_STATUS_PK_SERIES + 2;
	public static final long VOTE_COUNT_STATUS_PK_REJECTED = VOTE_COUNT_STATUS_PK_SERIES + 3;
	public static final int COUNT_STATUS_ID_COUNTING = CountStatus.SAVED.getId();
	public static final int COUNT_STATUS_ID_APPROVED = CountStatus.APPROVED.getId();
	public static final int COUNT_STATUS_ID_REVOKED = CountStatus.REVOKED.getId();

	public static final long VOTE_COUNT_CATEGORY_PK_VO = VOTE_COUNT_CATEGORY_PK_SERIES + 1;
	public static final long VOTE_COUNT_CATEGORY_PK_EO = VOTE_COUNT_CATEGORY_PK_SERIES + 2;
	public static final long VOTE_COUNT_CATEGORY_PK_EK = VOTE_COUNT_CATEGORY_PK_SERIES + 3;
	public static final long VOTE_COUNT_CATEGORY_PK_VF = VOTE_COUNT_CATEGORY_PK_SERIES + 4;
	public static final long VOTE_COUNT_CATEGORY_PK_FO = VOTE_COUNT_CATEGORY_PK_SERIES + 5;

	public static final long COUNT_QUALIFIER_PK_PROTOCOL = COUNT_QUALIFIER_PK_SERIES + 1;
	public static final String COUNT_QUALIFIER_ID_PROTOCOL = no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL.getId();

	public static final long COUNT_QUALIFIER_PK_PRELIMINARY = COUNT_QUALIFIER_PK_SERIES + 2;
	public static final String COUNT_QUALIFIER_ID_PRELIMINARY = no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY.getId();

	public static final long COUNT_QUALIFIER_PK_FINAL = COUNT_QUALIFIER_PK_SERIES + 3;
	
	public static final String COUNT_QUALIFIER_ID_FINAL = no.valg.eva.admin.common.counting.model.CountQualifier.FINAL.getId();

	public static final boolean MANUAL_COUNT_TRUE = true;
	public static final boolean MODIFIED_BALLOTS_PROCESSED_FALSE = false;
	public static final boolean REJECTED_BALLOTS_PROCESSED_FALSE = false;
	public static final int SPECIAL_COVERS = 11;
	public static final int SPECIAL_COVERS_ZERO = 0;
	public static final boolean FOREIGN_SPECIAL_COVERS_ENABLED_TRUE = true;
	public static final boolean FOREIGN_SPECIAL_COVERS_ENABLED_FALSE = false;
	public static final Integer FOREIGN_SPECIAL_COVERS = 22;
	public static final Integer FOREIGN_SPECIAL_COVERS_ZERO = 0;
	public static final int EMERGENCY_SPECIAL_COVERS = 33;
	public static final int EMERGENCY_SPECIAL_COVERS_ZERO = 0;
	public static final int APPROVED_BALLOTS = BALLOT_COUNT_NKP + BALLOT_COUNT_KYST + BALLOT_COUNT_DEM;
	public static final int REJECTED_BALLOTS = 10;
	public static final String INFO_TEXT = "Uvisst";
	public static final String INFO_TEXT_NULL = null;

	public static final long VOTE_COUNT_COUNT_ZERO = 0;

	private VoteCountMockups() {
		// no instances allowed
	}

	public static VoteCount voteCount(
			final Long voteCountPk,
			final String voteCountId,
			final VoteCountStatus voteCountStatus,
			final VoteCountCategory voteCountCategory,
			final MvArea mvArea,
			final ContestReport contestReport,
			final CountQualifier countQualifier,
			final Integer approvedBallots,
			final Integer rejectedBallots,
			final boolean manualCount,
			final boolean modifiedBallotsProcessed,
			final boolean rejectedBallotsProcessed,
			final String infoText,
			final int foreignSpecialCovers,
			final int specialCovers,
			final Boolean electronicMarkOffs,
			final Integer emergencySpecialCovers) {

		VoteCount voteCount = new VoteCount();
		voteCount.setPk(voteCountPk);
		voteCount.setId(voteCountId);
		voteCount.setVoteCountStatus(voteCountStatus);
		voteCount.setVoteCountCategory(voteCountCategory);
		voteCount.setMvArea(mvArea);
		voteCount.setContestReport(contestReport);
		voteCount.setCountQualifier(countQualifier);
		voteCount.setApprovedBallots(approvedBallots);
		voteCount.setRejectedBallots(rejectedBallots);
		voteCount.setTechnicalVotings(null);
		voteCount.setManualCount(manualCount);
		voteCount.setModifiedBallotsProcessed(modifiedBallotsProcessed);
		voteCount.setRejectedBallotsProcessed(rejectedBallotsProcessed);
		voteCount.setInfoText(infoText);
		voteCount.setForeignSpecialCovers(foreignSpecialCovers);
		voteCount.setSpecialCovers(specialCovers);
		if (electronicMarkOffs != null && electronicMarkOffs) {
			voteCount.setEmergencySpecialCovers(emergencySpecialCovers);
		} else {
			voteCount.setEmergencySpecialCovers(0);
		}
		return voteCount;
	}

	public static VoteCount protocolVoteCount(
			final VoteCountStatus voteCountStatus,
			final MvArea mvArea,
			final ContestReport contestReport,
			final Integer approvedBallots,
			final Integer rejectedBallots,
			final String infoText,
			final int foreignSpecialCovers,
			final int specialCovers,
			final boolean electronicMarkOffs,
			final Integer emergencySpecialCovers) {

		return voteCount(
				VOTE_COUNT_PK_PROTOCOL,
				VOTE_COUNT_ID_PROTOCOL,
				voteCountStatus,
				voteCountCategoryVo(),
				mvArea,
				contestReport,
				protocolCountQualifier(),
				approvedBallots,
				rejectedBallots,
				MANUAL_COUNT_TRUE,
				MODIFIED_BALLOTS_PROCESSED_FALSE,
				REJECTED_BALLOTS_PROCESSED_FALSE,
				infoText,
				foreignSpecialCovers,
				specialCovers,
				electronicMarkOffs,
				emergencySpecialCovers);
	}

	public static VoteCount defaultProtocolVoteCount() {
		return protocolVoteCount(
				voteCountStatusCounting(),
				pollingDistrictMvArea(municipality(ELECTRONIC_MARK_OFFS_TRUE)),
				contestReport(contest(election(electionGroup(electionEvent()))), reportingUnit()),
				APPROVED_BALLOTS,
				REJECTED_BALLOTS,
				INFO_TEXT,
				FOREIGN_SPECIAL_COVERS,
				SPECIAL_COVERS,
				ELECTRONIC_MARK_OFFS_TRUE,
				EMERGENCY_SPECIAL_COVERS);
	}

	public static VoteCount preliminaryVoteCount(
			final VoteCountStatus voteCountStatus,
			final VoteCountCategory voteCountCategory,
			final MvArea mvArea,
			final ContestReport contestReport,
			final Integer approvedBallots,
			final Integer rejectedBallots,
			final boolean manualCount,
			final boolean modifiedBallotsProcessed,
			final boolean rejectedBallotsProcessed,
			final String infoText) {

		return voteCount(
				VOTE_COUNT_PK_PRELIMINARY,
				VOTE_COUNT_ID_PRELIMINARY,
				voteCountStatus,
				voteCountCategory,
				mvArea,
				contestReport,
				preliminaryCountQualifier(),
				approvedBallots,
				rejectedBallots,
				manualCount,
				modifiedBallotsProcessed,
				rejectedBallotsProcessed,
				infoText,
				0,
				0,
				null,
				null);
	}

	public static VoteCount defaultPreliminaryVoteCount() {
		return preliminaryVoteCount(
				voteCountStatusCounting(),
				voteCountCategoryVo(),
				pollingDistrictMvArea(municipality(ELECTRONIC_MARK_OFFS_TRUE)),
				contestReport(contest(election(electionGroup(electionEvent()))), reportingUnit()),
				APPROVED_BALLOTS,
				REJECTED_BALLOTS,
				MANUAL_COUNT_TRUE,
				MODIFIED_BALLOTS_PROCESSED_FALSE,
				REJECTED_BALLOTS_PROCESSED_FALSE,
				INFO_TEXT);
	}

	public static VoteCount finalVoteCount(
			final VoteCountStatus voteCountStatus,
			final VoteCountCategory voteCountCategory,
			final MvArea mvArea,
			final ContestReport contestReport,
			final Integer approvedBallots,
			final Integer rejectedBallots,
			final boolean manualCount,
			final boolean modifiedBallotsProcessed,
			final boolean rejectedBallotsProcessed,
			final String infoText) {

		return voteCount(
				VOTE_COUNT_PK_FINAL,
				VOTE_COUNT_ID_FINAL,
				voteCountStatus,
				voteCountCategory,
				mvArea,
				contestReport,
				finalCountQualifier(),
				approvedBallots,
				rejectedBallots,
				manualCount,
				modifiedBallotsProcessed,
				rejectedBallotsProcessed,
				infoText,
				0,
				0,
				null,
				null);
	}

	public static VoteCount defaultFinalVoteCount() {
		return finalVoteCount(
				voteCountStatusCounting(),
				voteCountCategoryVo(),
				pollingDistrictMvArea(municipality(ELECTRONIC_MARK_OFFS_TRUE)),
				contestReport(contest(election(electionGroup(electionEvent()))), reportingUnit()),
				APPROVED_BALLOTS,
				REJECTED_BALLOTS,
				MANUAL_COUNT_TRUE,
				MODIFIED_BALLOTS_PROCESSED_FALSE,
				REJECTED_BALLOTS_PROCESSED_FALSE,
				INFO_TEXT);
	}

	public static VoteCountStatus voteCountStatus(final Long voteCountStatusPk, final int voteCountStatusId) {
		VoteCountStatus voteCountStatus = new VoteCountStatus();
		voteCountStatus.setPk(voteCountStatusPk);
		voteCountStatus.setId(voteCountStatusId);
		return voteCountStatus;
	}

	public static VoteCountStatus voteCountStatusCounting() {
		return voteCountStatus(VOTE_COUNT_STATUS_PK_COUNTING, COUNT_STATUS_ID_COUNTING);
	}

	public static VoteCountStatus voteCountStatusApproved() {
		return voteCountStatus(VOTE_COUNT_STATUS_PK_APPROVED, COUNT_STATUS_ID_APPROVED);
	}

	public static VoteCountStatus voteCountStatusRejected() {
		return voteCountStatus(VOTE_COUNT_STATUS_PK_REJECTED, COUNT_STATUS_ID_REVOKED);
	}

	public static VoteCountCategory voteCountCategory(final Long voteCountCategoryPk, final String voteCountCategoryId) {
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setPk(voteCountCategoryPk);
		voteCountCategory.setId(voteCountCategoryId);
		return voteCountCategory;
	}

	public static VoteCountCategory voteCountCategoryVo() {
		return voteCountCategory(VOTE_COUNT_CATEGORY_PK_VO, COUNT_CATEGORY_ID_VO);
	}

	public static VoteCountCategory voteCountCategoryFo() {
		return voteCountCategory(VOTE_COUNT_CATEGORY_PK_FO, COUNT_CATEGORY_ID_FO);
	}

	public static VoteCountCategory voteCountCategoryVf() {
		return voteCountCategory(VOTE_COUNT_CATEGORY_PK_VF, COUNT_CATEGORY_ID_VF);
	}

	public static CountQualifier protocolCountQualifier() {
		CountQualifier protocolCountQualifier = new CountQualifier();
		protocolCountQualifier.setPk(COUNT_QUALIFIER_PK_PROTOCOL);
		protocolCountQualifier.setId(COUNT_QUALIFIER_ID_PROTOCOL);
		return protocolCountQualifier;
	}

	public static CountQualifier preliminaryCountQualifier() {
		CountQualifier preliminaryCountQualifier = new CountQualifier();
		preliminaryCountQualifier.setPk(COUNT_QUALIFIER_PK_PRELIMINARY);
		preliminaryCountQualifier.setId(COUNT_QUALIFIER_ID_PRELIMINARY);
		return preliminaryCountQualifier;
	}

	public static CountQualifier finalCountQualifier() {
		CountQualifier finalCountQualifier = new CountQualifier();
		finalCountQualifier.setPk(COUNT_QUALIFIER_PK_FINAL);
		finalCountQualifier.setId(COUNT_QUALIFIER_ID_FINAL);
		return finalCountQualifier;
	}
}
