package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class LandTest {
	@Test
	public void nivaa_gittLand_returnerNivaa() throws Exception {
		assertThat(new Land(LAND_STI, "NAVN").nivaa()).isEqualTo(LAND);
	}
}
