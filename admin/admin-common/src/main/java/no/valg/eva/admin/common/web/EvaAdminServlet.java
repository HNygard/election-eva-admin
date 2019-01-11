package no.valg.eva.admin.common.web;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;

/**
 * Basisfunksjonalitet som deles mellom EVA Admin-servlets
 */
public abstract class EvaAdminServlet extends HttpServlet {

	protected abstract Logger getLogger();
	
	protected void sendHtmlTilbake(HttpServletResponse resp, String htmlBody) throws IOException {
		Writer w = getOutputWriter(resp);
		w.append(wrapInHtmlHeaders(htmlBody));
	}

	protected Writer getOutputWriter(HttpServletResponse resp) throws IOException {
		Writer w = resp.getWriter();
		resp.setContentType("text/html");
		return w;
	}

	protected String wrapInHtmlHeaders(String html) {
		return "<html><head></head><body>"
			+ html
			+ "</body></html>";
	}

	protected void sendJsonTilbake(HttpServletResponse resp, Object objekt) throws IOException {
		String json = json(objekt);
		resp.setContentType("application/json");
		resp.getWriter().append(json);
	}

	protected String json(Object object) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(object);
	}


	protected void sendNotFoundResponse(HttpServletResponse resp, String meldingTilLoggen) throws IOException {
		if (!nullEllerEmpty(meldingTilLoggen)) {
			getLogger().warn(meldingTilLoggen);
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	protected boolean nullEllerEmpty(String streng) {
		return streng == null || streng.trim().isEmpty();
	}

}
