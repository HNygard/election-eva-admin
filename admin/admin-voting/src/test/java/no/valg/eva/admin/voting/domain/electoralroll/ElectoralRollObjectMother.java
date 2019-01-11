package no.valg.eva.admin.voting.domain.electoralroll;

import org.joda.time.LocalDate;

public final class ElectoralRollObjectMother {

	private ElectoralRollObjectMother() {
		// Intended to avoid instantiation
	}

	public static final String VALGHENDELSESID = "123456";
	public static final String VALGHENDELSESID_ANNEN = "234567";

	
	public static final LocalDate BORN_BEFORE_2000 = new LocalDate(2000, 1, 1);
	public static final LocalDate BORN_BEFORE_1998 = new LocalDate(1998, 1, 1);
	public static final LocalDate BORN_IN_1999 = new LocalDate(1999, 4, 4);
	

	public static final String MUNICIPALITY_ID_PORSGRUNN = "0805";
	public static final String MUNICIPALITY_ID_STAVANGER = "1103";
	public static final String MUNICIPALITY_ID_SVALBARD = "2111";
	public static final String MUNICIPALITY_ID_NOT_EXIST = "9876";

	public static final LocalDate MUST_BE_BORN_BEFORE_PORSGRUNN = BORN_BEFORE_2000;
	public static final LocalDate MUST_BE_BORN_BEFORE_STAVANGER = BORN_BEFORE_2000;
	public static final LocalDate MUST_BE_BORN_BEFORE_SVALBARD = BORN_BEFORE_1998;

}
