package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1113;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

public final class KommuneStiTestData {
	public static final KommuneSti KOMMUNE_STI_111111_11_11_1111 = new KommuneSti(FYLKESKOMMUNE_STI_111111_11_11, KOMMUNE_ID_1111);
	public static final KommuneSti KOMMUNE_STI_111111_11_11_1112 = new KommuneSti(FYLKESKOMMUNE_STI_111111_11_11, KOMMUNE_ID_1112);
	public static final KommuneSti KOMMUNE_STI_111111_11_11_1113 = new KommuneSti(FYLKESKOMMUNE_STI_111111_11_11, KOMMUNE_ID_1113);
	public static final KommuneSti KOMMUNE_STI = KOMMUNE_STI_111111_11_11_1111;

	private KommuneStiTestData() {
	}

	public static KommuneSti kommuneSti() {
		return mock(KommuneSti.class, RETURNS_DEEP_STUBS);
	}
}
