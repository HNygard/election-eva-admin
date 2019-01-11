package no.valg.eva.admin.backend.reporting.jasperserver;

import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiWithTimeout;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ReportTemplateUploadStatusServletTest extends MockUtilsTestCase {

	private static final int HTML_RESPONSE_CODE_UNAVAILABLE = 503;
	
	private ReportTemplateUploadStatusServlet reportTemplateUploadStatusServlet;
	private HttpServletRequest stubRequest;
	private HttpServletResponse stubResponse;
	private PrintWriter mockPrintWriter;

	@BeforeMethod
	public void setUp() throws Exception {
		reportTemplateUploadStatusServlet = initializeMocks(ReportTemplateUploadStatusServlet.class);
		stubRequest = createMock(HttpServletRequest.class);
		stubResponse = createMock(HttpServletResponse.class);
		mockPrintWriter = getMockPrintWriter(stubResponse);
	}
	
	private PrintWriter getMockPrintWriter(HttpServletResponse stubResponse) throws IOException {
		PrintWriter mockPrintWriter = createMock(PrintWriter.class);
		when(stubResponse.getWriter()).thenReturn(mockPrintWriter);
		return mockPrintWriter;
	}

	@Test
	public void doGet_whenReportsAreUploaded_returnsReportsAreReady() throws Exception {
		JasperRestApiWithTimeout stubJasperRestApiWithTimeout = getInjectMock(JasperRestApiWithTimeout.class);
		when(stubJasperRestApiWithTimeout.getResources(any(), any())).thenReturn(null);

		reportTemplateUploadStatusServlet.doGet(stubRequest, stubResponse);

		Mockito.verify(mockPrintWriter).append(ArgumentMatchers.matches(".*Reports are ready.*"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doGet_whenReportsAreNotUploaded_returnsReportsAreUnavailable() throws Exception {
		JasperRestApiWithTimeout stubJasperRestApiWithTimeout = getInjectMock(JasperRestApiWithTimeout.class);
		when(stubJasperRestApiWithTimeout.getResources(any(), any())).thenThrow(ProcessingException.class);

		reportTemplateUploadStatusServlet.doGet(stubRequest, stubResponse);

		Mockito.verify(stubResponse).sendError(HTML_RESPONSE_CODE_UNAVAILABLE, "Reports are unavailable");
	}

	@Test
	public void doPost_whenInvoked_behavesLikeDoGet() throws Exception {
		JasperRestApiWithTimeout stubJasperRestApiWithTimeout = getInjectMock(JasperRestApiWithTimeout.class);
		when(stubJasperRestApiWithTimeout.getResources(any(), any())).thenReturn(null);

		reportTemplateUploadStatusServlet.doPost(stubRequest, stubResponse);

		Mockito.verify(mockPrintWriter).append(ArgumentMatchers.matches(".*Reports are ready.*"));
	}

}
