package no.valg.eva.admin.example;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExampleServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String op = req.getParameter("op");
		if (op == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if ("create".equals(op)) {
			String uri = req.getRequestURI();
			if (uri.contains("error")) {
				resp.sendRedirect("/somewhere");
				return;
			}
			req.getSession().setAttribute("foo", "bar");
			if (isAjax(req)) {
				resp.getWriter().print("ajax");
			} else {
				resp.getWriter().print("page");
			}
		}
	}

	private boolean isAjax(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
}
