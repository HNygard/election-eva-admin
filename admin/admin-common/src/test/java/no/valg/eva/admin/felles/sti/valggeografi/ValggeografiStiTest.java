package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggeografiStiTest {
	@Test(dataProvider = "areaPathOgValggeografiSti")
	public void fra_gittAreaPath_returnererValggeografiSti(AreaPath areaPath, ValggeografiSti valggeografiSti) throws Exception {
		assertThat(ValggeografiSti.fra(areaPath)).isEqualTo(valggeografiSti);
	}

	@Test(dataProvider = "areaPathOgValggeografiSti")
	public void areaPath_gittValggeografiSti_returnererAreaPath(AreaPath areaPath, ValggeografiSti valggeografiSti) throws Exception {
		assertThat(valggeografiSti.areaPath()).isEqualTo(areaPath);
	}

	@DataProvider
	public Object[][] areaPathOgValggeografiSti() {
		return new Object[][]{
				new Object[]{AREA_PATH_111111, VALGHENDELSE_STI},
				new Object[]{AREA_PATH_111111_11, LAND_STI},
				new Object[]{AREA_PATH_111111_11_11, FYLKESKOMMUNE_STI},
				new Object[]{AREA_PATH_111111_11_11_1111, KOMMUNE_STI},
				new Object[]{AREA_PATH_111111_11_11_1111_111111, BYDEL_STI},
				new Object[]{AREA_PATH_111111_11_11_1111_111111_1111, STEMMEKRETS_STI},
				new Object[]{AREA_PATH_111111_11_11_1111_111111_1111_1111, STEMMESTED_STI},
				new Object[]{AREA_PATH_111111_11_11_1111_111111_1111_1111_11, RODE_STI}
		};
	}

	@Test(dataProvider = "stierOgNivaaer")
	public void nivaa_gittSti_returnererRiktigNivaa(ValggeografiSti sti, ValggeografiNivaa nivaa) throws Exception {
		assertThat(sti.nivaa()).isEqualTo(nivaa);
	}

	@DataProvider
	public Object[][] stierOgNivaaer() {
		return new Object[][]{
				new Object[]{VALGHENDELSE_STI, VALGHENDELSE},
				new Object[]{LAND_STI, LAND},
				new Object[]{FYLKESKOMMUNE_STI, FYLKESKOMMUNE},
				new Object[]{KOMMUNE_STI, KOMMUNE},
				new Object[]{BYDEL_STI, BYDEL},
				new Object[]{STEMMEKRETS_STI, STEMMEKRETS},
				new Object[]{STEMMESTED_STI, STEMMESTED},
				new Object[]{RODE_STI, RODE}
		};
	}

	@Test
	public void isValghendelseSti_gittValghendelseSti_returnererTrue() throws Exception {
		assertThat(VALGHENDELSE_STI.isValghendelseSti()).isTrue();
	}

	@Test
	public void isValghendelseSti_gittIkkeValghendelseSti_returnererFalse() throws Exception {
		assertThat(STEMMEKRETS_STI.isValghendelseSti()).isFalse();
	}

	@Test
	public void isStemmekretsSti_gittStemmekretsSti_returnererTrue() throws Exception {
		assertThat(STEMMEKRETS_STI.isStemmekretsSti()).isTrue();
	}

	@Test
	public void isStemmekretsSti_gittIkkeStemmekretsSti_returnererFalse() throws Exception {
		assertThat(VALGHENDELSE_STI.isStemmekretsSti()).isFalse();
	}

	@Test
	public void tilStemmekretsSti_gittStemmekretsSti_returnererStemmekretsSti() throws Exception {
		assertThat(STEMMEKRETS_STI.tilStemmekretsSti()).isSameAs(STEMMEKRETS_STI);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void tilStemmekretsSti_gittIkkeStemmekretsSti_kasterException() throws Exception {
		assertThat(KOMMUNE_STI.tilStemmekretsSti());
	}
}
