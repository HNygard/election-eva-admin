package no.valg.eva.admin.backend.reporting.jasperserver;

import java.io.IOException;
import java.io.Writer;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.util.EvoteProperties;

@WebServlet("/uploadReportTemplates")
public class TemplateUploadTriggerServlet extends HttpServlet {
	@Inject
	private Event<UploadReportTemplatesEvent> uploadReportTemplatesEventEvent;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (Boolean.valueOf(EvoteProperties.getProperty(EvoteProperties.JASPERSERVER_ENABLE_REPORT_TEMPLATES_UPLOAD_TRIGGER_SERVLET, "false"))) {
			uploadReportTemplatesEventEvent.fire(new UploadReportTemplatesEvent());
			Writer w = resp.getWriter();
			resp.setContentType("text/html");
			w.append("Uploading report templates...\n");
		}
	}
}
