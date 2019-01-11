package no.valg.eva.admin.common.configuration.status;

import java.util.HashMap;
import java.util.Map;

public enum ElectionEventStatusEnum {
	CENTRAL_CONFIGURATION(0),
	LOCAL_CONFIGURATION(1),
	FINISHED_CONFIGURATION(2),
	APPROVED_CONFIGURATION(3),
	CLOSED(9);

	private static final Map<Integer, ElectionEventStatusEnum> ID_TO_ENUMVALUE_MAP = new HashMap<>();

	static {
		for (ElectionEventStatusEnum value : ElectionEventStatusEnum.values()) {
			ID_TO_ENUMVALUE_MAP.put(value.id, value);
		}
	}

	private int id;

	private ElectionEventStatusEnum(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static ElectionEventStatusEnum fromId(int id) {
		return ID_TO_ENUMVALUE_MAP.get(id);
	}
}
