package no.valg.eva.admin.counting.repository;

import java.util.List;

import javax.persistence.EntityManager;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.rbac.domain.model.Operator;

public class ModifiedBallotBatchRepository extends BaseRepository {

	private static final String BALLOT_COUNT_PK = "ballotCountPk";
	private static final String PROCESS = "process";

	public ModifiedBallotBatchRepository() {
	}

	public ModifiedBallotBatchRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<ModifiedBallotBatch> activeBatchesForOperator(Operator operator) {
		return getEm()
				.createNamedQuery("ModifiedBallotBatch.findActiveForOperator", ModifiedBallotBatch.class)
				.setParameter("operator", operator)
				.getResultList();
	}

	public ModifiedBallotBatch createModifiedBallotBatch(UserData userData, ModifiedBallotBatch modifiedBallotBatch) {
		return createEntity(userData, modifiedBallotBatch);
	}

	public int countModifiedBallotsNotInProcess(BallotCountRef ballotCountRef, ModifiedBallotBatchProcess process) {
		return getEm()
				.createNamedQuery("ModifiedBallotBatch.countModifiedBallotsNotInProcess", Long.class)
				.setParameter(BALLOT_COUNT_PK, ballotCountRef.getPk())
				.setParameter(PROCESS, process)
				.getSingleResult()
				.intValue();
	}
	
	public int findHighestBatchMemberSerialNumberForBallotCount(BallotCountRef ballotCountRef) {
		return (int) getEm()
				.createNamedQuery("ModifiedBallotBatch.lowestBatchMemberSerialNumberForBallotCount")
				.setParameter(BALLOT_COUNT_PK, ballotCountRef.getPk())
				.getSingleResult();
	}

	public ModifiedBallotBatch findByBatchId(BatchId batchId) {
		return findEntityById(ModifiedBallotBatch.class, batchId.getId());
	}

	public long countModifiedBallotBatchForBallotCountPks(List<Long> ballotCountPks) {
		if (ballotCountPks == null || ballotCountPks.isEmpty()) {
			return 0;
		}
		return (long) getEm()
				.createNamedQuery("ModifiedBallotBatch.countModifiedBallotBatchForBallotCountPks")
				.setParameter("ballotCountPks", ballotCountPks)
				.getSingleResult();
	}
}
