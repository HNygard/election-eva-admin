package no.valg.eva.admin.frontend.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Instance;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;

import no.valg.eva.admin.frontend.filter.WelcomePageFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WelcomePageFilterTest extends BaseFrontendTest {

	private WelcomePageFilter filter;
	private Instance<UserData> userDataInstance;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		filter = initializeMocks(WelcomePageFilter.class);
		userDataInstance = mockInstance("userDataInstance", UserData.class);
		container = getServletContainer();
	}

	@Test
	public void doFilter_withWelcomePageButNoUser_verifyFilterChainDoFilter() throws Exception {
		container.setRequestURI("/welcome.xhtml");
		when(userDataInstance.get()).thenReturn(null);

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		container.verifyChainDoFilter();
	}

	@Test
	public void doFilter_withWelcomePageAndUser_verifyRedirect() throws Exception {
		container.setRequestURI("/welcome.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getResponseMock()).sendRedirect("/secure/index.xhtml");
	}

	@Test
	public void doFilter_withOtherPage_verifyFilterChainDoFilter() throws Exception {
		container.setRequestURI("/other.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		container.verifyChainDoFilter();
	}
}
