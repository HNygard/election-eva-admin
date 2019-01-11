package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.bydelSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StemmekretsStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(BydelSti bydelSti, String stemmekretsId) throws Exception {
		new StemmekretsSti(bydelSti, stemmekretsId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, STEMMEKRETS_ID_1111},
				new Object[]{BYDEL_STI, null},
				new Object[]{BYDEL_STI, ""},
				new Object[]{BYDEL_STI, "111"},
				new Object[]{BYDEL_STI, "11111"},
				new Object[]{BYDEL_STI, "111a"}
		};
	}

	@Test
	public void valghendelseId_gittStemmekretsSti_returnerValghendelseId() throws Exception {
		assertThat(STEMMEKRETS_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittStemmekretsSti_returnerLandId() throws Exception {
		assertThat(STEMMEKRETS_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneId_gittStemmekretsSti_returnererFylkesKommuneId() throws Exception {
		assertThat(STEMMEKRETS_STI.fylkeskommuneId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void kommuneId_gittStemmekretsSti_returnererKommuneId() throws Exception {
		assertThat(STEMMEKRETS_STI.kommuneId()).isEqualTo(KOMMUNE_ID_1111);
	}

	@Test
	public void bydelId_gittStemmekretsSti_returnerBydelId() throws Exception {
		assertThat(STEMMEKRETS_STI.bydelId()).isEqualTo(BYDEL_ID_111111);
	}

	@Test
	public void stemmekretsId_gittStemmekretsSti_returnerStemmekretsSti() throws Exception {
		assertThat(STEMMEKRETS_STI.stemmekretsId()).isEqualTo(STEMMEKRETS_ID_1111);
	}

	@Test
	public void stemmestedSti_gittStemmestedId_returnerStemmestedSti() throws Exception {
		assertThat(STEMMEKRETS_STI.stemmestedSti(STEMMESTED_ID_1111)).isEqualTo(STEMMESTED_STI);
	}

	@Test
	public void valghendelseSti_gittStemmekretsSti_returnerValghendelseSti() throws Exception {
		assertThat(STEMMEKRETS_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void landSti_gittStemmekretsSti_returnererLandSti() {
		assertThat(STEMMEKRETS_STI.landSti()).isEqualTo(LAND_STI);
	}

	@Test
	public void fylkeskommuneSti_gittStemmekretsSti_returnererFylkeskommuneSti() {
		assertThat(STEMMEKRETS_STI.fylkeskommuneSti()).isEqualTo(FYLKESKOMMUNE_STI);
	}

	@Test
	public void kommuneSti_gittStemmekretsSti_returnererKommuneSti() {
		assertThat(STEMMEKRETS_STI.kommuneSti()).isEqualTo(KOMMUNE_STI);
	}

	@Test
	public void bydelSti_gittStemmekretsSti_returnererBydelSti() {
		assertThat(STEMMEKRETS_STI.bydelSti()).isEqualTo(BYDEL_STI);
	}

	@Test
	public void sisteId_gittStemmekretsSti_stemmekretsId() throws Exception {
		assertThat(STEMMEKRETS_STI.sisteId()).isEqualTo(STEMMEKRETS_ID_1111);
	}

	@Test
	public void toString_gittStemmekretsSti_returnerStringAvSti() throws Exception {
		assertThat(STEMMEKRETS_STI.toString()).isEqualTo("111111.11.11.1111.111111.1111");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(StemmekretsSti stemmekretsSti, Object other, boolean result) throws Exception {
		assertThat(stemmekretsSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(StemmekretsSti stemmekretsSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(stemmekretsSti.hashCode()).isNotZero();
		assertThat(stemmekretsSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{STEMMEKRETS_STI, STEMMEKRETS_STI, true},
				new Object[]{STEMMEKRETS_STI, new StemmekretsSti(BYDEL_STI, STEMMEKRETS_ID_1111), true},
				new Object[]{STEMMEKRETS_STI, new StemmekretsSti(BYDEL_STI, "9999"), false},
				new Object[]{STEMMEKRETS_STI, new Object(), false},
				new Object[]{STEMMEKRETS_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikStemmekretsSti_returnererTrue() throws Exception {
		assertThat(STEMMEKRETS_STI.likEllerUnder(new StemmekretsSti(BYDEL_STI, STEMMEKRETS_ID_1111))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderBydelSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		BydelSti bydelSti = bydelSti();
		StemmekretsSti stemmekretsSti = new StemmekretsSti(bydelSti, STEMMESTED_ID_1111);
		when(bydelSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(stemmekretsSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderBydelSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		BydelSti bydelSti = bydelSti();
		StemmekretsSti stemmekretsSti = new StemmekretsSti(bydelSti, STEMMESTED_ID_1111);
		when(bydelSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(stemmekretsSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
