package no.valg.eva.admin.common.configuration.status;

import java.util.HashMap;
import java.util.Map;

public enum CountyStatusEnum {
	CENTRAL_CONFIGURATION(0),
	LOCAL_CONFIGURATION(1),
	APPROVED_CONFIGURATION(3);

	private static final Map<Integer, CountyStatusEnum> ID_TO_ENUMVALUE_MAP = new HashMap<>();

	static {
		for (CountyStatusEnum value : CountyStatusEnum.values()) {
			ID_TO_ENUMVALUE_MAP.put(value.id, value);
		}
	}

	private int id;

	CountyStatusEnum(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static CountyStatusEnum fromId(int id) {
		return ID_TO_ENUMVALUE_MAP.get(id);
	}
}
