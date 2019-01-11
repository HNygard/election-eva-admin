package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class KommuneTest {
	@Test
	public void nivaa_gittKommune_returnerNivaa() throws Exception {
		assertThat(new Kommune(KOMMUNE_STI, "NAVN", false).nivaa()).isEqualTo(KOMMUNE);
	}
}
