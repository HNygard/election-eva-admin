package no.valg.eva.admin.frontend.reporting;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static no.evote.constants.EvoteConstants.MIME_TYPES;

import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.common.reporting.model.ReportExecution;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@WebServlet(ReportContentDownloadServlet.SECURE_REPORTING_REPORT_CONTENT_URL)
public class ReportContentDownloadServlet extends HttpServlet {
	private static final Logger LOG = Logger.getLogger(ReportContentDownloadServlet.class);
	public static final String SECURE_REPORTING_REPORT_CONTENT_URL = "/secure/reporting/reportContent";
	public static final int SIZE = 100;
	public static final int TEN = 10;
	private static final Cache<String, ReportExecution> FILE_CACHE = CacheBuilder.newBuilder().maximumSize(SIZE).expireAfterWrite(TEN, SECONDS).build();

	public void storeReportContentFileInCache(@Observes ReportExecution reportExecution) {
		getCache().put(reportExecution.getRequestId(), reportExecution);
	}

	public void clearFileCache(@Observes CleanReportCacheEvent cleanReportCacheEvent) {
		if (getCache().size() > 0) {
			LOG.debug(format("Clearing report content cache. There were %d items in the cache.", getCache().size()));
			getCache().cleanUp();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestId = req.getParameter("requestId");
		if (StringUtils.isBlank(requestId)) {
			resp.setStatus(SC_BAD_REQUEST);
			return;
		}
		ReportExecution reportExecution = getCache().getIfPresent(requestId);
		if (reportExecution != null) {
			try (ServletOutputStream out = resp.getOutputStream();) {
				resp.setContentType(MIME_TYPES.get(reportExecution.getFormat()));
				resp.addHeader("Content-Disposition", getContentDisposition(reportExecution.getFormat()) + "; filename=\"" + reportExecution.getFileName() + "\"");
				byte[] content = reportExecution.getContent();
				resp.setContentLength(content.length);
				out.write(content);
				LOG.debug(format("Served %s (%d bytes) from report content cache", reportExecution.getFileName(), content.length));
			}
		} else {
			resp.setStatus(SC_NOT_FOUND);
			LOG.error(format("Invalid report file requestId: %s", requestId));
		}
	}
	
	private String getContentDisposition(String format) {
		return "html".equals(format) ? "inline" : "attachment";
	}

	Cache<String, ReportExecution> getCache() {
		return FILE_CACHE;
	}
}
