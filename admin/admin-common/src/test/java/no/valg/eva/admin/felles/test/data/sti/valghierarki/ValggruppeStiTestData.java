package no.valg.eva.admin.felles.test.data.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_ID_12;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_ID_13;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;

public final class ValggruppeStiTestData {
	public static final ValggruppeSti VALGGRUPPE_STI_111111_11 = new ValggruppeSti(VALGHENDELSE_STI_111111, VALGGRUPPE_ID_11);
	public static final ValggruppeSti VALGGRUPPE_STI_111111_12 = new ValggruppeSti(VALGHENDELSE_STI_111111, VALGGRUPPE_ID_12);
	public static final ValggruppeSti VALGGRUPPE_STI_111111_13 = new ValggruppeSti(VALGHENDELSE_STI_111111, VALGGRUPPE_ID_13);
	public static final ValggruppeSti VALGGRUPPE_STI = VALGGRUPPE_STI_111111_11;

	private ValggruppeStiTestData() {
	}

	public static ValggruppeSti valggruppeSti() {
		return mock(ValggruppeSti.class, RETURNS_DEEP_STUBS);
	}
}
