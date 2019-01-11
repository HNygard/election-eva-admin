package no.valg.eva.admin.frontend.filter;

import static org.mockito.Mockito.verify;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;

import org.testng.annotations.Test;

public class IEModeFilterTest extends BaseFrontendTest {

	@Test
	public void doFilter_withMode_verifyHeader() throws Exception {
		IEModeFilter filter = initializeMocks(getIEModeFilter("testmode"));
		ServletContainer container = getServletContainer();

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getResponseMock()).setHeader("X-UA-Compatible", "testmode");
		container.verifyChainDoFilter();
	}

	@Test
	public void doFilter_withoutMode_verifyDefaultHeader() throws Exception {
		IEModeFilter filter = initializeMocks(getIEModeFilter(null));
		ServletContainer container = getServletContainer();

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getResponseMock()).setHeader("X-UA-Compatible", "IE=edge");
		container.verifyChainDoFilter();
	}

	private IEModeFilter getIEModeFilter(final String mode) {
		return new IEModeFilter() {
			@Override
			String modeFromEvoteProperties() {
				return mode;
			}
		};
	}

}
