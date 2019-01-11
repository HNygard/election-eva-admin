package no.valg.eva.admin.frontend.security;

import static no.evote.service.security.SelectRoleFilter.SELECT_ROLE_PAGE;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
import javax.servlet.http.HttpSession;

import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.evote.service.TranslationService;
import no.evote.service.rbac.OperatorService;
import no.evote.service.security.ScanningLoginUtil;
import no.evote.service.security.SelectRoleFilter;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.OperatorLoginAuditEvent;

import org.apache.log4j.Logger;

/**
 * Servlet filter handling /tmpLogin. Works in conjunction with TmpLoginServlet.
 * 
 * @see no.valg.eva.admin.frontend.servlets.TmpLoginServlet
 */
public class TmpLoginFilter implements Filter {

	private static final Logger LOGGER = Logger.getLogger(TmpLoginFilter.class);

	private Instance<UserDataProducer> userDataProducerInstance;
	private OperatorService operatorService;
	private AuditLogService auditLogService;
	private TranslationService translationService;
	private TmpLoginDetector tmpLoginDetector;

	public TmpLoginFilter() {
	}

	@Inject
	public TmpLoginFilter(Instance<UserDataProducer> userDataProducerInstance,
			OperatorService operatorService,
			AuditLogService auditLogService,
			TranslationService translationService) {
		this.userDataProducerInstance = userDataProducerInstance;
		this.operatorService = operatorService;
		this.auditLogService = auditLogService;
		this.translationService = translationService;

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		tmpLoginDetector = getTmpLoginDetector();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (skipFilter(request)) {
			chain.doFilter(req, res);
			return;
		}
		if (isAjax(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		HttpSession session = request.getSession(false);
		TmpLoginForm form = getTmpLoginForm(session);
		if (form == null) {
			// This is not a redirect from TmpLoginServlet and user needs to be presented with /tmpLogin screen
			response.sendRedirect("/tmpLogin?scanning=" + ScanningLoginUtil.isScanningLogin(request));
			return;
		} else if (request.getRequestURI().equals(SELECT_ROLE_PAGE)) {
			// This is a redirect from TmpLoginServlet with form data
			session.removeAttribute(TmpLoginForm.class.getName());

			UserData userData = popUserDataFromTmpLogin(form, getClientAddressFromRequest(request));
			if (!operatorService.hasOperator(userData.getUid())) {
				handleUnknownOperator(request, response, session, userData, form.isScanning());
				return;
			}

			userDataProducerInstance.get().setUserData(userData);
			auditLogService.addToAuditTrail(new OperatorLoginAuditEvent(userData, Outcome.Success));

			if (form.isScanning()) {
				// Scanning login, redirect to scanning start page
				// This is not used with SAML login, where SAML Relay State is used instead.
				response.sendRedirect(request.getServletContext().getContextPath() + "/secure/" + ScanningLoginUtil.startPage());
				return;
			}
		}
		// Continue chain
		chain.doFilter(req, res);
	}

	private boolean skipFilter(HttpServletRequest request) {
		// Skip this filter if tmpLogin functionality is not enabled or UserData exists or user is in switch role mode.
		HttpSession session = request.getSession(false);
		boolean switchRoleMode = session != null && session.getAttribute(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY) != null;
		return !tmpLoginDetector.isTmpLoginEnabled() || switchRoleMode || userDataProducerInstance.get().getUserData() != null;
	}
	
	private boolean isAjax(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}

	private TmpLoginForm getTmpLoginForm(HttpSession session) {
		return session != null ? (TmpLoginForm) session.getAttribute(TmpLoginForm.class.getName()) : null;
	}

	private UserData popUserDataFromTmpLogin(TmpLoginForm form, InetAddress clientAddress) {
		return new UserData(
				form.getUid(),
				form.getSecurityLevel(),
				translationService.findLocaleById("nb-NO"),
				clientAddress);
	}

	private InetAddress getClientAddressFromRequest(HttpServletRequest httpServletRequest) throws UnknownHostException {
		return InetAddress.getByName(httpServletRequest.getRemoteAddr());
	}

	private void handleUnknownOperator(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession session,
			UserData userData, boolean scanning)
			throws IOException {
		try {
			auditLogService.addToAuditTrail(new OperatorLoginAuditEvent(userData, Outcome.UnknownOperator));
		} finally {
			LOGGER.info(userData.getClientAddress() + " " + userData.getUid() + " is not a valid operator in the system!");
		}
		session.invalidate();
		httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/welcome.xhtml?type=error&scanning=" + scanning);
	}

	@Override
	public void destroy() {
	}

	protected TmpLoginDetector getTmpLoginDetector() {
		return new TmpLoginDetector();
	}
}
