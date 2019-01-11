package no.valg.eva.admin.felles.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.RodeStiTestData.RODE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.stemmestedSti;
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

public class RodeStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(StemmestedSti stemmestedSti, String rodeId) throws Exception {
		new RodeSti(stemmestedSti, rodeId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, RODE_ID_11},
				new Object[]{STEMMESTED_STI, null},
				new Object[]{STEMMESTED_STI, ""},
				new Object[]{STEMMESTED_STI, "1"},
				new Object[]{STEMMESTED_STI, "111"},
				new Object[]{STEMMESTED_STI, "1a"}
		};
	}

	@Test
	public void valghendelseId_gittRodeSti_returnerValghendelseId() throws Exception {
		assertThat(RODE_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void landId_gittRodeSti_returnerLandId() throws Exception {
		assertThat(RODE_STI.landId()).isEqualTo(LAND_ID_11);
	}

	@Test
	public void fylkeskommuneId_gittRodeSti_returnererFylkesKommuneId() throws Exception {
		assertThat(RODE_STI.fylkeskommuneId()).isEqualTo(FYLKESKOMMUNE_ID_11);
	}

	@Test
	public void kommuneId_gittRodeSti_returnererKommuneId() throws Exception {
		assertThat(RODE_STI.kommuneId()).isEqualTo(KOMMUNE_ID_1111);
	}

	@Test
	public void bydelId_gittRodeSti_returnerBydelId() throws Exception {
		assertThat(RODE_STI.bydelId()).isEqualTo(BYDEL_ID_111111);
	}

	@Test
	public void stemmekretsId_gittRodeSti_returnerStemmekretsId() throws Exception {
		assertThat(RODE_STI.stemmekretsId()).isEqualTo(STEMMEKRETS_ID_1111);
	}

	@Test
	public void stemmestedId_gittRodeSti_returnerStemmestedId() throws Exception {
		assertThat(RODE_STI.stemmestedId()).isEqualTo(STEMMESTED_ID_1111);
	}

	@Test
	public void rodeId_gittRodeId_returnerRodeId() throws Exception {
		assertThat(RODE_STI.rodeId()).isEqualTo(RODE_ID_11);
	}

	@Test
	public void valghendelseSti_gittRodeSti_returnerValghendelseSti() throws Exception {
		assertThat(RODE_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void landSti_gittRodeSti_returnererLandSti() {
		assertThat(RODE_STI.landSti()).isEqualTo(LAND_STI);
	}

	@Test
	public void fylkeskommuneSti_gittRodeSti_returnererFylkeskommuneSti() {
		assertThat(RODE_STI.fylkeskommuneSti()).isEqualTo(FYLKESKOMMUNE_STI);
	}

	@Test
	public void kommuneSti_gittRodeSti_returnererKommuneSti() {
		assertThat(RODE_STI.kommuneSti()).isEqualTo(KOMMUNE_STI);
	}

	@Test
	public void bydelSti_gittRodeSti_returnererBydelSti() {
		assertThat(RODE_STI.bydelSti()).isEqualTo(BYDEL_STI);
	}

	@Test
	public void stemmekretsSti_gittRodeSti_returnererStemmekretsSti() {
		assertThat(RODE_STI.stemmekretsSti()).isEqualTo(STEMMEKRETS_STI);
	}

	@Test
	public void stemmestedSti_gittRodeSti_returnererStemmestedSti() {
		assertThat(RODE_STI.stemmestedSti()).isEqualTo(STEMMESTED_STI);
	}

	@Test
	public void sisteId_gittRodeSti_rodeId() throws Exception {
		assertThat(RODE_STI.sisteId()).isEqualTo(RODE_ID_11);
	}

	@Test
	public void toString_gittRodeSti_returnerStringAvSti() throws Exception {
		assertThat(RODE_STI.toString()).isEqualTo("111111.11.11.1111.111111.1111.1111.11");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(RodeSti rodeSti, Object other, boolean result) throws Exception {
		assertThat(rodeSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(RodeSti rodeSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(rodeSti.hashCode()).isNotZero();
		assertThat(rodeSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{RODE_STI, RODE_STI, true},
				new Object[]{RODE_STI, new RodeSti(STEMMESTED_STI, RODE_ID_11), true},
				new Object[]{RODE_STI, new RodeSti(STEMMESTED_STI, "99"), false},
				new Object[]{RODE_STI, new Object(), false},
				new Object[]{RODE_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikRodeSti_returnererTrue() throws Exception {
		assertThat(RODE_STI.likEllerUnder(new RodeSti(STEMMESTED_STI, RODE_ID_11))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderStemmestedSti_returnererTrue() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		StemmestedSti stemmestedSti = stemmestedSti();
		RodeSti rodeSti = new RodeSti(stemmestedSti, RODE_ID_11);
		when(stemmestedSti.likEllerUnder(valggeografiSti)).thenReturn(true);
		assertThat(rodeSti.likEllerUnder(valggeografiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderStemmestedSti_returnererFalse() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		StemmestedSti stemmestedSti = stemmestedSti();
		RodeSti rodeSti = new RodeSti(stemmestedSti, RODE_ID_11);
		when(stemmestedSti.likEllerUnder(valggeografiSti)).thenReturn(false);
		assertThat(rodeSti.likEllerUnder(valggeografiSti)).isFalse();
	}
}
