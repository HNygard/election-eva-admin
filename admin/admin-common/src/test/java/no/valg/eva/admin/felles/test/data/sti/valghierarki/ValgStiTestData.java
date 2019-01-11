package no.valg.eva.admin.felles.test.data.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI_111111_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_ID_12;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_ID_13;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;

public final class ValgStiTestData {
	public static final ValgSti VALG_STI_111111_11_11 = new ValgSti(VALGGRUPPE_STI_111111_11, VALG_ID_11);
	public static final ValgSti VALG_STI_111111_11_12 = new ValgSti(VALGGRUPPE_STI_111111_11, VALG_ID_12);
	public static final ValgSti VALG_STI_111111_11_13 = new ValgSti(VALGGRUPPE_STI_111111_11, VALG_ID_13);
	public static final ValgSti VALG_STI = VALG_STI_111111_11_11;

	private ValgStiTestData() {
	}

	public static ValgSti valgSti() {
		return mock(ValgSti.class, RETURNS_DEEP_STUBS);
	}
}
