package no.valg.eva.admin.common.configuration.status;

import java.util.HashMap;
import java.util.Map;

public enum ContestStatus {

	CENTRAL_CONFIGURATION(0), LOCAL_CONFIGURATION(1), FINISHED_CONFIGURATION(2), APPROVED_CONFIGURATION(3);

	private static final Map<Integer, ContestStatus> ID_TO_ENUMVALUE_MAP = new HashMap<>();
	static {
		for (ContestStatus value : ContestStatus.values()) {
			ID_TO_ENUMVALUE_MAP.put(value.id, value);
		}
	}

	private int id;

	ContestStatus(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static ContestStatus fromId(int id) {
		return ID_TO_ENUMVALUE_MAP.get(id);
	}

	/**
	 * @return text id for this category used in GUI
	 */
	public String getName() {
		return "@contest_status[" + id() + "].name";
	}

	public boolean canBeApproved() {
		return this == FINISHED_CONFIGURATION || this == LOCAL_CONFIGURATION;
	}

	public boolean canBeSentToApproval() {
		return this == LOCAL_CONFIGURATION;
	}

	public boolean isApproved() {
		return this == APPROVED_CONFIGURATION;
	}

	public boolean isFinished() {
		return this == FINISHED_CONFIGURATION;
	}
}
