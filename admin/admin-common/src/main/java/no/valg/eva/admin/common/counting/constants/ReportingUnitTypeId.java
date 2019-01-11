package no.valg.eva.admin.common.counting.constants;

public enum ReportingUnitTypeId {
	VALGHENDELSESSTYRET(0),
	OPPTELLINGSVALGSTYRET(1),
	RIKSVALGSTYRET(2),
	FYLKESVALGSTYRET(3),
	VALGSTYRET(4),
	BYDELSVALGSTYRET(5),
	STEMMESTYRET(6);
	
	private final int id;

	ReportingUnitTypeId(final int id) {
		this.id = id;
	}

	public static ReportingUnitTypeId fromId(int id) {
		
		switch (id) {
			case 0: return VALGHENDELSESSTYRET;
			case 1: return OPPTELLINGSVALGSTYRET;
			case 2: return RIKSVALGSTYRET;
			case 3: return FYLKESVALGSTYRET;
			case 4: return VALGSTYRET;
			case 5: return BYDELSVALGSTYRET;
			case 6: return STEMMESTYRET;
			default: throw new IllegalStateException("No enum value for id: " + id);
		}
		
	}
	
	public int getId() {
		return id;
	}
}
