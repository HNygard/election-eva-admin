package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.util.List;

import org.apache.commons.collections.list.UnmodifiableList;

public class RegisterModifiedBallotCountStatus {
	private boolean hasNoUnfinishedBatches = true;
	private List<ModifiedBallotsStatus> modifiedBallotsStatusList;

	private RegisterModifiedBallotCountStatus() {
	}

	public RegisterModifiedBallotCountStatus(List<ModifiedBallotsStatus> modifiedBallotsStatuses) {
		modifiedBallotsStatusList = modifiedBallotsStatuses;
		for (ModifiedBallotsStatus status : modifiedBallotsStatusList) {
			if (status.isMustFinishOngoingBatch()) {
				this.hasNoUnfinishedBatches = false;
			}
			status.setBallotCountStatus(this);
		}
	}

	public List<ModifiedBallotsStatus> getModifiedBallotsStatusList() {
		return UnmodifiableList.decorate(modifiedBallotsStatusList);
	}

	public void setModifiedBallotsStatusList(final List<ModifiedBallotsStatus> modifiedBallotsStatusList) {
		this.modifiedBallotsStatusList = modifiedBallotsStatusList;
	}

	public boolean isRegistrationOfAllModifiedBallotsCompleted() {
		for (ModifiedBallotsStatus modifiedBallotsStatus : modifiedBallotsStatusList) {
			if (!modifiedBallotsStatus.isRegistrationComplete()) {
				return false;
			}
		}
		return true;
	}

	public boolean isHasNoUnfinishedBatches() {
		return hasNoUnfinishedBatches;
	}
}
