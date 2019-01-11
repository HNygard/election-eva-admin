package no.valg.eva.admin.felles.valghierarki.model;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ValghendelseTest {
	@Test
	public void nivaa_gittValghendelse_returnerNivaa() throws Exception {
		assertThat(new Valghendelse(VALGHENDELSE_STI, "NAVN").nivaa()).isEqualTo(VALGHENDELSE);
	}
}
