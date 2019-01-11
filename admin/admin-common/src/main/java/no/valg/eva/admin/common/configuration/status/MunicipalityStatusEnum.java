package no.valg.eva.admin.common.configuration.status;

import java.util.HashMap;
import java.util.Map;

public enum MunicipalityStatusEnum {
	CENTRAL_CONFIGURATION(0),
	LOCAL_CONFIGURATION(1),
	APPROVED_CONFIGURATION(3);

	private static final Map<Integer, MunicipalityStatusEnum> ID_TO_ENUMVALUE_MAP = new HashMap<>();

	static {
		for (MunicipalityStatusEnum value : MunicipalityStatusEnum.values()) {
			ID_TO_ENUMVALUE_MAP.put(value.id, value);
		}
	}

	private int id;

	MunicipalityStatusEnum(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static MunicipalityStatusEnum fromId(int id) {
		return ID_TO_ENUMVALUE_MAP.get(id);
	}
}
