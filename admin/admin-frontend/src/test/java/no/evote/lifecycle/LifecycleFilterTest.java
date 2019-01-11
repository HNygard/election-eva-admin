package no.evote.lifecycle;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;

import no.evote.service.security.SystemPasswordService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.frontend.status.StatusPropertiesProvider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LifecycleFilterTest extends BaseFrontendTest {

	@Test
	public void init_withSystemOK_verifyStatusEnabled() throws Exception {
		LifecycleFilter filter = initializeMocks(LifecycleFilter.class);
		ServletContainer container = getServletContainer();
		stubIsPasswordSet(true);
		stubSameVersion();

		filter.init(createMock(FilterConfig.class));
		FilterChain chain = createMock(FilterChain.class);
		filter.doFilter(container.getRequestMock(), container.getResponseMock(), chain);

		verify(chain).doFilter(container.getRequestMock(), container.getResponseMock());
	}

	@Test(dataProvider = "doFilter")
	public void doFilter_withDataProvider_verifyExpected(boolean isPasswordSet, String expected) throws Exception {
		LifecycleFilter filter = initializeMocks(LifecycleFilter.class);
		ServletContainer container = getServletContainer();
		stubIsPasswordSet(isPasswordSet);
		stubSameVersion();

		FilterChain chain = createMock(FilterChain.class);
		filter.doFilter(container.getRequestMock(), container.getResponseMock(), chain);

		verify(container.getResponseMock()).reset();
		verify(container.getResponseMock()).setContentType("text/plain");
		verify(container.getResponseMock().getWriter()).append(expected);
		verify(container.getResponseMock().getWriter()).flush();
	}

	@DataProvider(name = "doFilter")
	public Object[][] doFilter() {
		return new Object[][] {
				{ false, "System passphrase has not been entered, unable to continue." }
		};
	}

	@Test(dataProvider = "versionData")
	public void doFilter_givenVersionData_verifiesSameVersionOnFrontendAndBackend(String frontendVersion, String backendVersion, boolean expectedToPass) throws Exception {
		
		LifecycleFilter filter = initializeMocks(LifecycleFilter.class);
		ServletContainer container = getServletContainer();
		stubIsPasswordSet(true);
		stubVersions(frontendVersion, backendVersion);

		filter.init(createMock(FilterConfig.class));
		FilterChain chain = createMock(FilterChain.class);
		filter.doFilter(container.getRequestMock(), container.getResponseMock(), chain);

		if (expectedToPass) {
			verify(chain).doFilter(container.getRequestMock(), container.getResponseMock());
		} else {
			verify(container.getResponseMock()).reset();
			verify(container.getResponseMock()).setContentType("text/plain");
			verify(container.getResponseMock().getWriter()).append("Frontend and backend versions are different, unable to continue.");
			verify(container.getResponseMock().getWriter()).flush();
		}
	}

	@DataProvider
	private Object[][] versionData() {
		return new Object[][] {
			{ "2019.4.1234", "2019.4.1234", true },
			{ "2019.4-SNAPSHOT", "2019.4-SNAPSHOT", true },
			{ "2019.4.1234", "2019.5.2345", false }
		};
	}

	private void stubSameVersion() {
		stubVersions("same", "same");
	}

	private void stubVersions(String frontendVersion, String backendVersion) {
		Properties properties = new Properties();
		properties.setProperty("frontend-version", frontendVersion);
		properties.setProperty("backend-version", backendVersion);
		when(getInjectMock(StatusPropertiesProvider.class).getStatusProperties()).thenReturn(properties);
	}

	private void stubIsPasswordSet(boolean isPasswordSet) {
		when(getInjectMock(SystemPasswordService.class).isPasswordSet()).thenReturn(isPasswordSet);
	}

}
