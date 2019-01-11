package no.evote.exception;

public class EvoteTimeoutException extends EvoteException {
    public EvoteTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
