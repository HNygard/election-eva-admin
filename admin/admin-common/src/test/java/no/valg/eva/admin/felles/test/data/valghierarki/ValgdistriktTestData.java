package no.valg.eva.admin.felles.test.data.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI_111111_11_11_111112;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI_111111_11_11_111113;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static org.mockito.Mockito.mock;

import java.util.List;

import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;

public final class ValgdistriktTestData {
	public static final String VALGDISTRIKT_ID_111111 = "111111";
	public static final String VALGDISTRIKT_ID_111112 = "111112";
	public static final String VALGDISTRIKT_ID_111113 = "111113";
	public static final String VALGDISTRIKT_NAVN_111111_11_11_111111 = "VALGDISTRIKT_111111_11_11_111111";
	public static final String VALGDISTRIKT_NAVN_111111_11_11_111112 = "VALGDISTRIKT_111111_11_11_111112";
	public static final String VALGDISTRIKT_NAVN_111111_11_11_111113 = "VALGDISTRIKT_111111_11_11_111113";
	public static final Valgdistrikt VALGDISTRIKT_111111_11_11_111111 = valgdistrikt(VALGDISTRIKT_STI_111111_11_11_111111, VALGDISTRIKT_NAVN_111111_11_11_111111);
	public static final Valgdistrikt VALGDISTRIKT_111111_11_11_111112 = valgdistrikt(VALGDISTRIKT_STI_111111_11_11_111112, VALGDISTRIKT_NAVN_111111_11_11_111112);
	public static final Valgdistrikt VALGDISTRIKT_111111_11_11_111113 = valgdistrikt(VALGDISTRIKT_STI_111111_11_11_111113, VALGDISTRIKT_NAVN_111111_11_11_111113);

	private ValgdistriktTestData() {
	}

	private static Valgdistrikt valgdistrikt(ValgdistriktSti sti, String navn) {
		return new Valgdistrikt(sti, navn, FYLKESKOMMUNE);
	}

	@SuppressWarnings("unchecked")
	public static List<Valgdistrikt> valgdistrikter() {
		return mock(List.class);
	}
}
