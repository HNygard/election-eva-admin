package no.evote.service.security;

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

import no.evote.presentation.exceptions.ErrorPageRenderer;
import no.evote.security.UserData;
import no.valg.eva.admin.frontend.security.PageAccess;

public class PageAccessFilter implements Filter {

	@Inject
	private Instance<UserData> userDataInstance;
	@Inject
	private PageAccess pageAccess;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		UserData userData = userDataInstance.get();

		if (!pageAccess.hasAccess(userData, req.getRequestURI())) {
			String messsage = userData.getUid() + " is unauthorized to view " + req.getRequestURI();
			ErrorPageRenderer.renderError(req, (HttpServletResponse) response, ErrorPageRenderer.Error.UNAUTHORIZED, messsage, messsage);
			return;
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// To conform with interface
	}

	@Override
	public void destroy() {
		// To conform with interface
	}

}
