package no.valg.eva.admin.felles.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.valgSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
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

public class ValgdistriktStiTest {

	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kastException(ValgSti valgSti, String ugyldigValgdistriktId) throws Exception {
		new ValgdistriktSti(valgSti, ugyldigValgdistriktId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, VALGDISTRIKT_ID_111111},
				new Object[]{VALG_STI, ""},
				new Object[]{VALG_STI, "11111"},
				new Object[]{VALG_STI, "1111111"},
				new Object[]{VALG_STI, "11111a"}
		};
	}

	@Test
	public void valghendelseId_gittValgdistriktSti_returnererValghendelseId() throws Exception {
		assertThat(VALGDISTRIKT_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void valggruppeId_gittValgdistriktSti_returnerValggruppeID() throws Exception {
		assertThat(VALGDISTRIKT_STI.valggruppeId()).isEqualTo(VALGGRUPPE_ID_11);
	}

	@Test
	public void valgId_gittValgdistriktSti_returnereValgId() throws Exception {
		assertThat(VALGDISTRIKT_STI.valgId()).isEqualTo(VALG_ID_11);
	}

	@Test
	public void valgdistriktId_gittValgdistriktSti_returnereValgdistriktId() throws Exception {
		assertThat(VALGDISTRIKT_STI.valgdistriktId()).isEqualTo(VALGDISTRIKT_ID_111111);
	}

	@Test
	public void valghendelseSti_gittValgdistriktSti_returnererValghendelseSti() throws Exception {
		assertThat(VALGDISTRIKT_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void valggruppeSti_gittValgdistriktSti_returnererValggruppeSti() {
		assertThat(VALGDISTRIKT_STI.valggruppeSti()).isEqualTo(VALGGRUPPE_STI);
	}

	@Test
	public void valgSti_gittValgdistriktSti_returnererValgSti() {
		assertThat(VALGDISTRIKT_STI.valgSti()).isEqualTo(VALG_STI);
	}

	@Test
	public void sisteId_gittValgdistriktSti_returnerValgdistriktId() throws Exception {
		assertThat(VALGDISTRIKT_STI.sisteId()).isEqualTo(VALGDISTRIKT_ID_111111);
	}

	@Test
	public void toString_gittValgdistriktSti_returnererStringAvSti() throws Exception {
		assertThat(VALGDISTRIKT_STI.toString()).isEqualTo("111111.11.11.111111");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(ValgdistriktSti valgdistriktSti, Object other, boolean result) throws Exception {
		assertThat(valgdistriktSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(ValgdistriktSti valgdistriktSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(valgdistriktSti.hashCode()).isNotZero();
		assertThat(valgdistriktSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{VALGDISTRIKT_STI, VALGDISTRIKT_STI, true},
				new Object[]{VALGDISTRIKT_STI, new ValgdistriktSti(VALG_STI, VALGDISTRIKT_ID_111111), true},
				new Object[]{VALGDISTRIKT_STI, new ValgdistriktSti(VALG_STI, "999999"), false},
				new Object[]{VALGDISTRIKT_STI, new Object(), false},
				new Object[]{VALGDISTRIKT_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikValgdistriktSti_returnererTrue() throws Exception {
		assertThat(VALGDISTRIKT_STI.likEllerUnder(new ValgdistriktSti(VALG_STI, VALGDISTRIKT_ID_111111))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderValgSti_returnererTrue() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValgSti valgSti = valgSti();
		ValgdistriktSti valgdistriktSti = new ValgdistriktSti(valgSti, VALGDISTRIKT_ID_111111);
		when(valgSti.likEllerUnder(valghierarkiSti)).thenReturn(true);
		assertThat(valgdistriktSti.likEllerUnder(valghierarkiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderValgSti_returnererFalse() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValgSti valgSti = valgSti();
		ValgdistriktSti valgdistriktSti = new ValgdistriktSti(valgSti, VALGDISTRIKT_ID_111111);
		when(valgSti.likEllerUnder(valghierarkiSti)).thenReturn(false);
		assertThat(valgdistriktSti.likEllerUnder(valghierarkiSti)).isFalse();
	}
}
