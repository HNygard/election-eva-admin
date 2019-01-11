package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI_111111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_111111_11;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiPanelTestData.valggeografiPanel;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiRadTestData.LAND_RAD_111111_11;
import static no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi.ValggeografiTabellAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

import org.testng.annotations.Test;

public class LandTabellTest extends FellesValggeografiTabellTest {
	@Test
	public void oppdater_gittLandTabell_oppdatererTabell() throws Exception {
		ValggeografiPanel valggeografiPanel = valggeografiPanel();
		when(valggeografiPanel.getValggeografiService().land(valggeografiPanel.getUserData())).thenReturn(LAND_111111_11);
		LandTabell landTabell = new LandTabell(valggeografiPanel);
		landTabell.oppdater();
		assertThat(landTabell).harAntallRaderLikMed(1);
		assertThat(landTabell).harValgtRadLikMed(LAND_RAD_111111_11);
		assertThat(landTabell).harValgtStiLikMed(LAND_STI_111111_11);
	}

	@Test
	public void valgtRadSatt_gittValghendelsesTabell_doNothing() throws Exception {
		ValggeografiPanel valggeografiPanel = valggeografiPanel();
		LandTabell landTabell = new LandTabell(valggeografiPanel);
		landTabell.valgtRadSatt();
		verify(valggeografiPanel).oppdaterFylkeskommunerTabell();
	}

}
