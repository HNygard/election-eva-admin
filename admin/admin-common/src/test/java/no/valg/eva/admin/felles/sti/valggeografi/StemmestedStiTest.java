package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.stemmekretsSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StemmestedStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(StemmekretsSti stemmekretsSti, String stemmestedId) throws Exception {
		new StemmestedSti(stemmekretsSti, stemmestedId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, STEMMESTED_ID_1111},
				new Object[]{STEMMEKRETS_STI, null},
				new Object[]{STEMMEKRETS_STI, ""},
				new Object[]{STEMMEKRETS_STI, "111"},
				new Object[]{STEMMEKRETS_STI, "11111"},
				new Object[]{STEMMEKRETS_STI, "111a"}
		};
	}

	@Test
	public void valghendelseId_gittStemmestedSti_returnerValghendelseId() throws Exception {
		assertThat(STEMMESTED_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittStemmestedSti_returnerLandId() throws Exception {
		assertThat(STEMMESTED_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneId_gittStemmestedSti_returnererFylkesKommuneId() throws Exception {
		assertThat(STEMMESTED_STI.fylkeskommuneId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void kommuneId_gittStemmestedSti_returnererKommuneId() throws Exception {
		assertThat(STEMMESTED_STI.kommuneId()).isEqualTo(KOMMUNE_ID_1111);
	}

	@Test
	public void bydelId_gittStemmestedSti_returnerBydelId() throws Exception {
		assertThat(STEMMESTED_STI.bydelId()).isEqualTo(BYDEL_ID_111111);
	}

	@Test
	public void stemmekretsId_gittStemmestedSti_returnerStemmekretsId() throws Exception {
		assertThat(STEMMESTED_STI.stemmekretsId()).isEqualTo(STEMMEKRETS_ID_1111);
	}

	@Test
	public void stemmestedId_gittStemmestedSti_returnerStemmestedId() throws Exception {
		assertThat(STEMMESTED_STI.stemmestedId()).isEqualTo(STEMMESTED_ID_1111);
	}

	@Test
	public void rodeSti_gittRodeId_returnerRodeSti() throws Exception {
		assertThat(STEMMESTED_STI.rodeSti(RODE_ID_11)).isEqualTo(RODE_STI);
	}

	@Test
	public void valghendelseSti_gittStemmestedSti_returnerValghendelseSti() throws Exception {
		assertThat(STEMMESTED_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void landSti_gittStemmestedSti_returnererLandSti() {
		assertThat(STEMMESTED_STI.landSti()).isEqualTo(LAND_STI);
	}

	@Test
	public void fylkeskommuneSti_gittStemmestedSti_returnererFylkeskommuneSti() {
		assertThat(STEMMESTED_STI.fylkeskommuneSti()).isEqualTo(FYLKESKOMMUNE_STI);
	}

	@Test
	public void kommuneSti_gittStemmestedSti_returnererKommuneSti() {
		assertThat(STEMMESTED_STI.kommuneSti()).isEqualTo(KOMMUNE_STI);
	}

	@Test
	public void bydelSti_gittStemmestedSti_returnererBydelSti() {
		assertThat(STEMMESTED_STI.bydelSti()).isEqualTo(BYDEL_STI);
	}

	@Test
	public void stemmekretsSti_gittStemmestedSti_returnererStemmekretsSti() {
		assertThat(STEMMESTED_STI.stemmekretsSti()).isEqualTo(STEMMEKRETS_STI);
	}

	@Test
	public void sisteId_gittStemmestedSti_stemmestedId() throws Exception {
		assertThat(STEMMESTED_STI.sisteId()).isEqualTo(STEMMESTED_ID_1111);
	}

	@Test
	public void toString_gittStemmestedSti_returnerStringAvSti() throws Exception {
		assertThat(STEMMESTED_STI.toString()).isEqualTo("111111.11.11.1111.111111.1111.1111");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(StemmestedSti stemmestedSti, Object other, boolean result) throws Exception {
		assertThat(stemmestedSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(StemmestedSti stemmestedSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(stemmestedSti.hashCode()).isNotZero();
		assertThat(stemmestedSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{STEMMESTED_STI, STEMMESTED_STI, true},
				new Object[]{STEMMESTED_STI, new StemmestedSti(STEMMEKRETS_STI, STEMMESTED_ID_1111), true},
				new Object[]{STEMMESTED_STI, new StemmestedSti(STEMMEKRETS_STI, "9999"), false},
				new Object[]{STEMMESTED_STI, new Object(), false},
				new Object[]{STEMMESTED_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikStemmestedSti_returnererTrue() throws Exception {
		assertThat(STEMMESTED_STI.likEllerUnder(new StemmestedSti(STEMMEKRETS_STI, STEMMESTED_ID_1111))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderStemmekretsSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		StemmekretsSti stemmekretsSti = stemmekretsSti();
		StemmestedSti stemmestedSti = new StemmestedSti(stemmekretsSti, STEMMESTED_ID_1111);
		when(stemmekretsSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(stemmestedSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderStemmekretsSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		StemmekretsSti stemmekretsSti = stemmekretsSti();
		StemmestedSti stemmestedSti = new StemmestedSti(stemmekretsSti, STEMMESTED_ID_1111);
		when(stemmekretsSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(stemmestedSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
