package no.valg.eva.admin.felles.valggeografi.model;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1111;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class StemmekretsTest {
	@Test
	public void nivaa_gittStemmekrets_returnerNivaa() throws Exception {
		Stemmekrets stemmekrets = new Stemmekrets(
				STEMMEKRETS_STI, "NAVN", false, FYLKESKOMMUNE_NAVN_111111_11_11, KOMMUNE_NAVN_111111_11_11_1111, BYDEL_NAVN_111111_11_11_1111_111111);
		assertThat(stemmekrets.nivaa()).isEqualTo(STEMMEKRETS);
	}
}
