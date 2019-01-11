package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.landSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_ID_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FylkeskommuneStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(LandSti landSti, String fylkeskommuneId) throws Exception {
		new FylkeskommuneSti(landSti, fylkeskommuneId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, FYLKESKOMMUNE_ID_11},
				new Object[]{LAND_STI, null},
				new Object[]{LAND_STI, ""},
				new Object[]{LAND_STI, "1"},
				new Object[]{LAND_STI, "111"},
				new Object[]{LAND_STI, "1a"}
		};
	}

	@Test
	public void valghendelseId_gittFylkeskommuneSti_returnerValghendelseId() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittFylkeskommuneSti_returnerLandId() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneId_gittFylkeskommuneSti_returnererFylkeskommuneId() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.fylkeskommuneId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void kommuneSti_gittKommuneId_returnerKommuneSti() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.kommuneSti(KOMMUNE_ID_1111)).isEqualTo(KOMMUNE_STI);
	}

	@Test
	public void valghendelseSti_gittFylkeskommuneSti_returnerValghendelseSti() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void landSti_gittFylkeskommuneSti_returnererLandSti() {
		assertThat(FYLKESKOMMUNE_STI.landSti()).isEqualTo(LAND_STI);
	}

	@Test
	public void sisteId_gittFylkeskommuneSti_fylkeskommuneId() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.sisteId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void toString_gittFylkeskommuneSti_returnerStringAvSti() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.toString()).isEqualTo("111111.11.11");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(FylkeskommuneSti fylkeskommuneSti, Object other, boolean result) throws Exception {
		assertThat(fylkeskommuneSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(FylkeskommuneSti fylkeskommuneSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(fylkeskommuneSti.hashCode()).isNotZero();
		assertThat(fylkeskommuneSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{FYLKESKOMMUNE_STI, FYLKESKOMMUNE_STI, true},
				new Object[]{FYLKESKOMMUNE_STI, new FylkeskommuneSti(LAND_STI, FYLKESKOMMUNE_ID_11), true},
				new Object[]{FYLKESKOMMUNE_STI, new FylkeskommuneSti(LAND_STI, "99"), false},
				new Object[]{FYLKESKOMMUNE_STI, new Object(), false},
				new Object[]{FYLKESKOMMUNE_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikFylkeskommuneSti_returnererTrue() throws Exception {
		assertThat(FYLKESKOMMUNE_STI.likEllerUnder(new FylkeskommuneSti(LAND_STI, FYLKESKOMMUNE_ID_11))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderLandSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		LandSti landSti = landSti();
		FylkeskommuneSti fylkeskommuneSti = new FylkeskommuneSti(landSti, FYLKESKOMMUNE_ID_11);
		when(landSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(fylkeskommuneSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderLandSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		LandSti landSti = landSti();
		FylkeskommuneSti fylkeskommuneSti = new FylkeskommuneSti(landSti, FYLKESKOMMUNE_ID_11);
		when(landSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(fylkeskommuneSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
