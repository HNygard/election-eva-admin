package no.evote.exception;


public class EvoteInitiationException extends EvoteException {
	public EvoteInitiationException(final String message) {
		super(message);
	}

	public EvoteInitiationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
