package no.evote.presentation.exceptions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ResourceNotFound", value = "/404")
public class ResourceNotFoundServlet extends HttpServlet {


	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse res) {
		String message = req.getAttribute("javax.servlet.error.message").toString();
		ErrorPageRenderer.renderError(req, res, ErrorPageRenderer.Error.NOT_FOUND, message, message);
	}
}
