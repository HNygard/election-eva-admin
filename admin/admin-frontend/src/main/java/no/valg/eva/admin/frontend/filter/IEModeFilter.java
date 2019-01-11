package no.valg.eva.admin.frontend.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import no.evote.util.EvoteProperties;

/**
 * Set the X-UA-Compatible HTTP header to prevent IE compatibility mode.
 * <p/>
 * The mode is "edge" by default, which disables compatibility modes. This can be overridden by setting the evote property
 * {@code no.valg.eva.admin.frontend.filter.IEModeFilter.mode}.
 */
public class IEModeFilter implements Filter {
	private static final String DEFAULT_MODE = "IE=edge";

	private final String mode;

	public IEModeFilter() {
		String modeFromEvoteProperties = modeFromEvoteProperties();
		if (modeFromEvoteProperties == null) {
			this.mode = DEFAULT_MODE;
		} else {
			this.mode = modeFromEvoteProperties;
		}
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		((HttpServletResponse) response).setHeader("X-UA-Compatible", mode);
		filterChain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(final FilterConfig config) throws ServletException {
	}

	String modeFromEvoteProperties() {
		return EvoteProperties.getProperty(EvoteProperties.NO_EVOTE_PRESENTATION_UTIL_FILTERS_IEMODE_FILTER_MODE, true);
	}

}
