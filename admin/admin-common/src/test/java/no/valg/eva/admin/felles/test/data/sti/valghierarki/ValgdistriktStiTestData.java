package no.valg.eva.admin.felles.test.data.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_ID_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_ID_111112;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_ID_111113;

import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;

public final class ValgdistriktStiTestData {
	public static final ValgdistriktSti VALGDISTRIKT_STI_111111_11_11_111111 = new ValgdistriktSti(VALG_STI_111111_11_11, VALGDISTRIKT_ID_111111);
	public static final ValgdistriktSti VALGDISTRIKT_STI_111111_11_11_111112 = new ValgdistriktSti(VALG_STI_111111_11_11, VALGDISTRIKT_ID_111112);
	public static final ValgdistriktSti VALGDISTRIKT_STI_111111_11_11_111113 = new ValgdistriktSti(VALG_STI_111111_11_11, VALGDISTRIKT_ID_111113);
	public static final ValgdistriktSti VALGDISTRIKT_STI = VALGDISTRIKT_STI_111111_11_11_111111;

	private ValgdistriktStiTestData() {
	}
}
