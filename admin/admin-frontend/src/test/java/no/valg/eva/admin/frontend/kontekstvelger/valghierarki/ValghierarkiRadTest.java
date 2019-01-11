package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_12;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiRadTest {
	@Test
	public void propertySti_virkerSomForventet() throws Exception {
		ValghierarkiRad<ValgSti> rad = new ValghierarkiRad<>();
		rad.setSti(VALG_STI);
		assertThat(rad.getSti()).isEqualTo(VALG_STI);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_returnererForventetResultat(ValghierarkiRad rad, Object o, boolean forventetResultat) throws Exception {
		assertThat(rad.equals(o)).isEqualTo(forventetResultat);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_oppfoererSegSomForventet(ValghierarkiRad rad, Object o, boolean forventetResultat) throws Exception {
		assertThat(rad.hashCode() == hashCode(o)).isEqualTo(forventetResultat);
	}

	private int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		ValghierarkiRad rad = rad(VALG_111111_11_11);
		return new Object[][]{
				{rad, null, false},
				{rad, new Object(), false},
				{rad, rad(VALG_111111_11_12), false},
				{rad, rad(VALG_111111_11_11), true},
				{rad, rad, true}
		};
	}

	private ValghierarkiRad rad(Valg valg) {
		return new ValghierarkiRad<>(valg);
	}
}
