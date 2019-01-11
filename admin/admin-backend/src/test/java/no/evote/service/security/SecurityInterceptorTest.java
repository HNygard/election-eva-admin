package no.evote.service.security;

import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.rbac.RBACAuthenticator;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.common.rbac.SecurityType;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import javax.interceptor.InvocationContext;
import javax.transaction.TransactionSynchronizationRegistry;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityInterceptorTest extends MockUtilsTestCase {

	@Test
	public void intercept_withPrivateMethod_proceeds() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("privateMethod"));

		interceptor.intercept(ctx);

		verify(ctx).proceed();
		verify(ctx, never()).getTarget();
	}

	@Test(enabled = false, expectedExceptions = EvoteSecurityException.class, expectedExceptionsMessageRegExp = "Missing 'Security' annotation.*")
	public void intercept_withMissingSecurityAnnotation_throwsException() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("missingSecurityAnnotation"));

		interceptor.intercept(ctx);
	}

	@Test
	public void intercept_withSecurityNoneMethod_proceeds() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("securityNone"));

		interceptor.intercept(ctx);

		verify(ctx).proceed();
	}

	@Test(expectedExceptions = EvoteSecurityException.class, expectedExceptionsMessageRegExp = "UserData required as first argument.*")
	public void intercept_withNoUserData_throwsException() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("read"));

		interceptor.intercept(ctx);
	}

	@Test(expectedExceptions = EvoteSecurityException.class, expectedExceptionsMessageRegExp = "Operator operatorId is disabled")
	public void intercept_withInactiveOperator_throwsException() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("read"), new Object[] { withInactiveOperator() });

		interceptor.intercept(ctx);
	}

	@Test(expectedExceptions = EvoteSecurityException.class, expectedExceptionsMessageRegExp = "Role roleId is disabled")
	public void intercept_withInactiveRole_throwsException() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("read"), new Object[] { withInactiveRole() });

		interceptor.intercept(ctx);
	}

	@Test(expectedExceptions = EvoteSecurityException.class, expectedExceptionsMessageRegExp = "User userId with support role does not have write access.*")
	public void intercept_withUserSupportWrite_throwsException() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("write"), new Object[] { with(true) });

		interceptor.intercept(ctx);
	}

	@Test
	public void intercept_withValidAccess_proceeds() throws Exception {
		SecurityInterceptor interceptor = initializeMocks(SecurityInterceptor.class);
		InvocationContext ctx = ctx(Intercepted.class.getDeclaredMethod("read"), new Object[] { with(false) });
		when(registryMock().getTransactionKey()).thenReturn("key");
		when(registryMock().getResource("accessCacheVerified")).thenReturn(true);
		when(getInjectMock(RBACAuthenticator.class).hasAccess(any(UserData.class), any(Accesses.class))).thenReturn(true);

		interceptor.intercept(ctx);

		verify(ctx).proceed();
		verify(registryMock()).putResource(eq(SecurityInterceptor.USER_DATA_KEY), any(UserData.class));
	}

	private TransactionSynchronizationRegistry registryMock() {
		return getInjectMock(TransactionSynchronizationRegistry.class);
	}

	private UserData withInactiveOperator() {
		UserData result = userData();
		when(result.getOperator().isActive()).thenReturn(false);
		when(result.getRole().isActive()).thenReturn(true);
		return result;
	}

	private UserData withInactiveRole() {
		UserData result = userData();
		when(result.getOperator().isActive()).thenReturn(true);
		when(result.getRole().isActive()).thenReturn(false);
		return result;
	}

	private UserData with(boolean support) {
		UserData result = userData();
		when(result.getOperator().isActive()).thenReturn(true);
		when(result.getRole().isActive()).thenReturn(true);
		when(result.getRole().isUserSupport()).thenReturn(support);
		return result;
	}

	private UserData userData() {
		UserData result = createMock(UserData.class);
		when(result.getUid()).thenReturn("userId");
		when(result.getOperator().getId()).thenReturn("operatorId");
		when(result.getRole().getId()).thenReturn("roleId");
		return result;
	}

	private InvocationContext ctx(Method method) {
		return ctx(method, new Object[0]);
	}

	private InvocationContext ctx(Method method, Object[] parameters) {
		InvocationContext result = createMock(InvocationContext.class);
		when(result.getMethod()).thenReturn(method);
		when(result.getParameters()).thenReturn(parameters);
		return result;
	}

	public class Intercepted {
		private void privateMethod() {
		}

		public void missingSecurityAnnotation() {
		}

		@SecurityNone
		public void securityNone() {
		}

		@Security(accesses = Accesses.Konfigurasjon_Grunnlagsdata_Redigere, type = SecurityType.READ)
		public void read() {
		}

		@Security(accesses = Accesses.Konfigurasjon_Grunnlagsdata_Redigere, type = SecurityType.WRITE)
		public void write() {
		}
	}

}
