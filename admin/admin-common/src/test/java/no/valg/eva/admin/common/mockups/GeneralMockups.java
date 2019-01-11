package no.valg.eva.admin.common.mockups;

import no.valg.eva.admin.common.counting.model.CountCategory;

import org.joda.time.LocalDate;

public final class GeneralMockups {

	// global values
	public static final String COUNT_CATEGORY_ID_VO = CountCategory.VO.getId();
	public static final String COUNT_CATEGORY_ID_FO = CountCategory.FO.getId();
	public static final String COUNT_CATEGORY_ID_VF = CountCategory.VF.getId();
	public static final boolean ELECTRONIC_MARK_OFFS_TRUE = true;

	private GeneralMockups() {
		// no instances allowed
	}

	public static LocalDate localDate(final String dateString) {
		return LocalDate.parse(dateString);
	}
}
