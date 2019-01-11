package no.valg.eva.admin.felles.sti.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_ID_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_ID_111112;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghendelseStiTest {
	@Test(dataProvider = "ugyldigInput", expectedExceptions = IllegalArgumentException.class)
	public void constructor_gittUgyldigInput_kasterException(String ugyldigValghendelseId) throws Exception {
		new ValghendelseSti(ugyldigValghendelseId);
	}

	@DataProvider
	public Object[][] ugyldigInput() {
		return new Object[][]{
				new Object[]{""},
				new Object[]{"11111"},
				new Object[]{"1111111"},
				new Object[]{"11111a"}
		};
	}

	@Test
	public void valghendelseId_gittValghendelseSti_returnerValghendelseId() throws Exception {
		assertThat(VALGHENDELSE_STI.valghendelseId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void valggruppeSti_gittValggruppeId_returnerValggruppeSti() throws Exception {
		assertThat(VALGHENDELSE_STI.valggruppeSti(VALGGRUPPE_ID_11)).isEqualTo(VALGGRUPPE_STI);
	}

	@Test
	public void valghendelseSti_gittValghendelseSti_returnerSammeObjekt() throws Exception {
		assertThat(VALGHENDELSE_STI.valghendelseSti()).isSameAs(VALGHENDELSE_STI);
	}

	@Test
	public void sisteId_gittValghendelseSti_returnerValghendelseId() throws Exception {
		assertThat(VALGHENDELSE_STI.sisteId()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test
	public void toString_gittValghendelseSti_returnerSti() throws Exception {
		assertThat(VALGHENDELSE_STI.toString()).isEqualTo(VALGHENDELSE_ID_111111);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_erGyldig(ValghendelseSti sti, Object that, boolean resultat) throws Exception {
		assertThat(sti.equals(that)).isEqualTo(resultat);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(ValghendelseSti sti, Object that, boolean resultat) throws Exception {
		if (that == null) {
			return;
		}
		assertThat(sti.hashCode()).isNotZero();
		assertThat(sti.hashCode() == that.hashCode()).isEqualTo(resultat);
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		return new Object[][]{
				new Object[]{VALGHENDELSE_STI, VALGHENDELSE_STI, true},
				new Object[]{VALGHENDELSE_STI, new ValghendelseSti(VALGHENDELSE_ID_111111), true},
				new Object[]{VALGHENDELSE_STI, new ValghendelseSti("999999"), false},
				new Object[]{VALGHENDELSE_STI, new Object(), false},
				new Object[]{VALGHENDELSE_STI, null, false}
		};
	}

	@Test
	public void likEllerUnder_gittLikValghendelseSti_returnererTrue() throws Exception {
		assertThat(VALGHENDELSE_STI.likEllerUnder(new ValghendelseSti(VALGHENDELSE_ID_111111))).isTrue();
	}

	@Test
	public void likEllerUnder_gittUlikValghendelseSti_returnererFalse() throws Exception {
		assertThat(VALGHENDELSE_STI.likEllerUnder(new ValghendelseSti(VALGHENDELSE_ID_111112))).isFalse();
	}
}
