package no.valg.eva.admin.frontend.configuration.ctrls;

import java.util.HashMap;
import java.util.Map;

import no.evote.constants.EvoteConstants;

public final class BatchStatusMap {
	private BatchStatusMap() {
		// Intentionally empty
	}

	private static final Map<Integer, String> BATCH_STATUS_MAP = new HashMap<Integer, String>();
	static {
		BATCH_STATUS_MAP.put(EvoteConstants.BATCH_STATUS_IN_QUEUE_ID, "@batch_status[0].name");
		BATCH_STATUS_MAP.put(EvoteConstants.BATCH_STATUS_STARTED_ID, "@batch_status[1].name");
		BATCH_STATUS_MAP.put(EvoteConstants.BATCH_STATUS_COMPLETED_ID, "@batch_status[2].name");
		BATCH_STATUS_MAP.put(EvoteConstants.BATCH_STATUS_FAILED_ID, "@batch_status[3].name");
	};

	public static String getBatchStatusMessage(final int batchStatus) {
		return BATCH_STATUS_MAP.get(batchStatus);
	}
}
