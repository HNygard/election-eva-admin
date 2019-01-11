package no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.BydelerTabell;

public final class BydelerTabellTestData {
	private BydelerTabellTestData() {
	}

	public static BydelerTabell bydelerTabell(BydelSti valgtBydelSti) {
		BydelerTabell bydelerTabell = mock(BydelerTabell.class, RETURNS_DEEP_STUBS);
		when(bydelerTabell.valgtSti()).thenReturn(valgtBydelSti);
		when(bydelerTabell.isRadValgt()).thenReturn(valgtBydelSti != null);
		return bydelerTabell;
	}
}
