package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI_111111_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI_111111_47;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_13;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;

public final class FylkeskommuneStiTestData {
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI_111111_11_11 = new FylkeskommuneSti(LAND_STI_111111_11, FYLKESKOMMUNE_ID_11);
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI_111111_11_12 = new FylkeskommuneSti(LAND_STI_111111_11, FYLKESKOMMUNE_ID_12);
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI_111111_11_13 = new FylkeskommuneSti(LAND_STI_111111_11, FYLKESKOMMUNE_ID_13);
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI_111111_47_11 = new FylkeskommuneSti(LAND_STI_111111_47, FYLKESKOMMUNE_ID_11);
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI_111111_47_12 = new FylkeskommuneSti(LAND_STI_111111_47, FYLKESKOMMUNE_ID_12);
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI_111111_47_13 = new FylkeskommuneSti(LAND_STI_111111_47, FYLKESKOMMUNE_ID_13);
	public static final FylkeskommuneSti FYLKESKOMMUNE_STI = FYLKESKOMMUNE_STI_111111_11_11;

	private FylkeskommuneStiTestData() {
	}

	public static FylkeskommuneSti fylkeskommuneSti() {
		return mock(FylkeskommuneSti.class, RETURNS_DEEP_STUBS);
	}
}
