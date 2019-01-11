package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_111111;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiPanelTestData.valggeografiPanel;
import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiRadTestData.VALGHENDELSE_RAD_111111;
import static no.valg.eva.admin.frontend.test.kontekstvelger.valggeografi.ValggeografiTabellAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

import org.testng.annotations.Test;

public class ValghendelseTabellTest extends FellesValggeografiTabellTest {
	@Test
	public void oppdater_gittValghendelseTabell_oppdatererTabell() throws Exception {
		ValggeografiPanel valggeografiPanel = valggeografiPanel();
		when(valggeografiPanel.getValggeografiService().valghendelse(valggeografiPanel.getUserData())).thenReturn(VALGHENDELSE_111111);
		ValghendelseTabell valghendelseTabell = new ValghendelseTabell(valggeografiPanel);
		valghendelseTabell.oppdater();
		assertThat(valghendelseTabell).harAntallRaderLikMed(1);
		assertThat(valghendelseTabell).harValgtRadLikMed(VALGHENDELSE_RAD_111111);
		assertThat(valghendelseTabell).harValgtStiLikMed(VALGHENDELSE_STI_111111);
	}

	@Test
	public void valgtRadSatt_gittValghendelsesTabell_doNothing() throws Exception {
		ValggeografiPanel valggeografiPanel = valggeografiPanel();
		ValghendelseTabell valghendelseTabell = new ValghendelseTabell(valggeografiPanel);
		valghendelseTabell.valgtRadSatt();
		verify(valggeografiPanel).oppdaterLandTabell();
	}
}
