package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_ID_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_ID_1113;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;

public final class StemmestedStiTestData {
	public static final StemmestedSti STEMMESTED_STI_111111_11_11_1111_111111_1111_1111 =
			new StemmestedSti(STEMMEKRETS_STI_111111_11_11_1111_111111_1111, STEMMESTED_ID_1111);
	public static final StemmestedSti STEMMESTED_STI_111111_11_11_1111_111111_1111_1112 =
			new StemmestedSti(STEMMEKRETS_STI_111111_11_11_1111_111111_1111, STEMMESTED_ID_1112);
	public static final StemmestedSti STEMMESTED_STI_111111_11_11_1111_111111_1111_1113 =
			new StemmestedSti(STEMMEKRETS_STI_111111_11_11_1111_111111_1111, STEMMESTED_ID_1113);
	public static final StemmestedSti STEMMESTED_STI = STEMMESTED_STI_111111_11_11_1111_111111_1111_1111;

	private StemmestedStiTestData() {
	}

	public static StemmestedSti stemmestedSti() {
		return mock(StemmestedSti.class, RETURNS_DEEP_STUBS);
	}
}
