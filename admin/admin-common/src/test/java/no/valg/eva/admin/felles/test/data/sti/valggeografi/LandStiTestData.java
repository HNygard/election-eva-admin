package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_47;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.LandSti;

public final class LandStiTestData {
	public static final LandSti LAND_STI_111111_11 = new LandSti(VALGHENDELSE_STI_111111, LAND_ID_11);
	public static final LandSti LAND_STI_111111_47 = new LandSti(VALGHENDELSE_STI_111111, LAND_ID_47);
	public static final LandSti LAND_STI = LAND_STI_111111_11;

	private LandStiTestData() {
	}

	public static LandSti landSti() {
		return mock(LandSti.class, RETURNS_DEEP_STUBS);
	}
}
