package no.evote.service.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

/**
 * Servlet that lists any tasks that have logged their execution to the TaskLogger singleton.
 */
@WebServlet(urlPatterns = "/tasklog")
public class TaskLoggerServlet extends HttpServlet {
	@Inject
	private TaskLogger taskLogger;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		Writer writer = response.getWriter();

		Map<String, DateTime> log = taskLogger.getLog();
		if (!log.isEmpty()) {
			writer.write("The following tasks have been logged: \n");
			for (Map.Entry<String, DateTime> entry : log.entrySet()) {
				writer.write(entry.getValue() + ": " + entry.getKey() + "\n");
			}
		} else {
			writer.write("No tasks have been logged yet. \n");
		}

		writer.flush();
	}
}
