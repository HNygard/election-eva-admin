package no.valg.eva.admin.felles.valghierarki.model;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ValgdistriktTest {
	@Test
	public void nivaa_gittValgdistrikt_returnerNivaa() throws Exception {
		assertThat(new Valgdistrikt(VALGDISTRIKT_STI, "NAVN", KOMMUNE).nivaa()).isEqualTo(VALGDISTRIKT);
	}
}
