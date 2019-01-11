package no.valg.eva.admin.felles.valghierarki.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiTest {
	private static final ValghierarkiSti STI_1 = mock(ValghierarkiSti.class);
	private static final ValghierarkiSti STI_2 = mock(ValghierarkiSti.class);
	private static final String NAVN_1 = "NAVN_1";
	private static final String NAVN_2 = "NAVN_2";

	@Test
	public void sti_gittInstansAvValghierarki_returnerSti() throws Exception {
		assertThat(valghierarki().sti()).isEqualTo(STI_1);
	}

	@Test
	public void id_gittInstansAvValggeografi_returnerSisteIdAvSti() throws Exception {
		assertThat(valghierarki().id()).isEqualTo(STI_1.sisteId());
	}

	@Test
	public void navn_gittInstansAvValghierarki_returnerNavn() throws Exception {
		assertThat(valghierarki().navn()).isEqualTo(NAVN_1);
	}

	@Test(dataProvider = "equalsOgHashCodeTestData")
	public void equals_gittTestData_erGyldig(Valghierarki valghierarki, Object other, boolean result) throws Exception {
		assertThat(valghierarki.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsOgHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(Valghierarki valghierarki, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(valghierarki.hashCode()).isNotZero();
		assertThat(valghierarki.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsOgHashCodeTestData() {
		Valghierarki valghierarki1 = valghierarki();
		Valghierarki valghierarki2 = valghierarki();
		Valghierarki valghierarki3 = valghierarki(STI_1, NAVN_2);
		Valghierarki valghierarki4 = valghierarki(STI_2, NAVN_1);
		return new Object[][]{
				new Object[]{valghierarki1, valghierarki1, true},
				new Object[]{valghierarki1, valghierarki2, true},
				new Object[]{valghierarki1, valghierarki3, false},
				new Object[]{valghierarki1, valghierarki4, false},
				new Object[]{valghierarki1, new Object(), false},
				new Object[]{valghierarki1, null, false}
		};
	}

	private Valghierarki valghierarki() {
		return valghierarki(STI_1, NAVN_1);
	}

	private Valghierarki valghierarki(ValghierarkiSti sti, String navn) {
		return new Valghierarki<ValghierarkiSti>(sti, navn) {
			@Override
			public ValghierarkiNivaa nivaa() {
				return null;
			}

			@Override
			public ValggeografiNivaa valggeografiNivaa() {
				return null;
			}
		};
	}
}
