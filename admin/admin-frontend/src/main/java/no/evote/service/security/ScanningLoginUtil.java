package no.evote.service.security;

import javax.servlet.http.HttpServletRequest;

public final class ScanningLoginUtil {
	public static final String SELECT_ELECTION_EVENT = "scanningLoginSelectElectionEvent.xhtml";
	public static final String SUCCESSFUL_LOGIN = "scanningLoginSuccessful.xhtml";
	
	private ScanningLoginUtil() {
	}

	public static boolean isScanningLogin(HttpServletRequest request) {
		String servletPath = request.getServletPath();

		return servletPath.contains(SELECT_ELECTION_EVENT)
				|| servletPath.contains(SUCCESSFUL_LOGIN);
	}

	public static String startPage() {
		return SELECT_ELECTION_EVENT;
	}
}
