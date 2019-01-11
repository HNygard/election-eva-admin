package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class BydelTest {
	@Test
	public void nivaa_gittBydel_returnerNivaa() throws Exception {
		assertThat(new Bydel(BYDEL_STI, "NAVN", false).nivaa()).isEqualTo(BYDEL);
	}
}
