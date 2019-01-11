package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.kommuneSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BydelStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(KommuneSti kommuneSti, String bydelId) throws Exception {
		new BydelSti(kommuneSti, bydelId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, BYDEL_ID_111111},
				new Object[]{KOMMUNE_STI, null},
				new Object[]{KOMMUNE_STI, ""},
				new Object[]{KOMMUNE_STI, "11111"},
				new Object[]{KOMMUNE_STI, "1111111"},
				new Object[]{KOMMUNE_STI, "11111a"}
		};
	}

	@Test
	public void valghendelseId_gittBydelSti_returnerValghendelseId() throws Exception {
		assertThat(BYDEL_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittBydelSti_returnerLandId() throws Exception {
		assertThat(BYDEL_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneId_gittBydelSti_returnererFylkesKommuneId() throws Exception {
		assertThat(BYDEL_STI.fylkeskommuneId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void kommuneId_gittBydelSti_returnererKommuneId() throws Exception {
		assertThat(BYDEL_STI.kommuneId()).isEqualTo(KOMMUNE_ID_1111);
	}

	@Test
	public void bydelId_gittBydelSti_returnerBydelId() throws Exception {
		assertThat(BYDEL_STI.bydelId()).isEqualTo(BYDEL_ID_111111);
	}

	@Test
	public void stemmekretsSti_gittStemmekretsId_returnerStemmekretsSti() throws Exception {
		assertThat(BYDEL_STI.stemmekretsSti(STEMMEKRETS_ID_1111)).isEqualTo(STEMMEKRETS_STI);
	}

	@Test
	public void valghendelseSti_gittBydelSti_returnerValghendelseSti() throws Exception {
		assertThat(BYDEL_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void landSti_gittBydelSti_returnererLandSti() {
		assertThat(BYDEL_STI.landSti()).isEqualTo(LAND_STI);
	}

	@Test
	public void fylkeskommuneSti_gittBydelSti_returnererFylkeskommuneSti() {
		assertThat(BYDEL_STI.fylkeskommuneSti()).isEqualTo(FYLKESKOMMUNE_STI);
	}

	@Test
	public void kommuneSti_gittBydelSti_returnererKommuneSti() {
		assertThat(BYDEL_STI.kommuneSti()).isEqualTo(KOMMUNE_STI);
	}

	@Test
	public void sisteId_gittBydelSti_bydelId() throws Exception {
		assertThat(BYDEL_STI.sisteId()).isEqualTo(BYDEL_ID_111111);
	}

	@Test
	public void toString_gittBydelSti_returnerStringAvSti() throws Exception {
		assertThat(BYDEL_STI.toString()).isEqualTo("111111.11.11.1111.111111");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(BydelSti bydelSti, Object other, boolean result) throws Exception {
		assertThat(bydelSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(BydelSti bydelSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(bydelSti.hashCode()).isNotZero();
		assertThat(bydelSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{BYDEL_STI, BYDEL_STI, true},
				new Object[]{BYDEL_STI, new BydelSti(KOMMUNE_STI, BYDEL_ID_111111), true},
				new Object[]{BYDEL_STI, new BydelSti(KOMMUNE_STI, "999999"), false},
				new Object[]{BYDEL_STI, new Object(), false},
				new Object[]{BYDEL_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikBydelSti_returnererTrue() throws Exception {
		assertThat(BYDEL_STI.likEllerUnder(new BydelSti(KOMMUNE_STI, BYDEL_ID_111111))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderKommuneSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		KommuneSti kommuneSti = kommuneSti();
		BydelSti bydelSti = new BydelSti(kommuneSti, BYDEL_ID_111111);
		when(kommuneSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(bydelSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderKommuneSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		KommuneSti kommuneSti = kommuneSti();
		BydelSti bydelSti = new BydelSti(kommuneSti, BYDEL_ID_111111);
		when(kommuneSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(bydelSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
