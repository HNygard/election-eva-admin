package no.valg.eva.admin.backend.reporting.jasperserver;

import static no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApi.ResourceType.reportUnit;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiWithTimeout;

/**
 * Intensjonen med denne servlet'en er å lage en mekanisme som vår infrastruktur ellers (feks docker-script)
 * kan benytte for å finne ut om rapportopplasting er gjennomført eller ikke.
 */
@WebServlet(urlPatterns = "/reportTemplateUploadStatus")
public class ReportTemplateUploadStatusServlet extends HttpServlet {

	private static final int RETURN_CODE_UNAVAILABLE = 503;
	
	@Inject
	private JasperRestApiWithTimeout jasperRestApiWithTimeout;


	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		Writer w = getOutputWriter(resp);

		if (isReportTemplatesUploaded()) {
			w.append(wrapInHtmlHeaders("Reports are ready"));
		} else {
			resp.sendError(RETURN_CODE_UNAVAILABLE, "Reports are unavailable");
		}
	}

	private Writer getOutputWriter(HttpServletResponse resp) throws IOException {
		Writer w = resp.getWriter();
		resp.setContentType("text/html");
		return w;
	}

	private String wrapInHtmlHeaders(String html) {
		return "<html><head></head><body>"
			+ html
			+ "</body></html>";
	}

	private boolean isReportTemplatesUploaded() {
		try {
			jasperRestApiWithTimeout.getResources(reportUnit, "/reports/EVA");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
}
