package no.valg.eva.admin.frontend.filter;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import org.mockito.ArgumentMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ForceLocaleFilterTest extends BaseFrontendTest {

	private ForceLocaleFilter filter;
	private Instance<UserData> userDataInstance;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		filter = initializeMocks(ForceLocaleFilter.class);
		userDataInstance = mockInstance("userDataInstance", UserData.class);
		container = getServletContainer();
	}

	@Test
	public void doFilter_withUserDataLocale_verifyUserLocale() throws Exception {
		setupDoFilter(Locale.CANADA);

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getFilterChainMock()).doFilter(argThat(new ArgumentMatcher<HttpServletRequest>() {
			@Override
			public boolean matches(HttpServletRequest wrapper) {
				Enumeration<Locale> locales = wrapper.getLocales();
				return !locales.hasMoreElements() || locales.nextElement().equals(Locale.CANADA);
			}
		}), any(HttpServletResponse.class));

	}

	@Test
	public void doFilter_withoutUserDataLocale_verifyRequestLocales() throws Exception {
		setupDoFilter(null);

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getFilterChainMock()).doFilter(argThat(new ArgumentMatcher<HttpServletRequest>() {
			@Override
			public boolean matches(HttpServletRequest wrapper) {
				Enumeration<Locale> locales = wrapper.getLocales();
				return !locales.hasMoreElements() || locales.nextElement().equals(Locale.ENGLISH);
			}
		}), any(HttpServletResponse.class));

	}

	private void setupDoFilter(Locale locale) {
		if (locale == null) {
			List<Locale> locales = new ArrayList<>();
			locales.add(Locale.ENGLISH);
			when(container.getRequestMock().getLocales()).thenReturn(Collections.enumeration(locales));
		} else {
			when(userDataInstance.get().getJavaLocale()).thenReturn(locale);
		}
	}
}
