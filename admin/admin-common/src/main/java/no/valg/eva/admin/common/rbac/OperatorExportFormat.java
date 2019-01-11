package no.valg.eva.admin.common.rbac;

import static java.lang.String.format;

public enum OperatorExportFormat {
	ALLE_ROLLER("ALLE"),
	VALGANSVARLIG_EVA("EVA"),
	VALGANSVARLIG_CIM("CIM");

	public static OperatorExportFormat fromId(String id) {
		switch (id) {
			case "ALLE":
				return ALLE_ROLLER;
			case "EVA":
				return VALGANSVARLIG_EVA;
			case "CIM":
				return VALGANSVARLIG_CIM;
			default:
				throw new IllegalArgumentException(format("unknown count qualifier id: <%s>", id));
		}
	}

	private final String id;

	OperatorExportFormat(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
