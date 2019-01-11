package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_ID_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_ID_1113;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;

public final class StemmekretsStiTestData {
	public static final StemmekretsSti STEMMEKRETS_STI_111111_11_11_1111_111111_1111 = new StemmekretsSti(BYDEL_STI_111111_11_11_1111_111111, STEMMEKRETS_ID_1111);
	public static final StemmekretsSti STEMMEKRETS_STI_111111_11_11_1111_111111_1112 = new StemmekretsSti(BYDEL_STI_111111_11_11_1111_111111, STEMMEKRETS_ID_1112);
	public static final StemmekretsSti STEMMEKRETS_STI_111111_11_11_1111_111111_1113 = new StemmekretsSti(BYDEL_STI_111111_11_11_1111_111111, STEMMEKRETS_ID_1113);
	public static final StemmekretsSti STEMMEKRETS_STI = STEMMEKRETS_STI_111111_11_11_1111_111111_1111;

	private StemmekretsStiTestData() {
	}

	public static StemmekretsSti stemmekretsSti() {
		return mock(StemmekretsSti.class, RETURNS_DEEP_STUBS);
	}
}
