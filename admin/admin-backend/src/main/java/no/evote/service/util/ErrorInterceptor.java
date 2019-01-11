package no.evote.service.util;

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.ws.rs.ProcessingException;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.util.ExceptionUtil;

import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

public class ErrorInterceptor implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(ErrorInterceptor.class);

	@AroundInvoke
	public Object cleanException(InvocationContext ctx) throws Exception {
		try {
			return ctx.proceed();
		} catch (PersistenceException e) {
			Exception unwrapped = ExceptionUtil.unwrapThrowable(e, OptimisticLockException.class);
			if (unwrapped != null) {
				throw evoteException(ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK, unwrapped);
			}
			unwrapped = ExceptionUtil.unwrapThrowable(e, EntityNotFoundException.class);
			if (unwrapped != null) {
				throw evoteException(ErrorCode.ERROR_CODE_0502_ENTITY_NOT_FOUND, unwrapped);
			}
			unwrapped = ExceptionUtil.unwrapThrowable(e, ConstraintViolationException.class);
			if (unwrapped != null) {
				throw evoteException(ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION, unwrapped);
			}
			unwrapped = ExceptionUtil.unwrapThrowable(e, StaleObjectStateException.class);
			if (unwrapped != null) {
				throw evoteException(ErrorCode.ERROR_CODE_0504_STALE_OBJECT, unwrapped);
			}
			LOGGER.error("Unexpected database error " + e.getMessage(), e);
			throw evoteException(ErrorCode.ERROR_CODE_0500_UNEXPECTED, e);
		} catch (Exception e) {
			preventInvalidCause(e);
			throw e;
		}
	}

	private EvoteException evoteException(ErrorCode errorCode, Exception e) {
		EvoteException result = new EvoteException(errorCode, null, null);
		if (e instanceof ConstraintViolationException) {
			String constraintName = ((ConstraintViolationException) e).getConstraintName();
			if (constraintName != null && constraintName.startsWith("nk_")) {
				result = new EvoteException(ErrorCode.ERROR_CODE_0505_UNIQUE_CONSTRAINT_VIOLATION, null, new String[] { constraintName, getBottomMessage(e) });
			} else {
				result = new EvoteException(errorCode, null, constraintName);
			}
		} else if (e instanceof StaleObjectStateException) {
			String entityName = ((StaleObjectStateException) e).getEntityName();
			result = new EvoteException(errorCode, null, entityName);
		}
		result.setStackTrace(e.getStackTrace());
		throw result;
	}

	private String getBottomMessage(Throwable t) {
		String result = t.getMessage();
		while (t.getCause() != null) {
			t = t.getCause();
			result = t.getMessage();
		}
		return result;
	}

	private void preventInvalidCause(Exception e) {
		for (Class cls : invalidCauses()) {
			Throwable unwrapped = ExceptionUtil.unwrapThrowable(e, cls);
			if (unwrapped != null) {
				EvoteException ex = new EvoteException(unwrapped.getClass().getName() + ": " + unwrapped.getMessage());
				ex.setStackTrace(unwrapped.getStackTrace());
				throw ex;
			}
		}
	}

	private Class[] invalidCauses() {
		return new Class[] { PSQLException.class, ProcessingException.class };
	}

}
