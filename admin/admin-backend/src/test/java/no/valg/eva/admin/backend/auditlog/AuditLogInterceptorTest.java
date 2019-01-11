package no.valg.eva.admin.backend.auditlog;

import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.interceptor.InvocationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

public class AuditLogInterceptorTest {
	private InvocationContext contextStub;
	private AuditEventFactory auditEventFactoryMock;
	private AuditLogServiceBean auditLogServiceMock;
	private AuditLogInterceptor interceptor;
	private AuditEvent auditEventMock;

	@BeforeMethod
	public void injectMocks() {
		contextStub = mock(InvocationContext.class);
		auditEventFactoryMock = mock(AuditEventFactory.class);
		auditEventMock = mock(AuditEvent.class);
		when(auditEventFactoryMock.buildSuccessfulAuditEvent(any())).thenReturn(auditEventMock);
		auditLogServiceMock = mock(AuditLogServiceBean.class);
		interceptor = new AuditLogInterceptor(auditLogServiceMock) {
			@Override
			AuditEventFactory createAuditEventFactory(InvocationContext context) {
				return auditEventFactoryMock;
			}
		};
	}

	@Test
	public void intercept_withoutAnnotationOnMethod_proceedsWithoutDoingAnything() throws Exception {
		when(auditEventFactoryMock.isAuditedInvocation()).thenReturn(false);
		when(contextStub.proceed()).thenReturn(Boolean.TRUE);

		Object response = interceptor.intercept(contextStub);

		assertThat(response).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void intercept_normalReturnWhenInvokingAnnotatedMethod_createsSuccessfulAuditEvent() throws Exception {
		when(auditEventFactoryMock.isAuditedInvocation()).thenReturn(true);
		when(contextStub.proceed()).thenReturn(Boolean.TRUE);

		Object response = interceptor.intercept(contextStub);

		assertThat(response).isEqualTo(Boolean.TRUE);
		verify(auditLogServiceMock).addToAuditTrail(any(AuditEvent.class));
	}

	@Test
	public void intercept_normalReturnWhenInvokingAnnotatedMethodButEventShouldBeMuted_createsSuccessfulAuditEventWhichIsNotAddedToAuditTrail()
			throws Exception {
		when(auditEventMock.muteEvent()).thenReturn(true);
		when(auditEventFactoryMock.isAuditedInvocation()).thenReturn(true);
		when(contextStub.proceed()).thenReturn(Boolean.TRUE);

		Object response = interceptor.intercept(contextStub);

		assertThat(response).isEqualTo(Boolean.TRUE);
		verify(auditLogServiceMock, never()).addToAuditTrail(any(AuditEvent.class));
	}

	@Test
	public void intercept_errorOccursWhenInvokingAnnotatedMethod_createsErrorAuditEvent() throws Exception {
		when(auditEventFactoryMock.isAuditedInvocation()).thenReturn(true);
		when(contextStub.proceed()).thenThrow(new IllegalStateException("This was always supposed to happen"));

		try {
			interceptor.intercept(contextStub);
			fail("Received no exception");
		} catch (IllegalStateException e) {
            verify(auditLogServiceMock).addToAuditTrail(any());
		}
	}
}
