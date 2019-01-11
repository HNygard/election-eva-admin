package no.valg.eva.admin.counting.domain.service;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.BatchId.createBatchId;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.REJECTED_BALLOTS_PROCESS;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.CastBallotId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.RejectedBallot;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.counting.domain.auditevents.ThreadLocalVoteCountAuditDetailsMap;
import no.valg.eva.admin.counting.domain.auditevents.VoteCountAuditDetails;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;

public class CastBallotDomainService {
	@Inject
	private VoteCountService voteCountService;
	@Inject
	private ModifiedBallotBatchRepository modifiedBallotBatchRepository;
	@Inject
	private BallotRepository ballotRepository;

	public void processRejectedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef, List<RejectedBallot> rejectedBallots) {
		ReportingUnitTypeId overrideReportingUnitTypeId = approvedFinalCountRef.reportingUnitTypeId();
		ReportingUnitTypeId reportingUnitTypeId = reportingUnitTypeIdForProcessingRejectedBallots(userData, overrideReportingUnitTypeId);
		CountContext countContext = approvedFinalCountRef.countContext();
		AreaPath countingAreaPath = approvedFinalCountRef.countingAreaPath();
		VoteCount voteCount = voteCountService.findApprovedFinalVoteCount(reportingUnitTypeId, countContext, countingAreaPath, userData.getOperatorAreaPath());
		int newApprovedBallots;
		if (voteCountService.isLastReportingUnitForContest(reportingUnitTypeId, countContext, countingAreaPath)) {
			Map<String, ModifiedBallotBatch> modifiedBallotBatchMap = new HashMap<>();
			newApprovedBallots = processRejectBallots(userData, rejectedBallots, voteCount, modifiedBallotBatchMap);
			updateModifiedBallots(userData, voteCount, modifiedBallotBatchMap);
		} else {
			newApprovedBallots = processRejectBallots(userData, rejectedBallots, voteCount);
		}
		updateVoteCount(voteCount, newApprovedBallots);
		auditLogFinalVoteCount(voteCount);
	}

	private void auditLogFinalVoteCount(VoteCount finalVoteCount) {
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(FINAL, new VoteCountAuditDetails(finalVoteCount, true, true));
	}

	private ReportingUnitTypeId reportingUnitTypeIdForProcessingRejectedBallots(UserData userData, ReportingUnitTypeId overrideReportingUnitTypeId) {
		return userData.getOperatorAreaLevel() == COUNTY
				|| userData.isElectionEventAdminUser() && overrideReportingUnitTypeId == FYLKESVALGSTYRET
				? FYLKESVALGSTYRET : VALGSTYRET;
	}

	private int processRejectBallots(
			UserData userData, List<RejectedBallot> rejectedBallots, VoteCount voteCount, Map<String, ModifiedBallotBatch> modifiedBallotBatchMap) {
		Map<String, BallotCount> ballotCountMap = voteCount.getBallotCountMap();
		Map<String, BallotCount> rejectedBallotCountMap = voteCount.getRejectedBallotCountMap();
		int newApprovedBallots = 0;
		for (RejectedBallot rejectedBallot : rejectedBallots) {
			String ballotRejectionId = rejectedBallot.getBallotRejectionId();
			BallotCount rejectedBallotCount = rejectedBallotCountMap.get(ballotRejectionId);
			CastBallot castBallot = rejectedBallotCount.getCastBallot(new CastBallotId(rejectedBallot.getId()));
			switch (rejectedBallot.getState()) {
			case REJECTED:
				if (rejectedBallot.isBallotRejectionChanged()) {
					handleBallotRejectionChange(rejectedBallotCountMap, rejectedBallot, rejectedBallotCount, castBallot);
				}
				break;
			case MODIFIED:
			case UNMODIFIED:
				rejectedBallotCount.setUnmodifiedBallots(rejectedBallotCount.getUnmodifiedBallots() - 1);
				rejectedBallotCount.getCastBallots().remove(castBallot);
				String selectedBallotId = rejectedBallot.getSelectedBallotId();
				BallotCount ballotCount = ballotCountMap.get(selectedBallotId);
				if (ballotCount == null) {
					// Handle missing ballot type (typically BLANK).
					Ballot ballot = ballotRepository.findByContestAndId(voteCount.getContestReport().getContest().getPk(), selectedBallotId);
					ballotCount = voteCount.addNewBallotCount(ballot, 0, 0);
					ballotCountMap = voteCount.getBallotCountMap();
				}
				ballotCount.getCastBallots().add(castBallot);
				if (rejectedBallot.getState() == RejectedBallot.State.MODIFIED) {
					handleModifiedBallots(userData, modifiedBallotBatchMap, castBallot, selectedBallotId, ballotCount);
				} else {
					ballotCount.setUnmodifiedBallots(ballotCount.getUnmodifiedBallots() + 1);
					castBallot.setType(CastBallot.Type.UNMODIFIED);
				}
				castBallot.setBallotCount(ballotCount);
				newApprovedBallots++;
				break;
			default:
				throw new IllegalStateException("unknown rejected ballot state: " + rejectedBallot.getState());
			}
		}
		return newApprovedBallots;
	}

	private void handleModifiedBallots(UserData userData, Map<String, ModifiedBallotBatch> modifiedBallotBatchMap, CastBallot castBallot,
			String selectedBallotId, BallotCount ballotCount) {
		ballotCount.setModifiedBallots(ballotCount.getModifiedBallots() + 1);
		castBallot.setType(MODIFIED);
		if (modifiedBallotBatchMap != null) {
			ModifiedBallotBatch modifiedBallotBatch = modifiedBallotBatchMap.get(selectedBallotId);
			if (modifiedBallotBatch == null) {
				modifiedBallotBatch = new ModifiedBallotBatch(userData.getOperator(), ballotCount, REJECTED_BALLOTS_PROCESS);
				modifiedBallotBatchMap.put(selectedBallotId, modifiedBallotBatch);
			}
			ModifiedBallotBatchMember modifiedBallotBatchMember =
					new ModifiedBallotBatchMember(castBallot, false, -1);
			modifiedBallotBatch.addModifiedBallotBatchMember(modifiedBallotBatchMember);
		}
	}

	private void handleBallotRejectionChange(Map<String, BallotCount> rejectedBallotCountMap, RejectedBallot rejectedBallot, BallotCount rejectedBallotCount,
			CastBallot castBallot) {
		rejectedBallotCount.setUnmodifiedBallots(rejectedBallotCount.getUnmodifiedBallots() - 1);
		rejectedBallotCount.getCastBallots().remove(castBallot);
		BallotCount otherRejectedBallotCount = rejectedBallotCountMap.get(rejectedBallot.getSelectedBallotRejectionId());
		otherRejectedBallotCount.setUnmodifiedBallots(otherRejectedBallotCount.getUnmodifiedBallots() + 1);
		castBallot.setBallotCount(otherRejectedBallotCount);
		otherRejectedBallotCount.getCastBallots().add(castBallot);
	}

	private int processRejectBallots(UserData userData, List<RejectedBallot> rejectedBallots, VoteCount voteCount) {
		return processRejectBallots(userData, rejectedBallots, voteCount, null);
	}

	private void updateModifiedBallots(UserData userData, VoteCount voteCount, Map<String, ModifiedBallotBatch> modifiedBallotBatchMap) {
		if (!modifiedBallotBatchMap.isEmpty()) {
			createModifiedBallotBatchesForBallotIdsWithNewModifiedBallots(userData, modifiedBallotBatchMap);
			createModifiedBallotBatchesForBallotIdsWithOnlyExistingModifiedBallots(userData, voteCount, modifiedBallotBatchMap);
			voteCount.setModifiedBallotsProcessed(false);
		}
	}

	private void createModifiedBallotBatchesForBallotIdsWithNewModifiedBallots(UserData userData, Map<String, ModifiedBallotBatch> modifiedBallotBatchMap) {
		for (ModifiedBallotBatch modifiedBallotBatchToCreate : modifiedBallotBatchMap.values()) {
			Set<CastBallot> modifiedCastBallotsWithoutBatch = modifiedCastBallotsWithoutBatch(modifiedBallotBatchToCreate);
			int nextSerialNumber = createCompletedModifiedBallotBatchForCastBallotsWithoutBatch(
					userData, modifiedBallotBatchToCreate.getBallotCount(), modifiedCastBallotsWithoutBatch);
			setBatchMemberSerialNumbers(modifiedBallotBatchToCreate, nextSerialNumber);
			createModifiedBallotBatch(userData, modifiedBallotBatchToCreate);
		}
	}

	private void createModifiedBallotBatchesForBallotIdsWithOnlyExistingModifiedBallots(
			UserData userData, VoteCount voteCount, Map<String, ModifiedBallotBatch> modifiedBallotBatchMap) {
		for (BallotCount ballotCount : voteCount.getBallotCountMap().values()) {
			if (modifiedBallotBatchMap.containsKey(ballotCount.getBallotId()) || ballotCount.getModifiedBallots() == 0) {
				continue;
			}
			createCompletedModifiedBallotBatchForCastBallotsWithoutBatch(userData, ballotCount, ballotCount.getModifiedCastBallots());
		}
	}

	private Set<CastBallot> modifiedCastBallotsWithoutBatch(ModifiedBallotBatch modifiedBallotBatch) {
		Set<CastBallot> modifiedCastBallotsWithoutBatch = new LinkedHashSet<>(modifiedBallotBatch.getBallotCount().getModifiedCastBallots());
		Set<ModifiedBallotBatchMember> batchMembers = modifiedBallotBatch.getBatchMembers();
		for (ModifiedBallotBatchMember batchMember : batchMembers) {
			modifiedCastBallotsWithoutBatch.remove(batchMember.getCastBallot());
		}
		return modifiedCastBallotsWithoutBatch;
	}

	private int createCompletedModifiedBallotBatchForCastBallotsWithoutBatch(UserData userData, BallotCount ballotCount, Set<CastBallot> castBallotsWithoutBatch) {
		int nextSerialNumber = 1;
		if (!castBallotsWithoutBatch.isEmpty()) {
			ModifiedBallotBatch completedModifiedBallotBatch = new ModifiedBallotBatch(userData.getOperator(), ballotCount, MODIFIED_BALLOTS_PROCESS);
			for (CastBallot castBallot : castBallotsWithoutBatch) {
				completedModifiedBallotBatch.addModifiedBallotBatchMember(new ModifiedBallotBatchMember(castBallot, true, nextSerialNumber++));
			}
			createModifiedBallotBatch(userData, completedModifiedBallotBatch);
		}
		return nextSerialNumber;
	}

	private void createModifiedBallotBatch(UserData userData, ModifiedBallotBatch modifiedBallotBatch) {
		modifiedBallotBatch.setId(createBatchId(modifiedBallotBatch.getBallotCount().getPk().toString(), modifiedBallotBatch.getSerialNumberRange()));
		modifiedBallotBatchRepository.createModifiedBallotBatch(userData, modifiedBallotBatch);
	}

	private void setBatchMemberSerialNumbers(ModifiedBallotBatch modifiedBallotBatchToCreate, int nextSerialNumber) {
		for (ModifiedBallotBatchMember batchMember : modifiedBallotBatchToCreate.getBatchMembers()) {
			batchMember.setSerialNumber(nextSerialNumber++);
		}
	}

	private void updateVoteCount(VoteCount voteCount, int newApprovedBallots) {
		if (newApprovedBallots > 0) {
			voteCount.setApprovedBallots(voteCount.getApprovedBallots() + newApprovedBallots);
			voteCount.setRejectedBallots(voteCount.getRejectedBallots() - newApprovedBallots);
		}
		voteCount.setRejectedBallotsProcessed(true);
	}
}
