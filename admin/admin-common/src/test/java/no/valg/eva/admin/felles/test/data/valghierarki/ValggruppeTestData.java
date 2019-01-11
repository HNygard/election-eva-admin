package no.valg.eva.admin.felles.test.data.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI_111111_11;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI_111111_12;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI_111111_13;
import static org.mockito.Mockito.mock;

import java.util.List;

import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;

public final class ValggruppeTestData {
	public static final String VALGGRUPPE_ID_11 = "11";
	public static final String VALGGRUPPE_ID_12 = "12";
	public static final String VALGGRUPPE_ID_13 = "13";
	public static final String VALGGRUPPE_NAVN_111111_11 = "VALGGRUPPE_111111_11";
	public static final String VALGGRUPPE_NAVN_111111_12 = "VALGGRUPPE_111111_12";
	public static final String VALGGRUPPE_NAVN_111111_13 = "VALGGRUPPE_111111_13";
	public static final Valggruppe VALGGRUPPE_111111_11 = valggruppe(VALGGRUPPE_STI_111111_11, VALGGRUPPE_NAVN_111111_11);
	public static final Valggruppe VALGGRUPPE_111111_12 = valggruppe(VALGGRUPPE_STI_111111_12, VALGGRUPPE_NAVN_111111_12);
	public static final Valggruppe VALGGRUPPE_111111_13 = valggruppe(VALGGRUPPE_STI_111111_13, VALGGRUPPE_NAVN_111111_13);

	private ValggruppeTestData() {
	}

	private static Valggruppe valggruppe(ValggruppeSti sti, String navn) {
		return new Valggruppe(sti, navn);
	}

	@SuppressWarnings("unchecked")
	public static List<Valggruppe> valggrupper() {
		return mock(List.class);
	}
}
