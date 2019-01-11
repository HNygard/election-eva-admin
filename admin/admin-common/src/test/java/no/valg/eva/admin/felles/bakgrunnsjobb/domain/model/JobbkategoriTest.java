package no.valg.eva.admin.felles.bakgrunnsjobb.domain.model;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.CONFIGURATION_DOWNLOAD;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.COUNT_UPLOAD;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.DELETE_VOTERS;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.ELECTORAL_ROLL;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.ELECTORAL_ROLL_DOWNLOAD;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VALGKORTUNDERLAG;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VOTER_NUMBER;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JobbkategoriTest {
	
	@Test(dataProvider = "kategorimapping")
	public void fromAccessPath_konvertererFraGamleKategorier(String gammelTilgangssti, Jobbkategori forventetEnumverdi) {
		Assertions.assertThat(Jobbkategori.fromAccessPath(gammelTilgangssti)).isEqualTo(forventetEnumverdi);
	}

	@DataProvider
	private Object[][] kategorimapping() {
		return new Object[][]{
			{ "e.count.upload", COUNT_UPLOAD },
			{ "e.configuration.download", CONFIGURATION_DOWNLOAD },
			{ "e.delete.voters", DELETE_VOTERS },
			{ "e.batch.electoral_roll", ELECTORAL_ROLL },
			{ "e.electoral_roll.download", ELECTORAL_ROLL_DOWNLOAD },
			{ "e.batch.voter_number", VOTER_NUMBER },
			{ "e.batch.valgkortunderlag", VALGKORTUNDERLAG },
		};
	}

}
