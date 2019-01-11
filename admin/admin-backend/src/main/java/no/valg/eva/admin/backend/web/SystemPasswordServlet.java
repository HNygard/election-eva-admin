package no.valg.eva.admin.backend.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.backend.application.service.SystemPasswordApplicationService;
import no.valg.eva.admin.common.web.EvaAdminServlet;

import org.apache.log4j.Logger;

@WebServlet(urlPatterns = "/systemPassword")
public class SystemPasswordServlet extends EvaAdminServlet {

	@Inject
	private SystemPasswordApplicationService systemPasswordApplicationService;

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(SystemPasswordServlet.class);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (!systemPasswordApplicationService.isPasswordSet()) {
			sendHtmlTilbake(resp, "<form method=\"POST\">" + "System password: <input type=\"text\" name=\"password\" /> " + "<br/>"
					+ "<input type=\"submit\" value=\"Set password\"/>" + "</form>");
		} else {
			sendHtmlTilbake(resp, "Password has already been set");
		}
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		String password = req.getParameter("password");
		if (!systemPasswordApplicationService.isPasswordSet() && systemPasswordIsCorrect(password)) {
			systemPasswordApplicationService.setSystemPassword(password);
			sendHtmlTilbake(resp, "Password is set");
		} else if (!systemPasswordApplicationService.isPasswordSet() && !systemPasswordIsCorrect(password)) {
			sendHtmlTilbake(resp, "The entered password was wrong!");
		} else if (systemPasswordApplicationService.isPasswordSet()) {
			sendHtmlTilbake(resp, "Password has already been set");
		}
	}

	private boolean systemPasswordIsCorrect(final String password) {
		return systemPasswordApplicationService.isPasswordCorrect(password);
	}
}
