package no.valg.eva.admin.frontend.security;

import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.evote.service.security.SelectRoleFilter;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.rbac.service.UserDataService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;

import static no.valg.eva.admin.frontend.security.OidcFilter.OIDC_CLAIM_ACR;
import static no.valg.eva.admin.frontend.security.OidcFilter.OIDC_CLAIM_LOCALE;
import static no.valg.eva.admin.frontend.security.OidcFilter.OIDC_CLAIM_PID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OidcFilterTest extends MockUtilsTestCase {

    private static final String PID = "12345678901";
    private static final String ACR = "Level3";
    private static final String LOCALE = "nb";
    
    private OidcFilter oidcFilter;
    private UserDataService userDataService;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private FilterChain filterChain;
    private UserDataProducer userDataProducer;
    private HttpSession httpSession;
    private UserData userData;

    @BeforeMethod
    public void setUp() throws Exception {
        oidcFilter = initializeMocks(OidcFilter.class);

        userDataService = getInjectMock(UserDataService.class);

        userDataProducer = createMock(UserDataProducer.class);
        Instance<UserDataProducer> userDataProducerInstance = mockInstance("userDataProducerInstance", UserDataProducer.class);
        when(userDataProducerInstance.get()).thenReturn(userDataProducer);

        httpServletRequest = createMock(HttpServletRequest.class);
        httpSession = createMock(HttpSession.class);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpServletRequest.getServletPath()).thenReturn("/mockPath");

        httpServletResponse = createMock(HttpServletResponse.class);

        filterChain = createMock(FilterChain.class);

        userData = new AuditLogTestsObjectMother().createUserData();
        when(userDataService.createUserDataAndCheckOperator(any(), any(), any(), any())).thenReturn(userData);
    }

    @Test
    public void doFilter_whenInvokedWithoutUserDataAndRoleSwitch_requestsAndSetsUserData() throws IOException, ServletException {
        invocationWithoutUserDataAndRoleSwitch(PID, ACR, LOCALE);

        oidcFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(userDataService, times(1)).createUserDataAndCheckOperator(eq(PID), eq(ACR), eq(LOCALE), any(InetAddress.class));
        verify(userDataProducer, times(1)).setUserData(userData);
    }

    private void invocationWithoutUserDataAndRoleSwitch(String pid, String acr, String locale) {
        when(userDataProducer.getUserData()).thenReturn(null);
        when(httpSession.getAttribute(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY)).thenReturn(null);
        when(httpServletRequest.getHeader(OIDC_CLAIM_PID)).thenReturn(pid);
        when(httpServletRequest.getHeader(OIDC_CLAIM_ACR)).thenReturn(acr);
        when(httpServletRequest.getHeader(OIDC_CLAIM_LOCALE)).thenReturn(locale);
    }


    @Test(dataProvider = "oidc_claims")
    public void doFilter_whenInvokedWithoutOidcClaims_doNotSetUserData(String pid, String acr, String locale) throws IOException, ServletException {
        invocationWithoutUserDataAndRoleSwitch(pid, acr, locale);

        oidcFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(userDataService, times(0)).createUserDataAndCheckOperator(eq(PID), eq(ACR), eq(LOCALE), any(InetAddress.class));
        verify(userDataProducer, times(0)).setUserData(userData);
    }

    @DataProvider
    public Object[][] oidc_claims() {
        return new Object[][]{
                {null, ACR, LOCALE},
                {PID, null, LOCALE},
                {PID, ACR, null}
        };
    }

    @Test(dataProvider = "oidc_claims")
    public void doFilter_whenInvokedWithoutOidcClaims_invalidateAndRedirect(String pid, String acr, String locale) throws IOException, ServletException {
        invocationWithoutUserDataAndRoleSwitch(pid, acr, locale);

        oidcFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletRequest.getSession(), times(1)).invalidate();
        verify(httpServletResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void doFilter_witjAjaxRequest_sendsError() throws IOException, ServletException {
        when(userDataProducer.getUserData()).thenReturn(null);
        when(httpSession.getAttribute(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY)).thenReturn(null);
        when(httpServletRequest.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        oidcFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(userDataProducer, times(0)).setUserData(userData);
        verify(httpServletResponse, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void dofilter_whenUserWitchesRoles_setUserDataFromPreviousSession() throws IOException, ServletException {
        when(userDataProducer.getUserData()).thenReturn(null);
        when(httpSession.getAttribute(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY)).thenReturn(userData);

        oidcFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(userDataProducer, times(1)).setUserData(userData);
    }

    @Test
    public void doFilter_whenUserIsLoggedIn_continue() throws IOException, ServletException {
        when(userDataProducer.getUserData()).thenReturn(userData);

        oidcFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(userDataProducer, times(0)).setUserData(userData);
        verify(filterChain, times(1)).doFilter(httpServletRequest, httpServletResponse);
    }
}
