package no.valg.eva.admin.common.counting.model.countingoverview;

import java.io.Serializable;

public interface Status extends Serializable {
	StatusType getStatusType();

	boolean isManualCount();

	String getPrimaryIconStyle();

	String getSecondaryIconStyle();

	Integer getRejectedBallotCount();

	default boolean isRejectedBallotsPending() {
		return false;
	}

	String getPanelStyle();

	Status merge(Status status);

}
