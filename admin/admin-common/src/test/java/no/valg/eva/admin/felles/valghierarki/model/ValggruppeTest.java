package no.valg.eva.admin.felles.valghierarki.model;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ValggruppeTest {
	@Test
	public void nivaa_gittValggruppe_returnerNivaa() throws Exception {
		assertThat(new Valggruppe(VALGGRUPPE_STI, "NAVN").nivaa()).isEqualTo(VALGGRUPPE);
	}
}
