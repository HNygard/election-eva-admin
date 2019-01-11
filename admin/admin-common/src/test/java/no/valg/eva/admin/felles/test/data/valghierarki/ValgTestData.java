package no.valg.eva.admin.felles.test.data.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI_111111_11_12;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI_111111_11_13;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_11;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.Valg;

public final class ValgTestData {
	public static final String VALG_ID_11 = "11";
	public static final String VALG_ID_12 = "12";
	public static final String VALG_ID_13 = "13";
	public static final String VALG_NAVN_111111_11_11 = "VALG_111111_11_11";
	public static final String VALG_NAVN_111111_11_12 = "VALG_111111_11_12";
	public static final String VALG_NAVN_111111_11_13 = "VALG_111111_11_13";
	public static final Valg VALG_111111_11_11 = valg(VALG_STI_111111_11_11, VALG_NAVN_111111_11_11, FYLKESKOMMUNE);
	public static final Valg VALG_111111_11_12 = valg(VALG_STI_111111_11_12, VALG_NAVN_111111_11_12, KOMMUNE);
	public static final Valg VALG_111111_11_13 = valg(VALG_STI_111111_11_13, VALG_NAVN_111111_11_13, BYDEL);

	private ValgTestData() {
	}

	public static Valg valg() {
		return mock(Valg.class, RETURNS_DEEP_STUBS);
	}

	public static Valg valg(ValggeografiNivaa valggeografiNivaa) {
		Valg valg = valg();
		when(valg.valggeografiNivaa()).thenReturn(valggeografiNivaa);
		return valg;
	}

	public static Valg valg(ValgSti sti, String navn, ValggeografiNivaa valggeografiNivaa) {
		return new Valg(sti, navn, valggeografiNivaa, true, VALGGRUPPE_NAVN_111111_11);
	}

	@SuppressWarnings("unchecked")
	public static List<Valg> flereValg() {
		return mock(List.class);
	}
	
}
