package no.valg.eva.admin.frontend.kontekstvelger;

import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerHjelp.kontekstvelgerURL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class KontekstvelgerHjelpTest extends MockUtilsTestCase {
	@Test
	public void kontekstvelgerURL_gittOppsett_returnerUrl() throws Exception {
		assertThat(kontekstvelgerURL(oppsett())).isEqualTo("/secure/kontekstvelger.xhtml?oppsett=OPPSETT");
	}

	@Test
	public void kontekstvelgerURL_gittOppsettOgKontekst_returnerUrl() throws Exception {
		assertThat(kontekstvelgerURL(oppsett(), kontekst())).isEqualTo("/secure/kontekstvelger.xhtml?oppsett=OPPSETT&kontekst=KONTEKST");
	}

	private KontekstvelgerOppsett oppsett() {
		KontekstvelgerOppsett oppsett = createMock(KontekstvelgerOppsett.class);
		when(oppsett.serialize()).thenReturn("OPPSETT");
		return oppsett;
	}

	private Kontekst kontekst() {
		Kontekst kontekst = createMock(Kontekst.class);
		when(kontekst.serialize()).thenReturn("KONTEKST");
		return kontekst;
	}
}
