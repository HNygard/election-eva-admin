package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class FylkeskommuneTest {
	@Test
	public void nivaa_gittFylkeskommune_returnerNivaa() throws Exception {
		assertThat(new Fylkeskommune(FYLKESKOMMUNE_STI, "NAVN").nivaa()).isEqualTo(FYLKESKOMMUNE);
	}
}
