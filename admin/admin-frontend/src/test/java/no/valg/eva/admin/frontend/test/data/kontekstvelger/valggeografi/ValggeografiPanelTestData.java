package no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;

public final class ValggeografiPanelTestData {
	private ValggeografiPanelTestData() {
	}

	public static ValggeografiPanel valggeografiPanel() {
		return mock(ValggeografiPanel.class, RETURNS_DEEP_STUBS);
	}
}
