package no.valg.eva.admin.felles.test.data.valggeografi;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1111;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.List;

import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;

public final class StemmekretsTestData {
	public static final String STEMMEKRETS_ID_1111 = "1111";
	public static final String STEMMEKRETS_ID_1112 = "1112";
	public static final String STEMMEKRETS_ID_1113 = "1113";
	public static final String STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111 = "STEMMEKRETS_111111_11_11_1111_111111_1111";
	public static final String STEMMEKRETS_NAVN_111111_11_11_1111_111111_1112 = "STEMMEKRETS_111111_11_11_1111_111111_1112";
	public static final String STEMMEKRETS_NAVN_111111_11_11_1111_111111_1113 = "STEMMEKRETS_111111_11_11_1111_111111_1113";
	public static final Stemmekrets STEMMEKRETS_111111_11_11_1111_111111_1111 =
			stemmekrets(STEMMEKRETS_STI_111111_11_11_1111_111111_1111, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111);
	public static final Stemmekrets STEMMEKRETS_111111_11_11_1111_111111_1112 =
			stemmekrets(STEMMEKRETS_STI_111111_11_11_1111_111111_1112, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1112);
	public static final Stemmekrets STEMMEKRETS_111111_11_11_1111_111111_1113 =
			stemmekrets(STEMMEKRETS_STI_111111_11_11_1111_111111_1113, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1113);
	public static final List<Stemmekrets> STEMMEKRETSER_111111_11_11_1111_111111_111X =
			asList(STEMMEKRETS_111111_11_11_1111_111111_1111, STEMMEKRETS_111111_11_11_1111_111111_1112, STEMMEKRETS_111111_11_11_1111_111111_1113);

	private StemmekretsTestData() {
	}

	public static Stemmekrets stemmekrets() {
		return mock(Stemmekrets.class, RETURNS_DEEP_STUBS);
	}

	private static Stemmekrets stemmekrets(StemmekretsSti sti, String navn) {
		return new Stemmekrets(sti, navn, false, FYLKESKOMMUNE_NAVN_111111_11_11, KOMMUNE_NAVN_111111_11_11_1111, BYDEL_NAVN_111111_11_11_1111_111111);
	}
}
