package no.valg.eva.admin.frontend.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import no.evote.security.UserData;

/**
 * Explicitly set the locale to what the user has selected, to avoid lookups on resource bundles that we haven't loaded. Specifically, this is a problem with
 * validation messages (supposedly fixed in newer versions of Hibernate)
 */
public class ForceLocaleFilter implements Filter {

	@Inject
	private Instance<UserData> userDataInstance;

	private class LocaleWrapper extends HttpServletRequestWrapper {
		public LocaleWrapper(final HttpServletRequest request) {
			super(request);
		}

		@Override
		public Enumeration<Locale> getLocales() {
			UserData userData = userDataInstance.get();

			List<Locale> locales = new ArrayList<>();
			if (userData == null || userData.getJavaLocale() == null) {
				return super.getLocales();
			} else {
				locales.add(userData.getJavaLocale());
			}

			return Collections.enumeration(locales);
		}
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		filterChain.doFilter(new LocaleWrapper((HttpServletRequest) request), response);
	}

	@Override
	public void destroy() {
		// To conform with interface
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
		// To conform with interface
	}

}
