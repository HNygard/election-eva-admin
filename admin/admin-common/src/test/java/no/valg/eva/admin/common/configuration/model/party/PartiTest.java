package no.valg.eva.admin.common.configuration.model.party;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.AreaPath;

import org.testng.annotations.Test;

public class PartiTest {

	private static final AreaPath EN_AREA_PATH = AreaPath.from("150001.47.03.0301");

	@Test
	public void leggTilOmraadeHvisLokalt_ikkeLokaltParti_omraadeLeggesIkkeTil() {
		Parti parti = new Parti(Partikategori.STORTING, "V");
		parti.leggTilOmraadeHvisLokalt(EN_AREA_PATH);
		assertThat(parti.getOmrader()).hasSize(0);
	}

	@Test
	public void leggTilOmraadeHvisLokalt_lokaltParti_omraadeLeggesTil() {
		Parti parti = new Parti(Partikategori.LOKALT, "FI");
		parti.leggTilOmraadeHvisLokalt(EN_AREA_PATH);
		assertThat(parti.getOmrader()).hasSize(1);
		assertThat(parti.getOmrader().get(0).getAreaPath()).isEqualTo(EN_AREA_PATH);
	}
}
