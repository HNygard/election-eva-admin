package no.valg.eva.admin.felles.test.data.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI_111111_11_11_1111_111111_1111_1111_12;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI_111111_11_11_1111_111111_1111_1111_13;

import no.valg.eva.admin.felles.sti.valggeografi.RodeSti;
import no.valg.eva.admin.felles.valggeografi.model.Rode;

public final class RodeTestData {
	public static final String RODE_ID_11 = "11";
	public static final String RODE_ID_12 = "12";
	public static final String RODE_ID_13 = "13";
	public static final String RODE_NAVN_111111_11_11_1111_111111_1111_1111_11 = "RODE_111111_11_11_1111_111111_1111_1111_11";
	public static final String RODE_NAVN_111111_11_11_1111_111111_1111_1111_12 = "RODE_111111_11_11_1111_111111_1111_1111_12";
	public static final String RODE_NAVN_111111_11_11_1111_111111_1111_1111_13 = "RODE_111111_11_11_1111_111111_1111_1111_13";
	public static final Rode RODE_111111_11_11_1111_111111_1111_1111_11 =
			rode(RODE_STI_111111_11_11_1111_111111_1111_1111_11, RODE_NAVN_111111_11_11_1111_111111_1111_1111_11);
	public static final Rode RODE_111111_11_11_1111_111111_1111_1111_12 =
			rode(RODE_STI_111111_11_11_1111_111111_1111_1111_12, RODE_NAVN_111111_11_11_1111_111111_1111_1111_12);
	public static final Rode RODE_111111_11_11_1111_111111_1111_1111_13 =
			rode(RODE_STI_111111_11_11_1111_111111_1111_1111_13, RODE_NAVN_111111_11_11_1111_111111_1111_1111_13);

	private RodeTestData() {
	}

	private static Rode rode(RodeSti sti, String navn) {
		return new Rode(sti, navn);
	}
}
