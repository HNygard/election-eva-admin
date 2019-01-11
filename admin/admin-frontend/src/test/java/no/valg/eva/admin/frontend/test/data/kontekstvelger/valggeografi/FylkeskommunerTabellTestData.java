package no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.FylkeskommunerTabell;

public final class FylkeskommunerTabellTestData {
	private FylkeskommunerTabellTestData() {
	}

	public static FylkeskommunerTabell fylkeskommunerTabell(FylkeskommuneSti valgtFylkeskommuneSti) {
		FylkeskommunerTabell fylkeskommunerTabell = mock(FylkeskommunerTabell.class, RETURNS_DEEP_STUBS);
		when(fylkeskommunerTabell.valgtSti()).thenReturn(valgtFylkeskommuneSti);
		when(fylkeskommunerTabell.isRadValgt()).thenReturn(valgtFylkeskommuneSti != null);
		return fylkeskommunerTabell;
	}
}
