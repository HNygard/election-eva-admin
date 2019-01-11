package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class RodeTest {
	@Test
	public void nivaa_gittRode_returnerNivaa() throws Exception {
		assertThat(new Rode(RODE_STI, "NAVN").nivaa()).isEqualTo(RODE);
	}
}
