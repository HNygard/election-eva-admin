package no.valg.eva.admin.counting.domain.modifiedballots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallotsStatus;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.counting.repository.BallotCountRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;

/**
 * Domain service for handling of modified ballots
 */
public class ModifiedBallotDomainService {
	@Inject
	protected ModifiedBallotBatchRepository modifiedBallotBatchRepository;
	@Inject
	protected ContestReportRepository contestReportRepository;
	@Inject
	protected BallotCountRepository ballotCountRepository;
	
	public List<ModifiedBallotsStatus> buildModifiedBallotsStatuses(FinalCount finalCount, Operator operator, ModifiedBallotBatchProcess process) {
		ContestReport contestReport = contestReportRepository.findByFinalCount(finalCount.getVoteCountPk());
		List<BallotCount> ballotCountEntities = contestReport.getVoteCount(finalCount.getVoteCountPk()).getBallotCountList();
		Map<String, BallotCount> idToBallotCountEntityMap = buildIdToBallotCountEntityMap(ballotCountEntities);
		List<ModifiedBallotBatch> modifiedBallotBatches = modifiedBallotBatchRepository.activeBatchesForOperator(operator);
		Map<String, String> idToActiveBatchIdMap = buildIdToModifiedBallotBatch(modifiedBallotBatches);
		return buildModifiedBallotsStatus(finalCount, idToBallotCountEntityMap, idToActiveBatchIdMap, process);
	}
	
	private Map<String, String> buildIdToModifiedBallotBatch(List<ModifiedBallotBatch> modifiedBallotBatches) {
		Map<String, String> idToBatchMap = new HashMap<>();
		for (ModifiedBallotBatch batch : modifiedBallotBatches) {
			idToBatchMap.put(ballotCountId(batch.getBallotCount()), batch.getId());
		}
		return idToBatchMap;
	}

	private Map<String, BallotCount> buildIdToBallotCountEntityMap(List<BallotCount> ballotCountEntities) {
		Map<String, BallotCount> idToBallotCountEntityMap = new HashMap<>();
		for (BallotCount ballotCountEntity : ballotCountEntities) {
			if (ballotCountEntity.getBallot() != null) {
				idToBallotCountEntityMap.put(ballotCountId(ballotCountEntity), ballotCountEntity);
			}
		}
		return idToBallotCountEntityMap;
	}

	private String ballotCountId(BallotCount ballotCountEntity) {
		return ballotCountEntity.getPk().toString();
	}

	private List<ModifiedBallotsStatus> buildModifiedBallotsStatus(
			FinalCount finalCount, Map<String, BallotCount> idToBallotCountEntityMap,
			Map<String, String> idToActiveBatchIdMap, ModifiedBallotBatchProcess process) {
		List<ModifiedBallotsStatus> modifiedBallotCounts = new ArrayList<>();
		for (no.valg.eva.admin.common.counting.model.BallotCount ballotCountForView : finalCount.getBallotCounts()) {
			String ballotCountMapId = ballotCountId(ballotCountRepository.findByPk(ballotCountForView.getBallotCountRef().getPk()));
			BallotCount ballotCountEntity = idToBallotCountEntityMap.get(ballotCountMapId);
			ModifiedBallotsStatus modifiedBallotsStatus = buildModifiedBallotsStatus(
					ballotCountEntity, ballotCountForView, idToActiveBatchIdMap.get(ballotCountMapId), process);
			modifiedBallotCounts.add(modifiedBallotsStatus);
		}
		return modifiedBallotCounts;
	}

	public ModifiedBallotsStatus buildModifiedBallotsStatus(
			BallotCount ballotCountEntity, no.valg.eva.admin.common.counting.model.BallotCount ballotCountForView,
			String batchId, ModifiedBallotBatchProcess process) {
		if (ballotCountEntity != null) {
			Set<ModifiedBallotBatch> modifiedBallotBatches =  ballotCountEntity.getModifiedBallotBatches();
			int total = countTotalModifiedBallotsForProcess(ballotCountEntity, process);

			int inProgress = calculateInProgress(process, modifiedBallotBatches);
			int completed = calculateCompleted(process, modifiedBallotBatches);
			
			return new ModifiedBallotsStatus(ballotCountForView, total, inProgress, completed, batchId);
		}
		return new ModifiedBallotsStatus(ballotCountForView, batchId);
	}

	private int countTotalModifiedBallotsForProcess(BallotCount ballotCountEntity, ModifiedBallotBatchProcess process) {
		return ballotCountEntity.getModifiedBallots()
				- modifiedBallotBatchRepository.countModifiedBallotsNotInProcess(new BallotCountRef(ballotCountEntity.getPk()), process);
	}

	public ModifiedBallotsStatus buildModifiedBallotsStatus(
			BallotCount count, no.valg.eva.admin.common.counting.model.BallotCount ballotCount, ModifiedBallotBatchProcess process) {
		return buildModifiedBallotsStatus(count, ballotCount, null, process);
	}


	private int calculateInProgress(ModifiedBallotBatchProcess process, Set<ModifiedBallotBatch> modifiedBallotBatches) {
		int inProgressCount = 0;
		for (ModifiedBallotBatch modifiedBallotBatch : modifiedBallotBatches) {
			if (modifiedBallotBatch.getProcess() != process) {
				continue;
			}
			inProgressCount += modifiedBallotBatch.inProgressCount();
		}
		return inProgressCount;
	}

	private int calculateCompleted(ModifiedBallotBatchProcess process, Set<ModifiedBallotBatch> modifiedBallotBatches) {
		int completedCount = 0;
		for (ModifiedBallotBatch modifiedBallotBatch : modifiedBallotBatches) {
			if (modifiedBallotBatch.getProcess() != process) {
				continue;
			}
			completedCount += modifiedBallotBatch.completedCount();
		}
		return completedCount;
	}

}
