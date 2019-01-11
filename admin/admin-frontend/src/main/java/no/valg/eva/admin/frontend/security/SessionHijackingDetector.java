package no.valg.eva.admin.frontend.security;

import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.auditevents.HttpSessionHijackedAuditEvent;

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
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * This filter detects if the user's identity is different in the application server session and the OIDC.
 * 
 * The functionality is implemented in a separate filter to {@link OidcFilter}, to reduce complexity. UserData is expected to exist.
 */
public class SessionHijackingDetector implements Filter {

	static final String OIDC_CLAIM_PID = "OIDC_CLAIM_pid";
	@Inject
	private Instance<UserDataProducer> userDataProducerInstance;
	@Inject
	private AuditLogService auditLogService;

	private final TmpLoginDetector tmpLoginDetector;

	@SuppressWarnings("unused")
	public SessionHijackingDetector() {
		tmpLoginDetector = new TmpLoginDetector();
	}

	SessionHijackingDetector(Instance<UserDataProducer> userDataProducerInstance, AuditLogService auditLogService, TmpLoginDetector tmpLoginDetector) {
		this.userDataProducerInstance = userDataProducerInstance;
		this.auditLogService = auditLogService;
		this.tmpLoginDetector = tmpLoginDetector;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
		UserData userData = userDataProducerInstance.get().getUserData();

		if (tmpLoginDetector.isTmpLoginEnabled()) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		String uidFromOicdSession = httpRequest.getHeader(OIDC_CLAIM_PID);
		String uidFromApplicationServer = userData.getUid();

		if (uidFromApplicationServer.equals(uidFromOicdSession)) {
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			auditLogService.addToAuditTrail(new HttpSessionHijackedAuditEvent(userData, uidFromOicdSession));
			httpResponse.sendError(SC_UNAUTHORIZED);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	    //Nothing to do
	}

	@Override
	public void destroy() {
        //Nothing to do
    }
}
