package no.evote.service.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Disables caching on files under /secure/, to stop browsers from accidentally caching secure data.
 */
@WebFilter(urlPatterns = { "/secure/*" })
public class DisableCachingFilter implements Filter {
	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		response.setHeader("Cache-control", "no-store");
		response.setHeader("Pragma", "no-cache");

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// To conform with interface

	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// To conform with interface
	}
}
