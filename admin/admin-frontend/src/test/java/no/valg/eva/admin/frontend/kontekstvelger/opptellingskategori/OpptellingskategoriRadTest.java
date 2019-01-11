package no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class OpptellingskategoriRadTest {
	@Test
	public void getCountCategory_gittConstructorMedCountCategory_returnererCountCategory() throws Exception {
		assertThat(new OpptellingskategoriRad(FO).getCountCategory()).isEqualTo(FO);
	}
}
