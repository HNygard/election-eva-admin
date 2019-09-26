package no.valg.eva.admin.backend.web;

import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.backend.application.service.StatusApplicationService;
import no.valg.eva.admin.common.web.EvaAdminServlet;

import org.apache.log4j.Logger;

@WebServlet(urlPatterns = "/status")
public class BackendStatusServlet extends EvaAdminServlet {

	private final StatusApplicationService statusApplicationService;

	BackendStatusServlet() {
		statusApplicationService = null;
	}

	@Inject
	public BackendStatusServlet(StatusApplicationService statusApplicationService) {
		this.statusApplicationService = statusApplicationService;
	}

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(BackendStatusServlet.class);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		sendJsonTilbake(resp, lagStatus());
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		sendJsonTilbake(resp, lagStatus());
	}

	private Properties lagStatus() {
		return statusApplicationService.getStatusAndConfiguredVersionProperties();
	}
	
}
