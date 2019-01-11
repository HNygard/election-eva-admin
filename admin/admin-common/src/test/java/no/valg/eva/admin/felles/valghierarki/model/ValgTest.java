package no.valg.eva.admin.felles.valghierarki.model;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_11;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ValgTest {
	@Test
	public void nivaa_gittValg_returnerNivaa() throws Exception {
		assertThat(new Valg(VALG_STI, "NAVN", KOMMUNE, true, VALGGRUPPE_NAVN_111111_11).nivaa()).isEqualTo(VALG);
	}
}
