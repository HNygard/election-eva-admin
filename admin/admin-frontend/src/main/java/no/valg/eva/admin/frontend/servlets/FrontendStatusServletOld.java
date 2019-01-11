package no.valg.eva.admin.frontend.servlets;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.common.application.service.StatusService;

import org.apache.log4j.Logger;

/**
 * Used by load balancer to verify that the services are up and running.
 */
@WebServlet(value = "/status.html")
@Deprecated // Bruk FrontendStatusServlet i stedet
public class FrontendStatusServletOld extends HttpServlet {

	private static final String ERROR = "IN ERROR";
	private static final Logger LOG = Logger.getLogger(FrontendStatusServletOld.class);
	@Inject
	private StatusService statusService;

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		try {
			res.getWriter().write(statusService.getStatus());
		} catch (final IllegalStateException ex) {
			LOG.fatal("Unable to access EJB.", ex);
			res.getWriter().write(ERROR);
		}
	}
}
