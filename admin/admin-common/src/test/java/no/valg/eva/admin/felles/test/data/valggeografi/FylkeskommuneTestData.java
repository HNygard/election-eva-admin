package no.valg.eva.admin.felles.test.data.valggeografi;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_12;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_13;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_47_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_47_12;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_47_13;

import java.util.List;

import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;

public final class FylkeskommuneTestData {
	public static final String FYLKESKOMMUNE_ID_11 = "11";
	public static final String FYLKESKOMMUNE_ID_12 = "12";
	public static final String FYLKESKOMMUNE_ID_13 = "13";
	public static final String FYLKESKOMMUNE_NAVN_111111_11_11 = "FYLKESKOMMUNE_111111_11_11";
	public static final String FYLKESKOMMUNE_NAVN_111111_11_12 = "FYLKESKOMMUNE_111111_11_12";
	public static final String FYLKESKOMMUNE_NAVN_111111_11_13 = "FYLKESKOMMUNE_111111_11_13";
	public static final String FYLKESKOMMUNE_NAVN_111111_47_11 = "FYLKESKOMMUNE_111111_47_11";
	public static final String FYLKESKOMMUNE_NAVN_111111_47_12 = "FYLKESKOMMUNE_111111_47_12";
	public static final String FYLKESKOMMUNE_NAVN_111111_47_13 = "FYLKESKOMMUNE_111111_47_13";
	public static final Fylkeskommune FYLKESKOMMUNE_111111_11_11 = fylkeskommune(FYLKESKOMMUNE_STI_111111_11_11, FYLKESKOMMUNE_NAVN_111111_11_11);
	public static final Fylkeskommune FYLKESKOMMUNE_111111_11_12 = fylkeskommune(FYLKESKOMMUNE_STI_111111_11_12, FYLKESKOMMUNE_NAVN_111111_11_12);
	public static final Fylkeskommune FYLKESKOMMUNE_111111_11_13 = fylkeskommune(FYLKESKOMMUNE_STI_111111_11_13, FYLKESKOMMUNE_NAVN_111111_11_13);
	public static final Fylkeskommune FYLKESKOMMUNE_111111_47_11 = fylkeskommune(FYLKESKOMMUNE_STI_111111_47_11, FYLKESKOMMUNE_NAVN_111111_47_11);
	public static final Fylkeskommune FYLKESKOMMUNE_111111_47_12 = fylkeskommune(FYLKESKOMMUNE_STI_111111_47_12, FYLKESKOMMUNE_NAVN_111111_47_12);
	public static final Fylkeskommune FYLKESKOMMUNE_111111_47_13 = fylkeskommune(FYLKESKOMMUNE_STI_111111_47_13, FYLKESKOMMUNE_NAVN_111111_47_13);
	public static final List<Fylkeskommune> FYLKESKOMMUNER_111111_11_1X = asList(FYLKESKOMMUNE_111111_11_11, FYLKESKOMMUNE_111111_11_12, FYLKESKOMMUNE_111111_11_13);

	private FylkeskommuneTestData() {
	}

	private static Fylkeskommune fylkeskommune(FylkeskommuneSti sti, String navn) {
		return new Fylkeskommune(sti, navn);
	}
}
