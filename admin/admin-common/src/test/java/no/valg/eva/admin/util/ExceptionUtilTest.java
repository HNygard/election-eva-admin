package no.valg.eva.admin.util;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;

import org.testng.annotations.Test;

public class ExceptionUtilTest {
	@Test
	public void unwrapThrowable_givenExceptionChainAndThrowableClass_returnsThrowableCause() throws Exception {
		Exception exception = new Exception();
		Exception exception2 = new Exception(exception);
		Throwable throwable = new Throwable(exception2);
		
		Throwable unwrappedThrowable = ExceptionUtil.unwrapThrowable(throwable, Exception.class);
		
		assertThat(unwrappedThrowable).isSameAs(exception2);
	}

	@Test
	public void unwrapThrowable_givenExceptionChainAndExceptionClass_returnsExceptionClause() throws Exception {
		EvoteException exception = new EvoteException();
		Throwable throwable = new Throwable(exception);
		RuntimeException runtimeException = new RuntimeException(throwable);

		Exception unwrappedException = ExceptionUtil.unwrapThrowable(runtimeException, EvoteException.class);

		assertThat(unwrappedException).isSameAs(exception);
	}

	@Test
	public void unwrapThrowable_givenExceptionChainAndUnknownCauseClass_returnsNull() throws Exception {
		Exception exception = new Exception();
		Throwable throwable = new Throwable(exception);
		RuntimeException runtimeException = new RuntimeException(throwable);

		IllegalArgumentException unknownException = ExceptionUtil.unwrapThrowable(runtimeException, IllegalArgumentException.class);

		assertThat(unknownException).isNull();
	}
	
	@Test
	public void unwrapThrowable_withEvoteExceptonAndEvoteExceptionType_returnsEvoteException() throws Exception {
		EvoteException e = new EvoteException();
		
		assertThat(ExceptionUtil.unwrapThrowable(e, EvoteException.class)).isNotNull();
	}

	@Test
	public void unwrapThrowable_withEvoteSecurityExceptonAndEvoteExceptionType_returnsEvoteException() throws Exception {
		EvoteSecurityException e = new EvoteSecurityException("");

		assertThat(ExceptionUtil.unwrapThrowable(e, EvoteSecurityException.class)).isNotNull();
		assertThat(ExceptionUtil.unwrapThrowable(e, EvoteException.class)).isNotNull();
	}
}
