package no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.KommunerTabell;

public final class KommunerTabellTestData {
	private KommunerTabellTestData() {
	}

	public static KommunerTabell kommunerTabell(KommuneSti valgtKommuneSti) {
		KommunerTabell kommunerTabell = mock(KommunerTabell.class, RETURNS_DEEP_STUBS);
		when(kommunerTabell.valgtSti()).thenReturn(valgtKommuneSti);
		when(kommunerTabell.isRadValgt()).thenReturn(valgtKommuneSti != null);
		return kommunerTabell;
	}
}
