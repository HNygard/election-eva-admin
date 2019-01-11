package no.valg.eva.admin.frontend.security;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.OperatorLoginAuditEvent;
import no.valg.eva.admin.common.rbac.service.UserDataService;

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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static no.evote.service.security.SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@NoArgsConstructor
@Log4j
public class OidcFilter implements Filter {

    static final String OIDC_CLAIM_PID = "OIDC_CLAIM_pid";
    static final String OIDC_CLAIM_ACR = "OIDC_CLAIM_acr";
    static final String OIDC_CLAIM_LOCALE = "OIDC_CLAIM_locale";
    @Inject
    private UserDataService userDataService;
    @Inject
    private Instance<UserDataProducer> userDataProducerInstance;
    @Inject
    private AuditLogService auditLogService;

    @Override
    public void init(final FilterConfig filterConfig) {
        //Nothing to do
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        HttpSession session = ((HttpServletRequest) servletRequest).getSession();
        UserDataProducer userDataProducer = userDataProducerInstance.get();

        UserData userData = userDataProducer.getUserData();
        if (userData != null) {
            log.debug("userData er satt");
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else if (userSwitchedRoles(session)) {
            log.debug("bytter rolle");
            UserData userDataFromPreviousSession = popUserDataFromRoleSwitch(session);
            userDataProducer.setUserData(userDataFromPreviousSession);
            if (log.isDebugEnabled()) {
                log.debug("uid: " + userDataFromPreviousSession.getUid() + ", operator role " + userDataFromPreviousSession.getOperatorRole().toString());
            }
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            log.debug("userdata er null");
            if (isAjax(httpServletRequest)) {
                log.debug("isAjax-request");
                httpServletResponse.sendError(SC_UNAUTHORIZED);
                return;
            }
            handleRequestFromOidc(httpServletRequest, httpServletResponse, filterChain, userDataProducer);
        }
    }

    private boolean userSwitchedRoles(HttpSession session) {
        return session.getAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY) != null;
    }

    private UserData popUserDataFromRoleSwitch(HttpSession session) {
        UserData userData = (UserData) session.getAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY);
        session.removeAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY);
        return userData;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private void handleRequestFromOidc(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain,
                                       UserDataProducer userDataProducer) throws IOException, ServletException {
        if (log.isDebugEnabled()) {
            log.debug("Handle request from oidc: " + httpServletRequest.toString() + httpServletResponse.toString());
        }
        try {
            String oidcClaimPid = getHeader(httpServletRequest, OIDC_CLAIM_PID);
            String oidcClaimAcr = getHeader(httpServletRequest, OIDC_CLAIM_ACR);
            String oidcClaimLocale = getHeader(httpServletRequest, OIDC_CLAIM_LOCALE);
            InetAddress clientAddress = getClientAddressFromRequest(httpServletRequest);

            UserData userData = userDataService.createUserDataAndCheckOperator(oidcClaimPid, oidcClaimAcr, oidcClaimLocale, clientAddress);
            userDataProducer.setUserData(userData);
            auditLogService.addToAuditTrail(new OperatorLoginAuditEvent(userData, Outcome.Success));

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (EvoteSecurityException e) {
            log.error(e.getMessage(), e);
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/welcome.xhtml?type=error");
        }
    }

    private String getHeader(HttpServletRequest httpServletRequest, String headerName) {
        String header = httpServletRequest.getHeader(headerName);
        if (isNotBlank(header)) {
            return header;
        } else {
            throw new EvoteSecurityException(headerName + " is blank.");
        }
    }

    private InetAddress getClientAddressFromRequest(HttpServletRequest httpServletRequest) throws UnknownHostException {
        return InetAddress.getByName(httpServletRequest.getRemoteAddr());
    }

    @Override
    public void destroy() {
        //Nothing to do
    }

}
