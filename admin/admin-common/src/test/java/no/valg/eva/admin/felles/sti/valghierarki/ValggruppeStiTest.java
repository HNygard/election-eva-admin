package no.valg.eva.admin.felles.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.valghendelseSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggruppeStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kastException(ValghendelseSti valghendelseSti, String ugyldigValggruppeId) throws Exception {
		new ValggruppeSti(valghendelseSti, ugyldigValggruppeId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{null, VALGGRUPPE_ID_11},
				new Object[]{VALGHENDELSE_STI, ""},
				new Object[]{VALGHENDELSE_STI, "1"},
				new Object[]{VALGHENDELSE_STI, "111"},
				new Object[]{VALGHENDELSE_STI, "1a"}
		};
	}

	@Test
	public void valghendelseId_gittValggruppeSti_returnererValghendelseId() throws Exception {
		assertThat(VALGGRUPPE_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void valggruppeId_gittValggruppeSti_returnererValggruppeId() throws Exception {
		assertThat(VALGGRUPPE_STI.valggruppeId()).isEqualTo(VALGGRUPPE_ID_11);
	}

	@Test
	public void valgSti_gittValgId_returnereValgSti() throws Exception {
		assertThat(VALGGRUPPE_STI.valgSti(VALG_ID_11)).isEqualTo(VALG_STI);
	}

	@Test
	public void valghendelseSti_gittValggruppeSti_returnererValghendelseSti() throws Exception {
		assertThat(VALGGRUPPE_STI.valghendelseSti()).isEqualTo(VALGHENDELSE_STI);
	}

	@Test
	public void sisteId_gittValggruppeSti_returnerValggruppeId() throws Exception {
		assertThat(VALGGRUPPE_STI.sisteId()).isEqualTo(VALGGRUPPE_ID_11);
	}

	@Test
	public void toString_gittValggruppeSti_returnererStringAvSti() throws Exception {
		assertThat(VALGGRUPPE_STI.toString()).isEqualTo("111111.11");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(ValggruppeSti valggruppeSti, Object other, boolean result) throws Exception {
		assertThat(valggruppeSti.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(ValggruppeSti valggruppeSti, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(valggruppeSti.hashCode()).isNotZero();
		assertThat(valggruppeSti.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{VALGGRUPPE_STI, VALGGRUPPE_STI, true},
				new Object[]{VALGGRUPPE_STI, new ValggruppeSti(VALGHENDELSE_STI, VALGGRUPPE_ID_11), true},
				new Object[]{VALGGRUPPE_STI, new ValggruppeSti(VALGHENDELSE_STI, "99"), false},
				new Object[]{VALGGRUPPE_STI, new Object(), false},
				new Object[]{VALGGRUPPE_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikValggruppeSti_returnererTrue() throws Exception {
		assertThat(VALGGRUPPE_STI.likEllerUnder(new ValggruppeSti(VALGHENDELSE_STI, VALGGRUPPE_ID_11))).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiLikEllerUnderValghendelseSti_returnererTrue() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValghendelseSti valghendelseSti = valghendelseSti();
		ValggruppeSti valggruppeSti = new ValggruppeSti(valghendelseSti, VALGGRUPPE_ID_11);
		when(valghendelseSti.likEllerUnder(valghierarkiSti)).thenReturn(true);
		assertThat(valggruppeSti.likEllerUnder(valghierarkiSti)).isTrue();
	}

	@Test
	public void likEllerUnder_gittStiIkkeLikEllerUnderValghendelseSti_returnererFalse() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValghendelseSti valghendelseSti = valghendelseSti();
		ValggruppeSti valggruppeSti = new ValggruppeSti(valghendelseSti, VALGGRUPPE_ID_11);
		when(valghendelseSti.likEllerUnder(valghierarkiSti)).thenReturn(false);
		assertThat(valggruppeSti.likEllerUnder(valghierarkiSti)).isFalse();
	}
}
