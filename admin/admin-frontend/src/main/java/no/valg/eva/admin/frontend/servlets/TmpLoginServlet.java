package no.valg.eva.admin.frontend.servlets;

import no.evote.security.SecurityLevel;
import no.valg.eva.admin.frontend.security.TmpLoginDetector;
import no.valg.eva.admin.frontend.security.TmpLoginForm;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;

@WebServlet("/tmpLogin")
public class TmpLoginServlet extends HttpServlet {

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		if (getTmpLoginDetector().isTmpLoginEnabled()) {
			invalidateSessionIfExists(req);

			Writer w = resp.getWriter();
			resp.setContentType("text/html");

			w.append("<html>" + "<head><link type=\"text/css\" href=\"/javax.faces.resource/all.css.xhtml?ln=css&rv=4.0%20(90e94bd)\" "
					+ "rel=\"stylesheet\"></head>" + "<body id=\"page-tmplogin\"> <form method=\"POST\" action=\"" + getFormAction(req) + "\">"
					+ "Bruker ID: <input type=\"text\" name=\"username\" autofocus=\"autofocus\"/> " + "<br/>"
					+ "Security level: <input type=\"text\" name=\"secLevel\" value=\"3\"/> " + "<br/>" + "<input type=\"submit\" value=\"Login\"/>"
					+ "</form>" + "</body>" + "</html>");
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

		TmpLoginForm form = new TmpLoginForm(
				req.getParameter("username"),
				SecurityLevel.fromLevel(Integer.parseInt(req.getParameter("secLevel"))),
				"true".equals(req.getParameter("scanning")));
		req.getSession().setAttribute(TmpLoginForm.class.getName(), form);

		resp.sendRedirect("/secure/selectRole.xhtml");
	}

	private String getFormAction(HttpServletRequest request) {
		boolean scanning = "true".equals(request.getParameter("scanning"));
		return "/tmpLogin?scanning=" + scanning;
	}

	private void invalidateSessionIfExists(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	TmpLoginDetector getTmpLoginDetector() {
		return new TmpLoginDetector();
	}
}
