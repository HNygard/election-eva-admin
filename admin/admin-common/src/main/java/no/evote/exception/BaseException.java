package no.evote.exception;

public abstract class BaseException extends RuntimeException {
	public BaseException() {
		super();
	}

	public BaseException(final String message) {
		super(message);
	}

	public BaseException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
