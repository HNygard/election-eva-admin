package no.valg.eva.admin;

import org.apache.commons.lang3.ArrayUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServletContainer {

	private BaseFrontendTest test;
	private HttpSession httpSessionMock;
	private HttpServletRequest requestMock;
	private HttpServletResponse responseMock;
	private FilterChain filterChainMock;
	private ServletContext servletContextMock;

	public ServletContainer(BaseFrontendTest test) {
		this.test = test;
		requestMock = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
		responseMock = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
		filterChainMock = mock(FilterChain.class, RETURNS_DEEP_STUBS);
		servletContextMock = mock(ServletContext.class, RETURNS_DEEP_STUBS);
		httpSessionMock = mock(HttpSession.class, RETURNS_DEEP_STUBS);
		when(requestMock.getSession()).thenReturn(httpSessionMock);
		when(requestMock.getSession(anyBoolean())).thenReturn(httpSessionMock);
		when(test.getFacesContextMock().getExternalContext().getSession(anyBoolean())).thenReturn(httpSessionMock);
		when(test.getFacesContextMock().getExternalContext().getRequest()).thenReturn(requestMock);
		when(test.getFacesContextMock().getExternalContext().getResponse()).thenReturn(responseMock);
		when(requestMock.getServletContext()).thenReturn(servletContextMock);
		when(test.getFacesContextMock().getExternalContext().getContext()).thenReturn(servletContextMock);
		when(requestMock.getParameterMap()).thenReturn(new HashMap<>());
		when(test.getFacesContextMock().getExternalContext().getRequestParameterMap()).thenReturn(new HashMap<>());
		when(test.getFacesContextMock().getExternalContext().getRequestParameterValuesMap()).thenReturn(new HashMap<>());
	}

	public void verifyChainDoFilter() throws Exception {
		verify(filterChainMock).doFilter(requestMock, responseMock);
	}

	public void verifyRedirect(String uri) throws IOException {
		verify(test.getFacesContextMock().getExternalContext()).redirect(uri);
	}

	public void turnOnAjax() {
		setHeader("X-Requested-With", "XMLHttpRequest");
	}

	public void setUserAgent(String s) {
		setHeader("User-Agent", s);
	}

	public void setHeader(String key, String value) {
		when(requestMock.getHeader(key)).thenReturn(value);
	}

	public void setRequestURI(String uri) {
		when(requestMock.getRequestURI()).thenReturn(uri);
	}

	public void setMethod(String method) {
		when(requestMock.getMethod()).thenReturn(method);
	}

	public void setContextPath(String path) {
		when(requestMock.getContextPath()).thenReturn(path);
		when(requestMock.getServletContext().getContextPath()).thenReturn(path);
	}

	public void setRequestParameter(String key, Object value) {
		setRequestParameter(key, value != null ? value.toString() : null);
	}

	public void setRequestParameter(String key, String value) {
		when(requestMock.getParameter(key)).thenReturn(value);
		Map<String, String[]> map = requestMock.getParameterMap();
		Map<String, String> singleMap = test.getFacesContextMock().getExternalContext().getRequestParameterMap();
		Map<String, String[]> valuesMap = test.getFacesContextMock().getExternalContext().getRequestParameterValuesMap();
		if (map.containsKey(key)) {
			if (value == null) {
				map.remove(key);
				valuesMap.remove(key);
			} else {
				map.put(key, ArrayUtils.addAll(map.get(key), value));
				valuesMap.put(key, ArrayUtils.addAll(map.get(key), value));
			}
		} else {
			map.put(key, new String[] { value });
			valuesMap.put(key, new String[] { value });
		}
		singleMap.put(key, value);
	}

	public void setServletContextPath(String path) {
		when(requestMock.getServletContext().getContextPath()).thenReturn(path);
	}

	public void setServletPath(String path) {
		when(requestMock.getServletPath()).thenReturn(path);
		when(test.getFacesContextMock().getExternalContext().getRequestServletPath()).thenReturn(path);
	}

	public void setRemoteAddr(String addr) {
		when(requestMock.getRemoteAddr()).thenReturn(addr);
	}

	public void setQueryString(String queryString) {
		when(requestMock.getQueryString()).thenReturn(queryString);
	}

	public HttpSession getHttpSessionMock() {
		return httpSessionMock;
	}

	public HttpServletRequest getRequestMock() {
		return requestMock;
	}

	public HttpServletResponse getResponseMock() {
		return responseMock;
	}

	public FilterChain getFilterChainMock() {
		return filterChainMock;
	}

	public ServletContext getServletContextMock() {
		return servletContextMock;
	}
}
