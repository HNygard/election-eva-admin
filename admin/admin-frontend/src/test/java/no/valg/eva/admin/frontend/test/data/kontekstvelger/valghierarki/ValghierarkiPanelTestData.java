package no.valg.eva.admin.frontend.test.data.kontekstvelger.valghierarki;


import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;

public final class ValghierarkiPanelTestData {
	private ValghierarkiPanelTestData() {
	}

	public static ValghierarkiPanel valghierarkiPanel() {
		return mock(ValghierarkiPanel.class, RETURNS_DEEP_STUBS);
	}
}
