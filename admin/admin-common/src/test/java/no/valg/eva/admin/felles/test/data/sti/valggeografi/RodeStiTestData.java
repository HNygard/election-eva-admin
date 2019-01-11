package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_ID_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_ID_13;

import no.valg.eva.admin.felles.sti.valggeografi.RodeSti;

public final class RodeStiTestData {
	public static final RodeSti RODE_STI_111111_11_11_1111_111111_1111_1111_11 = new RodeSti(STEMMESTED_STI_111111_11_11_1111_111111_1111_1111, RODE_ID_11);
	public static final RodeSti RODE_STI_111111_11_11_1111_111111_1111_1111_12 = new RodeSti(STEMMESTED_STI_111111_11_11_1111_111111_1111_1111, RODE_ID_12);
	public static final RodeSti RODE_STI_111111_11_11_1111_111111_1111_1111_13 = new RodeSti(STEMMESTED_STI_111111_11_11_1111_111111_1111_1111, RODE_ID_13);
	public static final RodeSti RODE_STI = RODE_STI_111111_11_11_1111_111111_1111_1111_11;

	private RodeStiTestData() {
	}
}
