package no.valg.eva.admin.frontend.servlets;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.common.web.EvaAdminServlet;
import no.valg.eva.admin.frontend.status.StatusPropertiesProvider;

import org.apache.log4j.Logger;

/**
 * Brukes av lastbalanserer og monitoreringsverktøy for å sjekke at applikasjonen kjører som den skal
 */
@WebServlet(value = "/status")
public class FrontendStatusServlet extends EvaAdminServlet {

	@Inject
	private StatusPropertiesProvider statusPropertiesProvider;

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(FrontendStatusServlet.class);
	}

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		sendJsonTilbake(resp, statusPropertiesProvider.getStatusProperties());
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		sendJsonTilbake(resp, statusPropertiesProvider.getStatusProperties());
	}
}
