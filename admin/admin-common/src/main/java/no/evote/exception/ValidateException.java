package no.evote.exception;

public class ValidateException extends EvoteException {
    public ValidateException() {
    }

    public ValidateException(final String message, final String... params) {
        super(message, params);
    }

    public ValidateException(final String message, final Throwable cause, final String... params) {
        super(message, cause, params);
    }
}
