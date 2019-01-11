package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.valghendelseSti;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_ID_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LandStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(ValghendelseSti valghendelseSti, String landId) throws Exception {
		new LandSti(valghendelseSti, landId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, LAND_ID_11},
				new Object[]{VALGHENDELSE_STI, null},
				new Object[]{VALGHENDELSE_STI, ""},
				new Object[]{VALGHENDELSE_STI, "1"},
				new Object[]{VALGHENDELSE_STI, "111"},
				new Object[]{VALGHENDELSE_STI, "1a"}
		};
	}

	@Test
	public void valghendelseId_gittLandSti_returnererValghendelseId() throws Exception {
		assertThat(LAND_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittLandSti_returnererLandId() throws Exception {
		assertThat(LAND_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneSti_gittFylkeskommuneId_returnerFylkeskommuneSti() throws Exception {
		assertThat(LAND_STI.fylkeskommuneSti(FYLKESKOMMUNE_ID_11)).isEqualTo(FYLKESKOMMUNE_STI);
	}

	@Test
	public void valghendelseSti_gittLandSti_returnererValghendelseSti() throws Exception {
		assertThat(LAND_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void sisteId_gittLandSti_returnerLandId() throws Exception {
		assertThat(LAND_STI.sisteId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void toString_gittLandSti_returnererStringAvSti() throws Exception {
		assertThat(LAND_STI.toString()).isEqualTo("111111.11");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(LandSti landSti, Object other, boolean result) throws Exception {
		assertThat(landSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(LandSti landSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(landSti.hashCode()).isNotZero();
		assertThat(landSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{LAND_STI, LAND_STI, true},
				new Object[]{LAND_STI, new LandSti(VALGHENDELSE_STI, LAND_ID_11), true},
				new Object[]{LAND_STI, new LandSti(VALGHENDELSE_STI, "99"), false},
				new Object[]{LAND_STI, new Object(), false},
				new Object[]{LAND_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikLandSti_returnererTrue() throws Exception {
		assertThat(LAND_STI.likEllerUnder(new LandSti(VALGHENDELSE_STI, LAND_ID_11))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderValghendelseSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		ValghendelseSti valghendelseSti = valghendelseSti();
		LandSti landSti = new LandSti(valghendelseSti, LAND_ID_11);
		when(valghendelseSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(landSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderValghendelseSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		ValghendelseSti valghendelseSti = valghendelseSti();
		LandSti landSti = new LandSti(valghendelseSti, LAND_ID_11);
		when(valghendelseSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(landSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
