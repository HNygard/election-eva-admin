package no.valg.eva.admin.felles.test.data.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI_111111_11_11_1111_111111_1111_1112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI_111111_11_11_1111_111111_1111_1113;

import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;

public final class StemmestedTestData {
	public static final String STEMMESTED_ID_1111 = "1111";
	public static final String STEMMESTED_ID_1112 = "1112";
	public static final String STEMMESTED_ID_1113 = "1113";
	public static final String STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1111 = "STEMMESTED_111111_11_11_1111_111111_1111_1111";
	public static final String STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1112 = "STEMMESTED_111111_11_11_1111_111111_1111_1112";
	public static final String STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1113 = "STEMMESTED_111111_11_11_1111_111111_1111_1113";
	public static final Stemmested STEMMESTED_111111_11_11_1111_111111_1111_1111 =
			stemmested(STEMMESTED_STI_111111_11_11_1111_111111_1111_1111, STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1111);
	public static final Stemmested STEMMESTED_111111_11_11_1111_111111_1111_1112 =
			stemmested(STEMMESTED_STI_111111_11_11_1111_111111_1111_1112, STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1112);
	public static final Stemmested STEMMESTED_111111_11_11_1111_111111_1111_1113 =
			stemmested(STEMMESTED_STI_111111_11_11_1111_111111_1111_1113, STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1113);

	private StemmestedTestData() {
	}

	private static Stemmested stemmested(StemmestedSti sti, String navn) {
		return new Stemmested(sti, navn, true);
	}
}
