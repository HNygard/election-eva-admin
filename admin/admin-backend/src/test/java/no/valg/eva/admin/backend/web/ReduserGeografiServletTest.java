package no.valg.eva.admin.backend.web;

import no.valg.eva.admin.configuration.domain.service.ReduserGeografiDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentMatchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static no.valg.eva.admin.backend.web.ReduserGeografiServlet.PARAMETER_GEOGRAFISPESIFIKASJON;
import static no.valg.eva.admin.backend.web.ReduserGeografiServlet.PARAMETER_REDUKSJONSMODUS;
import static no.valg.eva.admin.backend.web.ReduserGeografiServlet.PARAMETER_VALGHENDELSE_ID;
import static no.valg.eva.admin.backend.web.ReduserGeografiServlet.VALUE_FLYTT;
import static no.valg.eva.admin.backend.web.ReduserGeografiServlet.VALUE_IKKE_SLETT;
import static no.valg.eva.admin.backend.web.ReduserGeografiServlet.VALUE_SLETT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReduserGeografiServletTest extends MockUtilsTestCase {

	private ReduserGeografiServlet reduserGeografiServlet;
	private HttpServletRequest stubRequest;
	private HttpServletResponse stubResponse;
	private PrintWriter mockPrintWriter;

	@BeforeMethod
	public void setUp() throws Exception {
		reduserGeografiServlet = getReduserGeografiServletMedKonfigurasjon();
		stubRequest = createMock(HttpServletRequest.class);
		stubResponse = createMock(HttpServletResponse.class);
		mockPrintWriter = getMockPrintWriter(stubResponse);
	}

	private ReduserGeografiServlet getReduserGeografiServletMedKonfigurasjon() throws Exception {
		ReduserGeografiServlet reduserGeografiServlet = initializeMocks(ReduserGeografiServlet.class);
		mockFieldValue("erTjenestenTilkoblet", Boolean.TRUE);
		return reduserGeografiServlet;
	}

	private PrintWriter getMockPrintWriter(HttpServletResponse stubResponse) throws IOException {
		PrintWriter mockPrintWriter = createMock(PrintWriter.class);
		when(stubResponse.getWriter()).thenReturn(mockPrintWriter);
		return mockPrintWriter;
	}

	@Test
	public void doGet_hvisIkkeKonfigurasjonsparameterErSatt_gisIkkeTilgangTilTjenesten() throws Exception {
		reduserGeografiServlet = getReduserGeografiServletUtenKonfigurasjon();

		reduserGeografiServlet.doGet(stubRequest, stubResponse);

		verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private ReduserGeografiServlet getReduserGeografiServletUtenKonfigurasjon() throws Exception {
		ReduserGeografiServlet reduserGeografiServlet = initializeMocks(ReduserGeografiServlet.class);
		mockFieldValue("erTjenestenTilkoblet", Boolean.FALSE);
		return reduserGeografiServlet;
	}

	@Test
	public void doGet_hvisTilgangErGitt_returnererHtmlskjema() throws Exception {
		reduserGeografiServlet.doGet(null, stubResponse);

        verify(mockPrintWriter).append(ArgumentMatchers.matches(".*Spesifiser konfigurasjon for geografi-reduksjon.*"));
	}

	@Test
	public void doPost_gikkSpesifikasjonOgValghendelse_kallerDomenetjenesteForSletting() throws Exception {
		String geografiSpesifikasjon = geografiSpesifikasjon();
		when(stubRequest.getParameter(PARAMETER_GEOGRAFISPESIFIKASJON)).thenReturn(geografiSpesifikasjon);
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("123456");
		when(stubRequest.getParameter(PARAMETER_REDUKSJONSMODUS)).thenReturn(VALUE_IKKE_SLETT);

		reduserGeografiServlet.doPost(stubRequest, stubResponse);

		verify(getInjectMock(ReduserGeografiDomainService.class)).reduserGeografi(any(), any());
        verify(mockPrintWriter).append(ArgumentMatchers.matches(".*Reduksjon av geografi er ferdigstilt.*Se i loggen for å se hva som ble utført.*"));
	}

	private String geografiSpesifikasjon() {
		return "{\n"
			+ "  \"beholdKommuner\": [],\n"
			+ "  \"beholdKretser\": [\n"
			+ "    \"0201.0000\",\n"
			+ "    \"0201.0001\",\n"
			+ "    \"0201.0002\",\n"
			+ "    \"0301.0000\",\n"
			+ "    \"0301.0001\",\n"
			+ "    \"0301.0002\",\n"
			+ "    \"0401.0000\",\n"
			+ "    \"0401.0001\"\n"
			+ "  ]\n"
			+ "}";
	}

	@Test
	public void doPost_hvisIkkeKonfigurasjonsparameterErSatt_gisIkkeTilgangTilTjenesten() throws Exception {
		reduserGeografiServlet = getReduserGeografiServletUtenKonfigurasjon();

		reduserGeografiServlet.doPost(stubRequest, stubResponse);

		verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void doPost_hvisIkkeValghendelsesIdErSatt_sendesFeilmeldingTilbake() throws Exception {
		when(stubRequest.getParameter(PARAMETER_GEOGRAFISPESIFIKASJON)).thenReturn("ikke av betydning nå");
		when(stubRequest.getParameter(PARAMETER_REDUKSJONSMODUS)).thenReturn(VALUE_FLYTT);

		reduserGeografiServlet.doPost(stubRequest, stubResponse);

        verify(mockPrintWriter).append(ArgumentMatchers.matches(".*valghendelsesid.*"));
	}

	@Test
	public void doPost_hvisIkkeGeografispesifikasjonErSatt_sendesFeilmeldingTilbake() throws Exception {
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("123456");
		when(stubRequest.getParameter(PARAMETER_REDUKSJONSMODUS)).thenReturn(VALUE_SLETT);

		reduserGeografiServlet.doPost(stubRequest, stubResponse);

        verify(mockPrintWriter).append(ArgumentMatchers.matches(".*geografispesifikasjon.*"));
	}

}
