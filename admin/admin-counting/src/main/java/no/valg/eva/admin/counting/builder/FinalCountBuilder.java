package no.valg.eva.admin.counting.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public class FinalCountBuilder {
	
	private FinalCount count;
	
	private final ReportingUnit reportingUnit;
	
	public FinalCountBuilder(
			CountCategory category,
			AreaPath areaPath,
			String areaName,
			boolean manualCount,
			ReportingUnit reportingUnit) {
		this.reportingUnit = reportingUnit;
		count = initCount(category, areaPath, areaName, manualCount);
	}

	protected FinalCount initCount(CountCategory category, AreaPath areaPath, String areaName, boolean manualCount) {
		return new FinalCount(null, areaPath, category, areaName, reportingUnit.reportingUnitTypeId(), reportingUnit.getNameLine(), manualCount);
	}

	protected void processBallotCounts(VoteCount voteCount) {
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap = voteCount.getBallotCountMap();
		ContestReport contestReport = voteCount.getContestReport();
		Contest contest = contestReport.getContest();
		Set<Ballot> sortedApprovedBallots = contest.getSortedApprovedBallots();
		for (Ballot sortedApprovedBallot : sortedApprovedBallots) {
			String ballotId = sortedApprovedBallot.getId();
			if (EvoteConstants.BALLOT_BLANK.equals(ballotId)) {
				if (ballotCountMap.containsKey(ballotId)) {
					no.valg.eva.admin.counting.domain.model.BallotCount blankBallotCountEntity = ballotCountMap.get(ballotId);
					this.count.setBlankBallotCount(blankBallotCountEntity.getUnmodifiedBallots());
				}
				continue;
			}

			no.valg.eva.admin.common.counting.model.BallotCount ballotCount;
			if (ballotCountMap.containsKey(ballotId)) {
				ballotCount = new BallotCountBuilder()
						.applyEntity(ballotCountMap.get(ballotId))
						.build();
			} else {
				ballotCount = new no.valg.eva.admin.common.counting.model.BallotCount();
				ballotCount.setId(ballotId);
				Affiliation affiliation = sortedApprovedBallot.getAffiliation();
				Party party = affiliation.getParty();
				ballotCount.setName(party.getName());
				ballotCount.setModifiedCount(0);
				ballotCount.setUnmodifiedCount(0);
			}
			this.count.getBallotCounts().add(ballotCount);
		}
	}


	public FinalCountBuilder applyBallotRejections(List<BallotRejection> ballotRejections) {
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		for (BallotRejection ballotRejection : ballotRejections) {
			String id = ballotRejection.getId();
			String name = ballotRejection.getName();
			RejectedBallotCount rejectedBallotCount = new RejectedBallotCount();
			rejectedBallotCount.setId(id);
			rejectedBallotCount.setName(name);
			rejectedBallotCounts.add(rejectedBallotCount);
		}
		this.count.setRejectedBallotCounts(rejectedBallotCounts);
		return this;
	}

	public FinalCountBuilder applyFinalVoteCount(VoteCount finalVoteCount) {
		this.count.setModifiedDate(finalVoteCount.getAuditTimestamp());
		this.count.setId(finalVoteCount.getId());
		this.count.setVersion(finalVoteCount.getAuditOplock());
		this.count.setStatus(CountStatus.fromId(finalVoteCount.getVoteCountStatusId()));
		this.count.setComment(finalVoteCount.getInfoText());
		this.count.setBallotCounts(new ArrayList<>());
		processBallotCounts(finalVoteCount);
		processRejectedBallotCounts(finalVoteCount);
		this.count.setModifiedBallotsProcessed(finalVoteCount.isModifiedBallotsProcessed());
		this.count.setRejectedBallotsProcessed(finalVoteCount.isRejectedBallotsProcessed());
		return this;
	}

	private void processRejectedBallotCounts(VoteCount voteCount) {
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> rejectedBallotCountMap = voteCount.getRejectedBallotCountMap();
		for (RejectedBallotCount rejectedBallotCount : this.count.getRejectedBallotCounts()) {
			String rejectedBallotId = rejectedBallotCount.getId();
			if (rejectedBallotCountMap.containsKey(rejectedBallotId)) {
				rejectedBallotCount.setCount(rejectedBallotCountMap.get(rejectedBallotId).getUnmodifiedBallots());
			}
		}
	}
	
	public FinalCount build() {
		return count;
	}
}
