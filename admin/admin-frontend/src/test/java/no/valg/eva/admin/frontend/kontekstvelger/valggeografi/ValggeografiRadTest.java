package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1112;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggeografiRadTest {
	@Test
	public void propertySti_virkerSomForventet() throws Exception {
		ValggeografiRad<KommuneSti> rad = new ValggeografiRad<>();
		rad.setSti(KOMMUNE_STI);
		assertThat(rad.getSti()).isEqualTo(KOMMUNE_STI);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_returnererForventetResultat(ValggeografiRad rad, Object o, boolean forventetResultat) throws Exception {
		assertThat(rad.equals(o)).isEqualTo(forventetResultat);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_oppfoererSegSomForventet(ValggeografiRad rad, Object o, boolean forventetResultat) throws Exception {
		assertThat(rad.hashCode() == hashCode(o)).isEqualTo(forventetResultat);
	}

	private int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		ValggeografiRad rad = rad(KOMMUNE_111111_11_11_1111);
		return new Object[][]{
				{rad, null, false},
				{rad, new Object(), false},
				{rad, rad(KOMMUNE_111111_11_11_1112), false},
				{rad, rad(KOMMUNE_111111_11_11_1111), true},
				{rad, rad, true}
		};
	}

	private ValggeografiRad rad(Kommune kommune) {
		return new ValggeografiRad<>(kommune);
	}
}
