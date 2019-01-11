package no.valg.eva.admin.frontend.filter;

import java.io.IOException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.security.UserData;

/**
 * Dette webfilteret sender innloggede brukere til Min Side.
 */
public class WelcomePageFilter implements Filter {

	private static final String WELCOME_PAGE = "/welcome.xhtml";
	private static final String MIN_SIDE = "/secure/index.xhtml";

	@Inject
	private Instance<UserData> userDataInstance;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

		if (isWelcomePageRequest(httpRequest) && userHasLoggedIn()) {
			httpResponse.sendRedirect(MIN_SIDE);
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	private boolean isWelcomePageRequest(HttpServletRequest httpRequest) {
		String requestURI = httpRequest.getRequestURI();
		return WELCOME_PAGE.equals(requestURI);
	}

	private boolean userHasLoggedIn() {
		UserData userData = userDataInstance.get();
		return userData != null;
	}

	@Override
	public void destroy() {
	}
}
