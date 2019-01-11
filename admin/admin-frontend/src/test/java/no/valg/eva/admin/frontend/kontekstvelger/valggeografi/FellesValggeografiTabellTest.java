package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi.ValggeografiPanelTestData.valggeografiPanel;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

import org.testng.annotations.BeforeMethod;

public class FellesValggeografiTabellTest {
	protected ValggeografiPanel valggeografiPanel;
	protected ValggeografiService valggeografiService;
	protected UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		valggeografiPanel = valggeografiPanel();
		valggeografiService = valggeografiPanel.getValggeografiService();
		userData = valggeografiPanel.getUserData();
	}
}
