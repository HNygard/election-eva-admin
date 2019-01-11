package no.valg.eva.admin.felles.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.valggruppeSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_ID_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValgStiTest {

	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kastException(ValggruppeSti valggruppeSti, String ugyldigValgId) throws Exception {
		new ValgSti(valggruppeSti, ugyldigValgId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, VALGGRUPPE_ID_11},
				new Object[]{VALGGRUPPE_STI, ""},
				new Object[]{VALGGRUPPE_STI, "1"},
				new Object[]{VALGGRUPPE_STI, "111"},
				new Object[]{VALGGRUPPE_STI, "1a"}
		};
	}

	@Test
	public void valghendelseId_gittValgSti_returnererValghendelseId() throws Exception {
		assertThat(VALG_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void valggruppeId_gittValgSti_returnererValggruppeId() throws Exception {
		assertThat(VALG_STI.valggruppeId()).isEqualTo(VALGGRUPPE_ID_11);
	}

	@Test
	public void valgId_gittValgSti_returnereValgId() throws Exception {
		assertThat(VALG_STI.valgId()).isEqualTo(VALG_ID_11);
	}

	@Test
	public void valgdistriktSti_gittValgdistriktId_returnereValgdistriktSti() throws Exception {
		assertThat(VALG_STI.valgdistriktSti(VALGDISTRIKT_ID_111111)).isEqualTo(VALGDISTRIKT_STI);
	}

	@Test
	public void valghendelseSti_gittValgSti_returnererValghendelseSti() throws Exception {
		assertThat(VALG_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void valggruppeSti_gittValgSti_returnererValggruppeSti() {
		assertThat(VALG_STI.valggruppeSti()).isEqualTo(VALGGRUPPE_STI);
	}

	@Test
	public void sisteId_gittValgSti_returnerValgId() throws Exception {
		assertThat(VALG_STI.sisteId()).isEqualTo(VALG_ID_11);
	}

	@Test
	public void toString_gittValgSti_returnererStringAvSti() throws Exception {
		assertThat(VALG_STI.toString()).isEqualTo("111111.11.11");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(ValgSti valgSti, Object other, boolean result) throws Exception {
		assertThat(valgSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(ValgSti valgSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(valgSti.hashCode()).isNotZero();
		assertThat(valgSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{VALG_STI, VALG_STI, true},
				new Object[]{VALG_STI, new ValgSti(VALGGRUPPE_STI, VALGGRUPPE_ID_11), true},
				new Object[]{VALG_STI, new ValgSti(VALGGRUPPE_STI, "99"), false},
				new Object[]{VALG_STI, new Object(), false},
				new Object[]{VALG_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikValgSti_returnererTrue() throws Exception {
		assertThat(VALG_STI.likEllerUnder(new ValgSti(VALGGRUPPE_STI, VALG_ID_11))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderValggruppeSti_returnererTrue() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggruppeSti valggruppeSti = valggruppeSti();
		ValgSti valgSti = new ValgSti(valggruppeSti, VALGGRUPPE_ID_11);
		when(valggruppeSti.likEllerUnder(valghierarkiSti)).thenReturn(true);
		assertThat(valgSti.likEllerUnder(valghierarkiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderValggruppeSti_returnererFalse() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggruppeSti valggruppeSti = valggruppeSti();
		ValgSti valgSti = new ValgSti(valggruppeSti, VALGGRUPPE_ID_11);
		when(valggruppeSti.likEllerUnder(valghierarkiSti)).thenReturn(false);
		assertThat(valgSti.likEllerUnder(valghierarkiSti)).isFalse();
	}
}
