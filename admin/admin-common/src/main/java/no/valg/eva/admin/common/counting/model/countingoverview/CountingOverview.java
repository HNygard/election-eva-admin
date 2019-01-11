package no.valg.eva.admin.common.counting.model.countingoverview;

import java.io.Serializable;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;

public interface CountingOverview extends Serializable {
	String getName();

	List<Status> getStatuses();

	default Status getStatus() {
		return getStatuses()
				.stream()
				.reduce(Status::merge)
				.orElse(new CountingStatus());
	}

	default CountCategory getCategory() {
		return null;
	}

	default ElectionPath getContestPath() {
		return null;
	}

	AreaPath getAreaPath();

	default boolean hasCount() {
		return false;
	}

	default boolean isRejectedBallotsPending() {
		return hasCount() && getStatuses()
				.stream()
				.filter(Status::isRejectedBallotsPending)
				.count() > 0;
	}

	default boolean isManualRejectedBallotsPending() {
		return hasCount() && getStatuses()
				.stream()
				.filter(Status::isRejectedBallotsPending)
				.filter(Status::isManualCount)
				.count() > 0;
	}
}
