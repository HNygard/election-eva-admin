package no.valg.eva.admin.backend.auditlog;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.InetAddress;

import javax.interceptor.InvocationContext;

import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.evote.service.security.ErrorCodeMapper;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.Locale;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecuredObjectsInterceptorTest {
	private static final String AN_ERROR_MESSAGE = "All your election are belong to us";
	@Mock
	private InvocationContext invocationContextMock;
	private ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	private FakeClass target;
	private Method loggedMethod;
	private Method anotherMethodNotLogged;
	private Method notLoggedMethod;
	private Object[] emptyParameterList;

	@BeforeMethod
	public void initMocks() throws NoSuchMethodException {
		MockitoAnnotations.initMocks(this);
		target = new FakeClass();
		loggedMethod = FakeClass.class.getDeclaredMethod("loggedMethod", UserData.class, String.class, String.class);
		notLoggedMethod = FakeClass.class.getDeclaredMethod("notLoggedMethod");
		anotherMethodNotLogged = FakeClass.class.getDeclaredMethod("anotherMethodNotLogged");
		emptyParameterList = new Object[] {};
	}

	@Test
	public void logCall_withSecObjAnnotation_isLogged() throws Exception {
		when(invocationContextMock.getTarget()).thenReturn(target);
		when(invocationContextMock.getMethod()).thenReturn(loggedMethod);
		when(invocationContextMock.getParameters()).thenReturn(new Object[] { new AuditLogTestsObjectMother().createUserData(), "foo", "bar" });

		SecuredObjectsInterceptor interceptor = new SecuredObjectsInterceptor(errorCodeMapper);
		interceptor.logCall(invocationContextMock);

		verify(invocationContextMock, times(1)).proceed();
	}

	@Test
	public void logCall_withSecObjAnnotationAndExceptionOccurs_isLogged() throws Exception {
		when(invocationContextMock.getTarget()).thenReturn(target);
		when(invocationContextMock.getMethod()).thenReturn(loggedMethod);
		when(invocationContextMock.getParameters()).thenReturn(new Object[] { new AuditLogTestsObjectMother().createUserData(), "foo", "bar" });
		when(invocationContextMock.proceed()).thenThrow(new RuntimeException(AN_ERROR_MESSAGE));

		SecuredObjectsInterceptor interceptor = new SecuredObjectsInterceptor(errorCodeMapper);
		try {
			interceptor.logCall(invocationContextMock);
			Assert.fail("Exception should have been thrown");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo(AN_ERROR_MESSAGE); // as expected
		}

		verify(invocationContextMock, times(1)).proceed();
	}

	@Test
	public void logCall_withAnyUserSecObjAnnotation_isNotLogged() throws Exception {
		when(invocationContextMock.getTarget()).thenReturn(target);
		when(invocationContextMock.getMethod()).thenReturn(notLoggedMethod);
		when(invocationContextMock.getParameters()).thenReturn(emptyParameterList);

		SecuredObjectsInterceptor interceptor = new SecuredObjectsInterceptor(errorCodeMapper);
		interceptor.logCall(invocationContextMock);

		verify(invocationContextMock, times(1)).proceed();
	}

	@Test
	public void logCall_withNoSecObjAnnotation_isNotLogged() throws Exception {
		when(invocationContextMock.getTarget()).thenReturn(target);
		when(invocationContextMock.getMethod()).thenReturn(anotherMethodNotLogged);
		when(invocationContextMock.getParameters()).thenReturn(emptyParameterList);

		SecuredObjectsInterceptor interceptor = new SecuredObjectsInterceptor(errorCodeMapper);
		interceptor.logCall(invocationContextMock);

		verify(invocationContextMock, times(1)).proceed();
	}

	@Test
	public void generateIdWithSeparator() throws Exception {
		UserData userData = new UserData("uid", SecurityLevel.ONE_FACTOR, mock(Locale.class), mock(InetAddress.class));
		Locale anEntity = new Locale();
		anEntity.setPk(1L);
		String s = "aRandomArgument";
		String parameterList = SecuredObjectsInterceptor.generateParameterList(asList(userData, anEntity, s));
		assertThat(parameterList).isEqualTo("<UserData,1,aRandomArgument>");
	}

	@SuppressWarnings("unused")
	private class FakeClass {
		@Security(accesses = Konfigurasjon_Valghendelse_Rediger, type = READ)
		public Object loggedMethod(UserData userData, String foo, String bar) {
			return null;
		}

		@SecurityNone
		public Object notLoggedMethod() {
			return null;
		}

		public Object anotherMethodNotLogged() {
			return null;
		}
	}
}
