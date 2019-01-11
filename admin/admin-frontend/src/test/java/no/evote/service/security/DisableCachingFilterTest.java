package no.evote.service.security;

import static org.mockito.Mockito.verify;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DisableCachingFilterTest extends BaseFrontendTest {

	private DisableCachingFilter filter;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		filter = initializeMocks(DisableCachingFilter.class);
		container = getServletContainer();
	}

	@Test
	public void doFilter_verifyHeaders() throws Exception {

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getResponseMock()).setHeader("Cache-control", "no-store");
		verify(container.getResponseMock()).setHeader("Pragma", "no-cache");
		container.verifyChainDoFilter();
	}

}
