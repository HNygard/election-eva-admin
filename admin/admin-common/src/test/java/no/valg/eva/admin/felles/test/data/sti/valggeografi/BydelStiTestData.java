package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111112;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111113;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;

public final class BydelStiTestData {
	public static final BydelSti BYDEL_STI_111111_11_11_1111_111111 = new BydelSti(KOMMUNE_STI_111111_11_11_1111, BYDEL_ID_111111);
	public static final BydelSti BYDEL_STI_111111_11_11_1111_111112 = new BydelSti(KOMMUNE_STI_111111_11_11_1111, BYDEL_ID_111112);
	public static final BydelSti BYDEL_STI_111111_11_11_1111_111113 = new BydelSti(KOMMUNE_STI_111111_11_11_1111, BYDEL_ID_111113);
	public static final BydelSti BYDEL_STI = BYDEL_STI_111111_11_11_1111_111111;

	private BydelStiTestData() {
	}

	public static BydelSti bydelSti() {
		return mock(BydelSti.class, RETURNS_DEEP_STUBS);
	}
}
