package no.evote.service.security;

import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.SimpleAuditEventType;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import static no.valg.eva.admin.common.auditlog.SimpleAuditEventType.OperatorLoggedOut;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionListenerTest extends BaseFrontendTest {

	private SessionListener sessionListener;
	private Instance<UserData> userDataInstance;

	@BeforeMethod
	public void setUp() throws Exception {
		sessionListener = initializeMocks(SessionListener.class);
		userDataInstance = mockInstance("userDataInstance", UserData.class);
		configureUserDataStub();
	}

	private void configureUserDataStub() {
		when(userDataInstance.get().getUid()).thenReturn("24120055555");
		when(userDataInstance.get().getSecurityLevelEnum()).thenReturn(SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC);
	}

	@Test
    public void sessionCreated_verifyTimeAttribute() {
		HttpSessionEvent event = mock(HttpSessionEvent.class);
		HttpSession session = mock(HttpSession.class);
		when(event.getSession()).thenReturn(session);

		sessionListener.sessionCreated(event);

        verify(session).setAttribute(eq("time"), any(DateTime.class));
	}

	@Test
    public void sessionDestroyed_withNoUser_nothingToCheck() {
		HttpSessionEvent event = mock(HttpSessionEvent.class);
		HttpSession session = mock(HttpSession.class);
		when(event.getSession()).thenReturn(session);
		when(userDataInstance.get()).thenReturn(null);

		sessionListener.sessionDestroyed(event);
	}

	@Test
    public void sessionDestroyed_withUser_verifyAuditLog() {
		HttpSessionEvent event = mock(HttpSessionEvent.class);
		HttpSession session = mock(HttpSession.class);
		when(event.getSession()).thenReturn(session);

		sessionListener.sessionDestroyed(event);

		verify(getInjectMock(AuditLogService.class)).addToAuditTrail(typeEq(OperatorLoggedOut));
	}

	private SimpleAuditEvent typeEq(final SimpleAuditEventType auditEventType) {
        return argThat(auditEvent -> auditEventType.equals(auditEvent.eventType()));
	}

}
