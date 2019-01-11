package no.evote.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ReadOnlyPrivilegeException extends EvoteSecurityException {

	public ReadOnlyPrivilegeException(final String msg) {
		super(msg);
	}

	public ReadOnlyPrivilegeException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
