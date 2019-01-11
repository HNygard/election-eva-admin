package no.valg.eva.admin.frontend.reporting;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.reporting.model.ReportExecution;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.cache.Cache;



public class ReportContentDownloadServletTest extends BaseFrontendTest {

	@Test
	public void doGet_withMissingRequestId_returnsBadRequest() throws Exception {
		ReportContentDownloadServlet servlet = initializeMocks(ReportContentDownloadServlet.class);

		servlet.doGet(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());

		verify(getServletContainer().getResponseMock()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void doGet_withRequestIdNoInFileCache_returnsNotFound() throws Exception {
		ReportContentDownloadServlet servlet = initializeMocks(ReportContentDownloadServlet.class);
		getServletContainer().setRequestParameter("requestId", "100");

		servlet.doGet(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());

		verify(getServletContainer().getResponseMock()).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test(dataProvider = "doGet")
	public void doGet_withDataProvider_verifyExpected(String format, String expectedContentType, String expectedContentDisp) throws Exception {
		ReportContentDownloadServlet servlet = initializeMocks(ReportContentDownloadServlet.class);
		getServletContainer().setRequestParameter("requestId", "123");
		Map<String, String> map = new HashMap<>();
		ReportExecution execution = new ReportExecution("123", "content".getBytes(), "reportName", "filename." + format, format, map, map);
		servlet.storeReportContentFileInCache(execution);

		servlet.doGet(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());

		HttpServletResponse response = getServletContainer().getResponseMock();
		verify(response).setContentType(expectedContentType);
		verify(response).addHeader("Content-Disposition", expectedContentDisp);
	}

	@DataProvider(name = "doGet")
	public Object[][] doGet() {
		return new Object[][] {
				{ "pdf", "application/pdf", "attachment; filename=\"filename.pdf\"" },
				{ "html", "text/html", "inline; filename=\"filename.html\"" },
		};
	}

	@Test
	public void clearFileCache() throws Exception {
		MyServlet servlet = initializeMocks(new MyServlet());
		when(servlet.cache.size()).thenReturn(10L);

		servlet.clearFileCache(createMock(CleanReportCacheEvent.class));

		verify(servlet.getCache()).cleanUp();
	}

	class MyServlet extends ReportContentDownloadServlet {

		private Cache<String, ReportExecution> cache;

		public MyServlet() {
			this.cache = createMock(Cache.class);
		}

		@Override
		Cache<String, ReportExecution> getCache() {
			return cache;
		}
	}

}

