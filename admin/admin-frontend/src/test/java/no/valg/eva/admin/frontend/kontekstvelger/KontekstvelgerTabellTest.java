package no.valg.eva.admin.frontend.kontekstvelger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.frontend.kontekstvelger.panel.KontekstvelgerPanel;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KontekstvelgerTabellTest extends MockUtilsTestCase {
	@Test
	public void isVisAntall_gittTabell_returnererTrue() throws Exception {
		assertThat(kontekstvelgerTabell().isVisAntallRader()).isTrue();
	}

	@Test
	public void propertyVisAntallRader_fungererSomForventet() throws Exception {
		KontekstvelgerTabell<KontekstvelgerPanel, KontekstvelgerRad, Object> tabell = kontekstvelgerTabell();
		tabell.setVisAntallRader(false);
		assertThat(tabell.isVisAntallRader()).isFalse();
		tabell.setVisAntallRader(true);
		assertThat(tabell.isVisAntallRader()).isTrue();
	}

	@Test
	public void propertyRader_fungererSomForventet() throws Exception {
		KontekstvelgerTabell<KontekstvelgerPanel, KontekstvelgerRad, Object> tabell = kontekstvelgerTabell();
		List<KontekstvelgerRad> rader = createListMock();
		tabell.setRader(rader);
		assertThat(tabell.getRader()).isEqualTo(rader);
	}

	@Test
	public void setRader_gittEnRad_setterValgtRad() throws Exception {
		int[] valgtRadKalt = {0};
		KontekstvelgerTabell<KontekstvelgerPanel, KontekstvelgerRad, Object> tabell = kontekstvelgerTabell(() -> valgtRadKalt[0]++);
		KontekstvelgerRad rad = kontekstvelgerRad();
		tabell.setRader(singletonList(rad));
		assertThat(tabell.isRadValgt());
		assertThat(tabell.getValgtRad()).isEqualTo(rad);
		assertThat(tabell.getAntallRader()).isEqualTo(1);
		assertThat(valgtRadKalt[0]).describedAs("valgRadKalt én gang").isEqualTo(1);
	}

	@Test(dataProvider = "visKnappOgFlereRaderOgResultat")
	public void isVisTabell_gittTestData_girForventeResultat(boolean visKnapp, boolean flereRader, boolean forventetResultat) throws Exception {
		KontekstvelgerTabell<KontekstvelgerPanel, KontekstvelgerRad, Object> tabell = kontekstvelgerTabell(visKnapp);
		if (flereRader) {
			tabell.setRader(asList(kontekstvelgerRad(), kontekstvelgerRad()));
		} else {
			tabell.setRader(singletonList(kontekstvelgerRad()));
		}
		assertThat(tabell.isVisTabell()).isEqualTo(forventetResultat);
	}

	@DataProvider
	public Object[][] visKnappOgFlereRaderOgResultat() {
		return new Object[][]{
				{true, true, true},
				{true, false, true},
				{false, false, false},
				{false, true, true}
		};
	}

	@Test
	public void isKnappDeaktivert_gittIkkeValgtRad_returnerTrue() throws Exception {
		assertThat(kontekstvelgerTabell().isKnappDeaktivert()).isTrue();
	}

	@Test
	public void isKnappDeaktivert_gittValgtRad_returnerFalse() throws Exception {
		KontekstvelgerTabell<KontekstvelgerPanel, KontekstvelgerRad, Object> tabell = kontekstvelgerTabell();
		tabell.setValgtRad(new KontekstvelgerRad() {
		});
		assertThat(tabell.isKnappDeaktivert()).isFalse();
	}

	private KontekstvelgerRad kontekstvelgerRad() {
		return new KontekstvelgerRad() {
		};
	}

	private <P extends KontekstvelgerPanel, R extends KontekstvelgerRad> KontekstvelgerTabell<P, R, Object> kontekstvelgerTabell() {
		return kontekstvelgerTabell(() -> {
		});
	}

	private <P extends KontekstvelgerPanel, R extends KontekstvelgerRad> KontekstvelgerTabell<P, R, Object> kontekstvelgerTabell(boolean visKnapp) {
		return kontekstvelgerTabell(visKnapp, () -> {
		});
	}

	private <P extends KontekstvelgerPanel, R extends KontekstvelgerRad> KontekstvelgerTabell<P, R, Object> kontekstvelgerTabell(Runnable valgtRadSatt) {
		return kontekstvelgerTabell(false, valgtRadSatt);
	}

	private <P extends KontekstvelgerPanel, R extends KontekstvelgerRad> KontekstvelgerTabell<P, R, Object> kontekstvelgerTabell(
			boolean visKnapp, Runnable valgtRadSatt) {
		return new KontekstvelgerTabell<P, R, Object>(panel(), true) {
			@Override
			protected void valgtRadSatt() {
				valgtRadSatt.run();
			}

			@Override
			public void oppdater() {
			}

			@Override
			public Object getId() {
				return null;
			}

			@Override
			public String getNavn() {
				return null;
			}

			@Override
			public boolean isVisKnapp() {
				return visKnapp;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <P extends KontekstvelgerPanel> P panel() {
		return (P) createMock(KontekstvelgerPanel.class);
	}
}
