package no.evote.exception;

import javax.ejb.ApplicationException;

import no.valg.eva.admin.util.StringUtil;

@ApplicationException(rollback = false)
public class EvoteNoRollbackException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String[] params;
	
	public EvoteNoRollbackException() {
		this.errorCode = null;
		this.params = null;
	}

	public EvoteNoRollbackException(String message) {
		super(message);
		this.errorCode = null;
		this.params = null;
	}

	public EvoteNoRollbackException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = null;
		this.params = null;
	}

	public EvoteNoRollbackException(ErrorCode errorCode, Throwable cause, Object... messageParameters) {
		super(errorCode.formatMessage(messageParameters), cause);
		this.errorCode = errorCode;
		this.params = StringUtil.convert(messageParameters);
	}
	
	public EvoteNoRollbackException(EvoteException e) {
		super(e.getMessage(), e);
		this.errorCode = e.getErrorCode();
		this.params = e.getParams();
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String[] getParams() {
		return params;
	}

	public String getCode() {
		return errorCode != null ? errorCode.getCode() : null;
	}
}
