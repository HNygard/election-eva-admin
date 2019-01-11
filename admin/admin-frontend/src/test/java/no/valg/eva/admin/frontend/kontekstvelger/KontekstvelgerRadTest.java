package no.valg.eva.admin.frontend.kontekstvelger;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KontekstvelgerRadTest {
	@Test
	public void getId_gittRad_returnererId() throws Exception {
		assertThat(kontekstvelgerRad().getId()).isEqualTo("ID");
	}

	@Test
	public void propertyId_virkerSomForventet() throws Exception {
		KontekstvelgerRad kontekstvelgerRad = kontekstvelgerRad();
		kontekstvelgerRad.setId("ANNEN_ID");
		assertThat(kontekstvelgerRad.getId()).isEqualTo("ANNEN_ID");
	}

	@Test
	public void isVisId_gittRad_returnererTrue() throws Exception {
		assertThat(kontekstvelgerRad().isVisId()).isTrue();
	}

	@Test
	public void propertyVisId_virkerSomForventet() throws Exception {
		KontekstvelgerRad kontekstvelgerRad = kontekstvelgerRad();
		kontekstvelgerRad.setVisId(false);
		assertThat(kontekstvelgerRad.isVisId()).isFalse();
		kontekstvelgerRad.setVisId(true);
		assertThat(kontekstvelgerRad.isVisId()).isTrue();
	}

	@Test
	public void getVerdi_gittRad_returnererVerdi() throws Exception {
		assertThat(kontekstvelgerRad().getVerdi()).isEqualTo("VERDI");
	}

	@Test
	public void propertyVerdi_virkerSomForventet() throws Exception {
		KontekstvelgerRad kontekstvelgerRad = kontekstvelgerRad();
		kontekstvelgerRad.setVerdi("ANNEN_VERDI");
		assertThat(kontekstvelgerRad.getVerdi()).isEqualTo("ANNEN_VERDI");
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void equals_gittTestData_returnererForventetResultat(KontekstvelgerRad rad, Object o, boolean forventetResultat) throws Exception {
		assertThat(rad.equals(o)).isEqualTo(forventetResultat);
	}

	@Test(dataProvider = "equalsHashCodeTestData")
	public void hashCode_gittTestData_oppfoererSegSomForventet(KontekstvelgerRad rad, Object o, boolean forventetResultat) throws Exception {
		assertThat(rad.hashCode() == hashCode(o)).isEqualTo(forventetResultat);
	}

	private int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	@DataProvider
	public Object[][] equalsHashCodeTestData() {
		KontekstvelgerRad rad = kontekstvelgerRad();
		return new Object[][]{
				{rad, null, false},
				{rad, new Object(), false},
				{rad, kontekstvelgerRad("ANNEN_ID", true, "VERDI"), false},
				{rad, kontekstvelgerRad("ID", false, "VERDI"), false},
				{rad, kontekstvelgerRad("ID", true, "ANNEN_VERDI"), false},
				{rad, kontekstvelgerRad(), true},
				{rad, rad, true}
		};
	}

	private KontekstvelgerRad kontekstvelgerRad() {
		return kontekstvelgerRad("ID", true, "VERDI");
	}

	private KontekstvelgerRad kontekstvelgerRad(String id, boolean visId, String verdi) {
		return new KontekstvelgerRad(id, visId, verdi) {
		};
	}
}
