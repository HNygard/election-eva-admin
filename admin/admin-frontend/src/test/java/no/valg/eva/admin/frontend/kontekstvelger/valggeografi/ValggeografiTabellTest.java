package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiPanelTestData.valggeografiPanel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValggeografiTabellTest {
	@Test
	public void getId_gittTabell_returnererNivaa() throws Exception {
		assertThat(valggeografiTabell().getId()).isEqualTo(KOMMUNE);
	}

	@Test
	public void getNavn_gittTabell_returnerVisningsnavnPaaNivaa() throws Exception {
		assertThat(valggeografiTabell().getNavn()).isEqualTo(KOMMUNE.visningsnavn());
	}

	@Test(dataProvider = "trueFalse")
	public void isVisKnapp_gittTabell_returnererForventetVerdi(boolean visKnappResultat) throws Exception {
		ValggeografiPanel panel = valggeografiPanel();
		when(panel.visKnapp(KOMMUNE)).thenReturn(visKnappResultat);
		assertThat(valggeografiTabell(panel).isVisKnapp()).isEqualTo(visKnappResultat);
	}

	@DataProvider
	public Object[][] trueFalse() {
		return new Object[][]{
				{true},
				{false}
		};
	}

	private ValggeografiTabell<KommuneSti, Kommune> valggeografiTabell() {
		return valggeografiTabell(valggeografiPanel());
	}

	private ValggeografiTabell<KommuneSti, Kommune> valggeografiTabell(ValggeografiPanel panel) {
		return new ValggeografiTabell<KommuneSti, Kommune>(panel, KOMMUNE, FO) {
			@Override
			protected void valgtRadSatt() {
			}

			@Override
			public void oppdater() {
			}
		};
	}
}
