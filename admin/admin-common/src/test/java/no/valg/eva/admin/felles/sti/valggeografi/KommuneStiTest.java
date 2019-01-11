package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.fylkeskommuneSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_ID_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KommuneStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(FylkeskommuneSti fylkeskommuneSti, String kommuneId) throws Exception {
		new KommuneSti(fylkeskommuneSti, kommuneId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, KOMMUNE_ID_1111},
				new Object[]{FYLKESKOMMUNE_STI, null},
				new Object[]{FYLKESKOMMUNE_STI, ""},
				new Object[]{FYLKESKOMMUNE_STI, "111"},
				new Object[]{FYLKESKOMMUNE_STI, "11111"},
				new Object[]{FYLKESKOMMUNE_STI, "111a"}
		};
	}

	@Test
	public void valghendelseId_gittKommuneSti_returnerValghendelseId() throws Exception {
		assertThat(KOMMUNE_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittKommuneSti_returnerLandId() throws Exception {
		assertThat(KOMMUNE_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneId_gittKommuneSti_returnererFylkesKommuneId() throws Exception {
		assertThat(KOMMUNE_STI.fylkeskommuneId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void kommuneId_gittKommuneSti_returnererKommuneId() throws Exception {
		assertThat(KOMMUNE_STI.kommuneId()).isEqualTo(KOMMUNE_ID_1111);
	}

	@Test
	public void bydelSti_gittBydelId_returnerBydelSti() throws Exception {
		assertThat(KOMMUNE_STI.bydelSti(BYDEL_ID_111111)).isEqualTo(BYDEL_STI);
	}

	@Test
	public void valghendelseSti_gittKommuneSti_returnerValghendelseSti() throws Exception {
		assertThat(KOMMUNE_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void landSti_gittKommuneSti_returnererLandSti() {
		assertThat(KOMMUNE_STI.landSti()).isEqualTo(LAND_STI);
	}

	@Test
	public void fylkeskommuneSti_gittKommuneSti_returnererFylkeskommuneSti() {
		assertThat(KOMMUNE_STI.fylkeskommuneSti()).isEqualTo(FYLKESKOMMUNE_STI);
	}

	@Test
	public void sisteId_gittKommuneSti_kommuneId() throws Exception {
		assertThat(KOMMUNE_STI.sisteId()).isEqualTo(KOMMUNE_ID_1111);
	}

	@Test
	public void toString_gittKommuneSti_returnerStringAvSti() throws Exception {
		assertThat(KOMMUNE_STI.toString()).isEqualTo("111111.11.11.1111");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(KommuneSti kommuneSti, Object other, boolean result) throws Exception {
		assertThat(kommuneSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(KommuneSti kommuneSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(kommuneSti.hashCode()).isNotZero();
		assertThat(kommuneSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{KOMMUNE_STI, KOMMUNE_STI, true},
				new Object[]{KOMMUNE_STI, new KommuneSti(FYLKESKOMMUNE_STI, KOMMUNE_ID_1111), true},
				new Object[]{KOMMUNE_STI, new KommuneSti(FYLKESKOMMUNE_STI, "9999"), false},
				new Object[]{KOMMUNE_STI, new Object(), false},
				new Object[]{KOMMUNE_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikKommuneSti_returnererTrue() throws Exception {
		assertThat(KOMMUNE_STI.likEllerUnder(new KommuneSti(FYLKESKOMMUNE_STI, KOMMUNE_ID_1111))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderFylkeskommuneSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		FylkeskommuneSti fylkeskommuneSti = fylkeskommuneSti();
		KommuneSti kommuneSti = new KommuneSti(fylkeskommuneSti, KOMMUNE_ID_1111);
		when(fylkeskommuneSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(kommuneSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderFylkeskommuneSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		FylkeskommuneSti fylkeskommuneSti = fylkeskommuneSti();
		KommuneSti kommuneSti = new KommuneSti(fylkeskommuneSti, KOMMUNE_ID_1111);
		when(fylkeskommuneSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(kommuneSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
