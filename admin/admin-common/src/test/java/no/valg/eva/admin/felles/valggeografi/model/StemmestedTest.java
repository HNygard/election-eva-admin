package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class StemmestedTest {
	@Test
	public void nivaa_gittStemmested_returnerNivaa() throws Exception {
		assertThat(new Stemmested(STEMMESTED_STI, "NAVN", false).nivaa()).isEqualTo(STEMMESTED);
	}
}
