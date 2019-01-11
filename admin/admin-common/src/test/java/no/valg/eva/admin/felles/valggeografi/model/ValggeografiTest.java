package no.valg.eva.admin.felles.valggeografi.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggeografiTest {
	private static final ValggeografiSti STI_1 = mock(ValggeografiSti.class);
	private static final ValggeografiSti STI_2 = mock(ValggeografiSti.class);
	private static final String NAVN_1 = "NAVN_1";
	private static final String NAVN_2 = "NAVN_2";

	@Test
	public void sti_gittInstansAvValggeografi_returnerSti() throws Exception {
		assertThat(valggeografi().sti()).isEqualTo(STI_1);
	}

	@Test
	public void id_gittInstansAvValggeografi_returnerSisteIdAvSti() throws Exception {
		assertThat(valggeografi().id()).isEqualTo(STI_1.sisteId());
	}

	@Test
	public void navn_gittInstansAvValggeografi_returnerNavn() throws Exception {
		assertThat(valggeografi().navn()).isEqualTo(NAVN_1);
	}

	@Test(dataProvider = "equalsOgHashCodeTestData")
	public void equals_gittTestData_erGyldig(Valggeografi valggeografi, Object other, boolean result) throws Exception {
		assertThat(valggeografi.equals(other)).isEqualTo(result);
	}

	@Test(dataProvider = "equalsOgHashCodeTestData")
	public void hashCode_gittTestData_erGyldig(Valggeografi valggeografi, Object other, boolean result) throws Exception {
		if (other == null) {
			return;
		}
		assertThat(valggeografi.hashCode()).isNotZero();
		assertThat(valggeografi.hashCode() == other.hashCode()).isEqualTo(result);
	}

	@DataProvider
	public Object[][] equalsOgHashCodeTestData() {
		Valggeografi valggeografi1 = valggeografi();
		Valggeografi valggeografi2 = valggeografi();
		Valggeografi valggeografi3 = valggeografi(STI_1, NAVN_2);
		Valggeografi valggeografi4 = valggeografi(STI_2, NAVN_1);
		return new Object[][]{
				new Object[]{valggeografi1, valggeografi1, true},
				new Object[]{valggeografi1, valggeografi2, true},
				new Object[]{valggeografi1, valggeografi3, false},
				new Object[]{valggeografi1, valggeografi4, false},
				new Object[]{valggeografi1, new Object(), false},
				new Object[]{valggeografi1, null, false}
		};
	}

	private Valggeografi valggeografi() {
		return valggeografi(STI_1, NAVN_1);
	}

	private Valggeografi valggeografi(ValggeografiSti sti, String navn) {
		return new Valggeografi<ValggeografiSti>(sti, navn) {
			@Override
			public ValggeografiNivaa nivaa() {
				return null;
			}
		};
	}

}
