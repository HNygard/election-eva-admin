package no.evote.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EvoteSecurityException extends EvoteException {

	public EvoteSecurityException(String msg) {
		super(msg);
	}

	public EvoteSecurityException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public EvoteSecurityException(ErrorCode errorCode, Throwable cause, Object... messageParameters) {
		super(errorCode, cause, messageParameters);
	}
}
