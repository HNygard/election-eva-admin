package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;

import no.valg.eva.admin.common.counting.model.BallotCount;

import org.apache.commons.lang3.StringUtils;

public class ModifiedBallotsStatus implements Serializable {
	private final int total;
	private final int inProgress;
	private final int completed;

	private final BallotCount ballotCount;
	private final String incompleteBatchId;
	private RegisterModifiedBallotCountStatus ballotCountStatus;

	public ModifiedBallotsStatus(BallotCount ballotCount, String incompleteBatchId) {
		this.ballotCount = ballotCount;
		this.total = 0;
		this.inProgress = 0;
		this.completed = 0;
		this.incompleteBatchId = incompleteBatchId;
	}

	public ModifiedBallotsStatus(BallotCount ballotCount, int total, int inProgress, int completed, String incompleteBatchId) {
		this.ballotCount = ballotCount;
		this.total = total;
		this.inProgress = inProgress;
		this.completed = completed;
		this.incompleteBatchId = incompleteBatchId;
	}
	
	public String getBallotId() {
		return ballotCount.getId();
	}

	public int getTotal() {
		return total;
	}

	public int getRemaining() {
		return total - completed - inProgress;
	}

	public boolean isRegistrationComplete() {
		return getRemaining() == 0 && inProgress == 0;
	}

	public int getInProgress() {
		return inProgress;
	}

	public int getCompleted() {
		return completed;
	}

	public BallotCount getBallotCount() {
		return ballotCount;
	}

	public boolean isCanCreateNewBatch() {
		return (hasModifiedBallots() && getRemaining() > 0 && StringUtils.isBlank(incompleteBatchId));
	}

	public boolean hasModifiedBallotsAndRegistrationIsDone() {
		return (total > 0 && getRemaining() == 0 && StringUtils.isBlank(incompleteBatchId));
	}

	public boolean hasModifiedBallots() {
		return total > 0;
	}

	public String getIncompleteBatchId() {
		return incompleteBatchId;
	}

	public boolean isMustFinishOngoingBatch() {
		return StringUtils.isNotBlank(incompleteBatchId);
	}

	public RegisterModifiedBallotCountStatus getBallotCountStatus() {
		return ballotCountStatus;
	}

	public void setBallotCountStatus(RegisterModifiedBallotCountStatus ballotCountStatus) {
		this.ballotCountStatus = ballotCountStatus;
	}
}
