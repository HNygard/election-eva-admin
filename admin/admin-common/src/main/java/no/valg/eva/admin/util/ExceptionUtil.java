package no.valg.eva.admin.util;

public final class ExceptionUtil {

	private ExceptionUtil() {
	}

	public static Exception getRootCause(final Exception e) {
		Exception eCurrentException = e;
		Exception eRootException = null;

		while (eCurrentException != null) {
			Exception eNextLevelUp = (Exception) eCurrentException.getCause();
			if (eNextLevelUp == null) {
				eRootException = eCurrentException;
				break;
			} else {
				eCurrentException = eNextLevelUp;
			}
		}
		return eRootException;
	}

	public static String getRootMessage(final Exception e) {
		String rootMessage = getRootCause(e).getMessage();
		return rootMessage == null ? "" : rootMessage;
	}

	public static String buildErrorMessage(final Exception e) {
		Exception eRootException = getRootCause(e);
		return eRootException.getClass() + " " + eRootException.getStackTrace()[0] + " " + getRootMessage(e);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T unwrapThrowable(Throwable t, Class<T> throwableType) {
		if (throwableType.isAssignableFrom(t.getClass())) {
			return (T) t;
		}
		Throwable cause = t.getCause();
		if (cause == null) {
			return null;
		}
		if (throwableType.isAssignableFrom(cause.getClass())) {
			return (T) cause;
		}
		return unwrapThrowable(cause, throwableType);
	}
}
