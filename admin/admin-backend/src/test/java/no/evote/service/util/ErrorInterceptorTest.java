package no.evote.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;

import java.security.AccessControlException;
import java.sql.SQLException;

import javax.interceptor.InvocationContext;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockTimeoutException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ErrorInterceptorTest extends MockUtilsTestCase {

	@Test(dataProvider = "cleanExceptionDatabase")
	public void cleanException_withDatabaseErrorsProvider_verifyExpected(Exception ex, ErrorCode expectedErrorCode, String expectedParam) throws Exception {
		InvocationContext ctx = createMock(InvocationContext.class);
		doThrow(ex).when(ctx).proceed();
		ErrorInterceptor interceptor = new ErrorInterceptor();

		try {
			interceptor.cleanException(ctx);
			fail("Expected EvoteException not thrown");
		} catch (EvoteException e) {
			assertThat(e.getErrorCode().getCode()).isEqualTo(expectedErrorCode.getCode());
			if (expectedParam != null) {
				assertThat(e.getParams()[0]).isEqualTo(expectedParam);
			}
		}

	}

	@DataProvider(name = "cleanExceptionDatabase")
	public Object[][] cleanExceptionDatabase() {
		return new Object[][] {
				{ new LockTimeoutException("LockTimeoutException"), ErrorCode.ERROR_CODE_0500_UNEXPECTED, null },
				{ new PersistenceException(new OptimisticLockException()), ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK, null },
				{ new PersistenceException(new EntityNotFoundException()), ErrorCode.ERROR_CODE_0502_ENTITY_NOT_FOUND, null },
				{ new PersistenceException(constraintViolationException("constr")), ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION, "constr" },
				{ new PersistenceException(staleObjectStateException("entity")), ErrorCode.ERROR_CODE_0504_STALE_OBJECT, "entity" }
		};
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "org.postgresql.util.PSQLException: PSQLException")
	public void cleanException_withInvalidCause_throwsEvoteException() throws Exception {
		InvocationContext ctx = createMock(InvocationContext.class);
		doThrow(new RuntimeException(new PSQLException("PSQLException", PSQLState.INVALID_CURSOR_STATE))).when(ctx).proceed();
		ErrorInterceptor interceptor = new ErrorInterceptor();

		interceptor.cleanException(ctx);
	}

	@Test(expectedExceptions = AccessControlException.class, expectedExceptionsMessageRegExp = "AccessControlException")
	public void cleanException_withValidCause_throwsEvoteException() throws Exception {
		InvocationContext ctx = createMock(InvocationContext.class);
		doThrow(new AccessControlException("AccessControlException")).when(ctx).proceed();
		ErrorInterceptor interceptor = new ErrorInterceptor();

		interceptor.cleanException(ctx);
	}

	private ConstraintViolationException constraintViolationException(String constraintName) {
		return new ConstraintViolationException("", new SQLException(""), constraintName);
	}

	private StaleObjectStateException staleObjectStateException(String entityName) {
		return new StaleObjectStateException(entityName, "");
	}

}
