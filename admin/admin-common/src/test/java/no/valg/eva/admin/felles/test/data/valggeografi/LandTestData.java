package no.valg.eva.admin.felles.test.data.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI_111111_11;

import no.valg.eva.admin.felles.valggeografi.model.Land;

public final class LandTestData {
	public static final String LAND_ID_11 = "11";
	public static final String LAND_ID_47 = "47";
	public static final String LAND_NAVN_111111_11 = "LAND_111111_11";
	public static final Land LAND_111111_11 = new Land(LAND_STI_111111_11, LAND_NAVN_111111_11);

	private LandTestData() {
	}
}
