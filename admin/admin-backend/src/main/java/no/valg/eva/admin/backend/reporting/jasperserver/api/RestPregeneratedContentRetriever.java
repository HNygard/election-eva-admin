package no.valg.eva.admin.backend.reporting.jasperserver.api;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.log4j.Logger.getLogger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

public class RestPregeneratedContentRetriever implements PregeneratedContentRetriever {
	private static final Logger LOGGER = getLogger(RestPregeneratedContentRetriever.class);
	@Inject
	private JasperRestApiNoTimeout jasperRestApiNoTimeout;

	@Override
	public byte[] tryPreGeneratedReport(String fileName) {
		Response resp = jasperRestApiNoTimeout.getPreGeneratedReportOutput(fileName);
		try {
			int status = resp.getStatus();
			switch (status) {
			case SC_OK:
				return resp.readEntity(byte[].class);
			case SC_NOT_FOUND:
				return null;
			default:
				LOGGER.error("Retrieval of pre-generated report content returned response with status " + status);
			}
		} finally {
			resp.close();
		}
		return null;
	}

	@Override
	public String getRepositoryType() {
		return "jasperserver";
	}

}
