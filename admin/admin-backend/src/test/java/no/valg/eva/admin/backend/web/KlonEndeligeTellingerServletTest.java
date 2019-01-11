package no.valg.eva.admin.backend.web;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static no.valg.eva.admin.backend.web.KlonEndeligeTellingerServlet.PARAMETER_LOKALT_FORDELT_PAA_KRETS;
import static no.valg.eva.admin.backend.web.KlonEndeligeTellingerServlet.PARAMETER_SENTRALT_FORDELT_PAA_KRETS;
import static no.valg.eva.admin.backend.web.KlonEndeligeTellingerServlet.PARAMETER_SENTRALT_SAMLET;
import static no.valg.eva.admin.backend.web.KlonEndeligeTellingerServlet.PARAMETER_VALGHENDELSE_ID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.counting.domain.model.KlonEndeligeTellingerResultat;
import no.valg.eva.admin.counting.domain.service.KlonEndeligeTellingerDomainService;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KlonEndeligeTellingerServletTest extends MockUtilsTestCase {
	private KlonEndeligeTellingerServlet servlet;
	private HttpServletRequest stubRequest;
	private HttpServletResponse stubResponse;
	private PrintWriter mockPrintWriter;
	
	@BeforeMethod
	public void setUp() throws Exception {
		servlet = servletMedKonfigurasjon();
		stubRequest = createMock(HttpServletRequest.class);
		stubResponse = createMock(HttpServletResponse.class);
		mockPrintWriter = mockPrintWriter(stubResponse);
	}

	private KlonEndeligeTellingerServlet servletMedKonfigurasjon() throws Exception {
		KlonEndeligeTellingerServlet servlet = initializeMocks(KlonEndeligeTellingerServlet.class);
		mockFieldValue("erTjenestenTilkoblet", TRUE);
		return servlet;
	}

	private PrintWriter mockPrintWriter(HttpServletResponse stubResponse) throws IOException {
		PrintWriter mockPrintWriter = createMock(PrintWriter.class);
		when(stubResponse.getWriter()).thenReturn(mockPrintWriter);
		return mockPrintWriter;
	}

	@Test
	public void doGet_hvisTilgangErGitt_returnererHtmlskjema() throws Exception {
		servlet.doGet(null, stubResponse);

		verify(mockPrintWriter).append(Matchers.matches(".*Klon kommunens endelige tellinger til fylkeskommunens kontrolltellinger.*"));
	}

	@Test
	public void doGet_hvisIkkeKonfigurasjonsparameterErSatt_gisIkkeTilgangTilTjenesten() throws Exception {
		servletUtenKonfigurasjon().doGet(stubRequest, stubResponse);

		verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void doPost_hvisIkkeKonfigurasjonsparameterErSatt_gisIkkeTilgangTilTjenesten() throws Exception {
		servletUtenKonfigurasjon().doPost(stubRequest, stubResponse);

		verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private KlonEndeligeTellingerServlet servletUtenKonfigurasjon() throws Exception {
		KlonEndeligeTellingerServlet servlet = initializeMocks(KlonEndeligeTellingerServlet.class);
		mockFieldValue("erTjenestenTilkoblet", FALSE);
		return servlet;
	}

	@Test
	public void doPost_gittInput_kallerServiceOgReturnererResultat() throws Exception {
		KlonEndeligeTellingerResultat klonEndeligeTellingerResultat = klonEndeligTellingerResultat();
		KlonEndeligeTellingerDomainService domainService = getInjectMock(KlonEndeligeTellingerDomainService.class);

		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("111111");
		when(stubRequest.getParameter(PARAMETER_LOKALT_FORDELT_PAA_KRETS)).thenReturn("1");
		when(stubRequest.getParameter(PARAMETER_SENTRALT_FORDELT_PAA_KRETS)).thenReturn("2");
		when(stubRequest.getParameter(PARAMETER_SENTRALT_SAMLET)).thenReturn("3");
		when(domainService.klonEndeligeTellinger(new ValghendelseSti("111111"), 1, 2, 3, emptyList())).thenReturn(klonEndeligeTellingerResultat);

		servlet.doPost(stubRequest, stubResponse);

		verify(domainService).klonEndeligeTellinger(new ValghendelseSti("111111"), 1, 2, 3, emptyList());
		verify(mockPrintWriter).append("<html><head></head><body>HTML</body></html>");
	}

	private KlonEndeligeTellingerResultat klonEndeligTellingerResultat() {
		KlonEndeligeTellingerResultat resultat = createMock(KlonEndeligeTellingerResultat.class);
		when(resultat.toHtml()).thenReturn("HTML");
		return resultat;
	}
}
