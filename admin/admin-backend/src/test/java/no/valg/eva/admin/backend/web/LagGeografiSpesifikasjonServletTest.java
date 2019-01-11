package no.valg.eva.admin.backend.web;

import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.service.GeografiSpesifikasjonDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentMatchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.PARAMETER_ANNTALL_KRETSER;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.PARAMETER_EKSPORTTYPE;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.PARAMETER_VALGHENDELSE_ID;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.VALUE_ALLE_KOMMUNER;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.VALUE_ALLE_KRETSER;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.VALUE_ALLE_STEMMESTEDER;
import static no.valg.eva.admin.backend.web.LagGeografiSpesifikasjonServlet.VALUE_BEGRENSET_ANTALL_KRETSER;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LagGeografiSpesifikasjonServletTest extends MockUtilsTestCase {

	private LagGeografiSpesifikasjonServlet lagGeografiSpesifikasjonServlet;
	private HttpServletRequest stubRequest;
	private HttpServletResponse stubResponse;
	private PrintWriter mockPrintWriter;

	@BeforeMethod
	public void setUp() throws Exception {
		lagGeografiSpesifikasjonServlet = getLagGeografiSpesifikasjonServletMedKonfigurasjon();
		stubRequest = createMock(HttpServletRequest.class);
		stubResponse = createMock(HttpServletResponse.class);
		mockPrintWriter = getMockPrintWriter(stubResponse);
	}

	private LagGeografiSpesifikasjonServlet getLagGeografiSpesifikasjonServletMedKonfigurasjon() throws Exception {
		LagGeografiSpesifikasjonServlet lagGeografiSpesifikasjonServlet = initializeMocks(LagGeografiSpesifikasjonServlet.class);
		mockFieldValue("erTjenestenTilkoblet", Boolean.TRUE);
		return lagGeografiSpesifikasjonServlet;
	}

	private PrintWriter getMockPrintWriter(HttpServletResponse stubResponse) throws IOException {
		PrintWriter mockPrintWriter = createMock(PrintWriter.class);
		when(stubResponse.getWriter()).thenReturn(mockPrintWriter);
		return mockPrintWriter;
	}

	@Test
	public void doGet_hvisTilgangErGitt_returnererHtmlskjema() throws Exception {
		lagGeografiSpesifikasjonServlet.doGet(null, stubResponse);

		verify(mockPrintWriter).append(ArgumentMatchers.matches(".*Velg hva slags type geografi-spesifikasjon du vil ha.*"));
	}

	@Test
	public void doGet_hvisIkkeKonfigurasjonsparameterErSatt_gisIkkeTilgangTilTjenesten() throws Exception {
		lagGeografiSpesifikasjonServlet = getLagGeografiSpesifikasjonServletUtenKonfigurasjon();

		lagGeografiSpesifikasjonServlet.doGet(stubRequest, stubResponse);

		verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private LagGeografiSpesifikasjonServlet getLagGeografiSpesifikasjonServletUtenKonfigurasjon() throws Exception {
		LagGeografiSpesifikasjonServlet lagGeografiSpesifikasjonServlet = initializeMocks(LagGeografiSpesifikasjonServlet.class);
		mockFieldValue("erTjenestenTilkoblet", Boolean.FALSE);
		return lagGeografiSpesifikasjonServlet;
	}

	@Test
	public void doPost_hvisAlleKommunerErValgt_returnererListeMedAlleKommuner() throws Exception {
		when(stubRequest.getParameter(PARAMETER_EKSPORTTYPE)).thenReturn(VALUE_ALLE_KOMMUNER);
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("111111");
		when(getInjectMock(GeografiSpesifikasjonDomainService.class).lagGeografiSpesifikasjonForKommuner(any())).thenReturn(geografiSpesifikasjon());

		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(geografiSpesifikasjonString());
	}

	private GeografiSpesifikasjon geografiSpesifikasjon() {
		return new GeografiSpesifikasjon(singletonList("0101"), asList("0201.0000", "0201.0001"));
	}

	private String geografiSpesifikasjonString() {
		return "{\n"
			+ "  \"beholdKommuner\": [\n"
			+ "    \"0101\"\n"
			+ "  ],\n"
			+ "  \"beholdKretser\": [\n"
			+ "    \"0201.0000\",\n"
			+ "    \"0201.0001\"\n"
			+ "  ],\n"
			+ "  \"beholdStemmesteder\": []\n"
			+ "}";
	}

	@Test
	public void doPost_hvisAlleKretseErValgt_returnererListeMedAlleKretser() throws Exception {
		when(stubRequest.getParameter(PARAMETER_EKSPORTTYPE)).thenReturn(VALUE_ALLE_KRETSER);
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("111111");
		when(getInjectMock(GeografiSpesifikasjonDomainService.class).lagGeografiSpesifikasjonForKretser(any())).thenReturn(geografiSpesifikasjon());

		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(geografiSpesifikasjonString());
	}

	@Test
	public void doPost_hvisBegrensetAntallKretseErValgt_returnererListeMedMaksAntallKretserValgt() throws Exception {
		when(stubRequest.getParameter(PARAMETER_EKSPORTTYPE)).thenReturn(VALUE_BEGRENSET_ANTALL_KRETSER);
		when(stubRequest.getParameter(PARAMETER_ANNTALL_KRETSER)).thenReturn("2");
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("111111");
		when(getInjectMock(GeografiSpesifikasjonDomainService.class).lagGeografiSpesifikasjonForBegrensetAntallKretser(any(), anyInt()))
			.thenReturn(geografiSpesifikasjon());

		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(geografiSpesifikasjonString());
	}

	@Test
	public void doPost_hvisAlleStemmestederErValgt_returnererListeMedAlleStemmesteder() throws Exception {
		when(stubRequest.getParameter(PARAMETER_EKSPORTTYPE)).thenReturn(VALUE_ALLE_STEMMESTEDER);
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("111111");
		when(getInjectMock(GeografiSpesifikasjonDomainService.class).lagGeografiSpesifikasjonForStemmesteder(any())).thenReturn(geografiSpesifikasjon());

		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(geografiSpesifikasjonString());
	}

	@Test
	public void doPost_hvisIkkeKonfigurasjonsparameterErSatt_gisIkkeTilgangTilTjenesten() throws Exception {
		lagGeografiSpesifikasjonServlet = getLagGeografiSpesifikasjonServletUtenKonfigurasjon();
		
		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void doPost_hvisIkkeValghendelsesIdErSatt_sendesFeilmeldingTilbake() throws Exception {
		when(stubRequest.getParameter(PARAMETER_EKSPORTTYPE)).thenReturn(VALUE_ALLE_KRETSER);
		
		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(ArgumentMatchers.matches(".*må ha skrevet inn valghendelsesid.*"));
	}

	@Test
	public void doPost_hvisIkkeEksporttypeErValgt_sendesFeilmeldingTilbake() throws Exception {
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("123456");
		
		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(ArgumentMatchers.matches(".*eksporttype.*"));
	}

	@Test
	public void doPost_hvisBegrensetEksportUtenAntallKretserErValgt_sendesFeilmeldingTilbake() throws Exception {
		when(stubRequest.getParameter(PARAMETER_EKSPORTTYPE)).thenReturn(VALUE_BEGRENSET_ANTALL_KRETSER);
		when(stubRequest.getParameter(PARAMETER_VALGHENDELSE_ID)).thenReturn("123456");
		
		lagGeografiSpesifikasjonServlet.doPost(stubRequest, stubResponse);

		verify(mockPrintWriter).append(ArgumentMatchers.matches(".*antall kretser.*"));
	}

}
