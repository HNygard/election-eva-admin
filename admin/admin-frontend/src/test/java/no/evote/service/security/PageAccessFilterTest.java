package no.evote.service.security;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class PageAccessFilterTest extends BaseFrontendTest {

	private PageAccessFilter filter;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		filter = initializeMocks(PageAccessFilter.class);
		Instance<UserData> userDataInstance = mockInstance("userDataInstance", UserData.class);
		container = getServletContainer();

		container.setContextPath("/secure");
		when(getInjectMock(PageAccess.class).hasAccess(userDataInstance.get(), "hasAccess.xhtml")).thenReturn(true);
		when(getInjectMock(PageAccess.class).hasAccess(userDataInstance.get(), "noAccess.xhtml")).thenReturn(false);
	}

	@Test
	public void doFilter_withAccess_checkChainFiltering() throws Exception {
		container.setRequestURI("hasAccess.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		container.verifyChainDoFilter();
	}

	@Test
	public void doFilter_withNoAccess_shouldReturn401() throws Exception {
		container.setRequestURI("noAccess.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getResponseMock()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(container.getResponseMock()).getWriter();
	}

	@Test
	public void doFilter_withNoAccessAndAjax_shouldReturn401() throws Exception {
		container.turnOnAjax();
		container.setRequestURI("noAccess.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getResponseMock()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

}
