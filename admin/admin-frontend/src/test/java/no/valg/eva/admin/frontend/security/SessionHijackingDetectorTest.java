package no.valg.eva.admin.frontend.security;

import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.auditevents.HttpSessionHijackedAuditEvent;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static no.valg.eva.admin.frontend.security.SessionHijackingDetector.OIDC_CLAIM_PID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SessionHijackingDetectorTest {
	@Mock
	private HttpServletRequest httpServletRequestStub;
	@Mock
	private HttpServletResponse httpServletResponseStub;
	@Mock
	private HttpSession httpSessionStub;
	@Mock
	private AuditLogService auditLogServiceMock;
	@Mock
	private Instance<UserDataProducer> userDataProducerInstanceStub;
	@Mock
	private UserDataProducer userDataProducerStub;
	@Mock
	private FilterChain filterChainStub;
	@Mock
	private TmpLoginDetector tmpLoginDetectorStub;

	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		userData = new AuditLogTestsObjectMother().createUserData();

		when(httpServletRequestStub.getSession()).thenReturn(mock(HttpSession.class));
		when(httpServletRequestStub.getSession()).thenReturn(httpSessionStub);
		when(userDataProducerInstanceStub.get()).thenReturn(userDataProducerStub);
		when(userDataProducerStub.getUserData()).thenReturn(userData);
	}

	@Test
	public void doFilter_whenTmpLoginIsUsed_allowRequest() throws Exception {
		when(tmpLoginDetectorStub.isTmpLoginEnabled()).thenReturn(true);

		SessionHijackingDetector sessionHijackFilter = createDefaultSessionHijackFilter();
		sessionHijackFilter.doFilter(httpServletRequestStub, httpServletResponseStub, filterChainStub);

		verify(filterChainStub).doFilter(httpServletRequestStub, httpServletResponseStub);
		verifyZeroInteractions(auditLogServiceMock);
	}

	@Test
	public void doFilter_whenSamlSessionHasSameUidAsApplicationServer_allowRequest() throws Exception {
		when(tmpLoginDetectorStub.isTmpLoginEnabled()).thenReturn(false);
		when(httpServletRequestStub.getHeader(OIDC_CLAIM_PID)).thenReturn(userData.getUid());

		SessionHijackingDetector sessionHijackFilter = createDefaultSessionHijackFilter();
		sessionHijackFilter.doFilter(httpServletRequestStub, httpServletResponseStub, filterChainStub);

		verify(filterChainStub).doFilter(httpServletRequestStub, httpServletResponseStub);
		verifyZeroInteractions(auditLogServiceMock);
	}

	@Test
	public void doFilter_whenSamlSessionHasDifferentUidFromApplicationServer_denyRequestAndAuditLog() throws Exception {
		when(tmpLoginDetectorStub.isTmpLoginEnabled()).thenReturn(false);
		when(httpServletRequestStub.getHeader(OIDC_CLAIM_PID)).thenReturn("wrong-uid");

		SessionHijackingDetector sessionHijackFilter = createDefaultSessionHijackFilter();
		sessionHijackFilter.doFilter(httpServletRequestStub, httpServletResponseStub, filterChainStub);

		verify(auditLogServiceMock).addToAuditTrail(any(HttpSessionHijackedAuditEvent.class));
		verify(httpServletResponseStub).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	private SessionHijackingDetector createDefaultSessionHijackFilter() {
		return new SessionHijackingDetector(userDataProducerInstanceStub, auditLogServiceMock, tmpLoginDetectorStub);
	}

}
