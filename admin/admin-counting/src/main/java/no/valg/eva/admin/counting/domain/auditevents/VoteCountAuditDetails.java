package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.auditlog.JsonBuilder.jsonObjectToString;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;

import javax.json.JsonObject;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public class VoteCountAuditDetails {
	private ContestReportAuditDetails contestReportAuditDetails;
	private String areaPath;
	private String qualifier;
	private String category;
	private String id;
	private CountStatus status;
	private Integer approvedBallots;
	private Integer rejectedBallots;
	private Integer technicalVotings;
	private boolean manualCount;
	private boolean modifiedBallotsProcessed;
	private boolean rejectedBallotsProcessed;
	private String infoText;
	private Integer foreignSpecialCovers;
	private Integer specialCovers;
	private Integer emergencySpecialCovers;
	private Integer ballotsForOtherContests;
	private BallotCountsAuditDetails ballotCountsAuditDetails;
	
	public VoteCountAuditDetails(VoteCount voteCount, boolean splitCount, boolean includeCastBallots) {
		this.contestReportAuditDetails = new ContestReportAuditDetails(voteCount.getContestReport());
		this.areaPath = voteCount.getMvArea().getAreaPath();
		this.qualifier = voteCount.getCountQualifierId();
		this.category = voteCount.getVoteCountCategoryId();
		this.id = voteCount.getId();
		this.status = voteCount.getCountStatus();
		this.approvedBallots = voteCount.getApprovedBallots();
		this.rejectedBallots = voteCount.getRejectedBallots();
		this.technicalVotings = voteCount.getTechnicalVotings();
		this.manualCount = voteCount.isManualCount();
		this.modifiedBallotsProcessed = voteCount.isModifiedBallotsProcessed();
		this.rejectedBallotsProcessed = voteCount.isRejectedBallotsProcessed();
		this.infoText = voteCount.getInfoText();
		if (includeForeignSpecialCovers(voteCount)) {
			this.foreignSpecialCovers = voteCount.getForeignSpecialCovers();
		}
		if (PROTOCOL.getId().equals(qualifier)) {
			this.specialCovers = voteCount.getSpecialCovers();
		}
		if (includeEmergencySpecialCovers(voteCount)) {
			this.emergencySpecialCovers = voteCount.getEmergencySpecialCovers();
		}
		if (includeBallotsForOtherContests(voteCount)) {
			this.ballotsForOtherContests = voteCount.getBallotsForOtherContests();
		}
		if (includeBallotCountsAuditDetails(voteCount)) {
			this.ballotCountsAuditDetails = new BallotCountsAuditDetails(voteCount.getBallotCountSet(), splitCount, includeCastBallots);
		}
	}

	private boolean includeForeignSpecialCovers(VoteCount voteCount) {
		return isProtocolVoteCount(voteCount) && !hasElectronicMarkOffs(voteCount);
	}

	private boolean includeEmergencySpecialCovers(VoteCount voteCount) {
		return isProtocolVoteCount(voteCount) && hasElectronicMarkOffs(voteCount);
	}

	private boolean includeBallotCountsAuditDetails(VoteCount voteCount) {
		return !PROTOCOL.getId().equals(voteCount.getCountQualifierId());
	}

	private boolean hasElectronicMarkOffs(VoteCount voteCount) {
		return voteCount.getMvArea().getMunicipality().isElectronicMarkoffs();
	}

	private boolean includeBallotsForOtherContests(VoteCount voteCount) {
		return isProtocolVoteCount(voteCount) && voteCount.getContestReport().getContest().isOnBoroughLevel();
	}

	private boolean isProtocolVoteCount(VoteCount voteCount) {
		return PROTOCOL.getId().equals(voteCount.getCountQualifierId());
	}

	public JsonObject toJsonObject() {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("contestReport", contestReportAuditDetails.toJsonObject())
				.add("areaPath", areaPath)
				.add("qualifier", qualifier)
				.add("category", category)
				.add("id", id)
				.add("status", status.name())
				.add("approvedBallots", approvedBallots)
				.add("rejectedBallots", rejectedBallots);
		if (technicalVotings != null) {
			jsonBuilder.add("technicalVotings", technicalVotings);
		}
		jsonBuilder.add("manualCount", manualCount);
		if (CountQualifier.FINAL.getId().equals(qualifier)) {
			jsonBuilder
					.add("modifiedBallotsProcessed", modifiedBallotsProcessed)
					.add("rejectedBallotsProcessed", rejectedBallotsProcessed);
		}
		jsonBuilder.add("infoText", infoText);
		if (foreignSpecialCovers != null) {
			jsonBuilder.add("foreignSpecialCovers", foreignSpecialCovers);
		}
		if (specialCovers != null) {
			jsonBuilder.add("specialCovers", specialCovers);
		}
		if (emergencySpecialCovers != null) {
			jsonBuilder.add("emergencySpecialCovers", emergencySpecialCovers);
		}
		if (ballotsForOtherContests != null) {
			jsonBuilder.add("ballotsForOtherContests", ballotsForOtherContests);
		}
		if (ballotCountsAuditDetails != null) {
			jsonBuilder.add("ballotCounts", ballotCountsAuditDetails.toJsonArray());
		}
		return jsonBuilder.asJsonObject();
	}

	public String toJson() {
		return jsonObjectToString(toJsonObject());
	}
}
