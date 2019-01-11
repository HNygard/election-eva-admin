package no.evote.exception;

import no.valg.eva.admin.util.StringUtil;
import no.valg.eva.admin.common.MessageTranslator;
import no.valg.eva.admin.common.UserMessage;

public class EvoteException extends BaseException {
	private UserMessage userMessage;
	protected ErrorCode errorCode;
	protected String[] params;

	public EvoteException() {
		super();
	}

	public EvoteException(String message, String... params) {
		super(message);
		this.params = params;
	}

	public EvoteException(String message, Throwable cause, String... params) {
		super(message, cause);
		this.params = params;
	}
	
	public EvoteException(ErrorCode errorCode, Throwable cause, Object... messageParameters) {
		super(errorCode.formatMessage(messageParameters), cause);
		this.errorCode = errorCode;
		this.params = StringUtil.convert(messageParameters);
	}

	public EvoteException(ErrorCode errorCode, Throwable cause, MessageTranslator messageTranslator, Object... messageParameters) {
		super(errorCode.formatMessage(messageTranslator, messageParameters), cause);
		this.errorCode = errorCode;
	}

	public EvoteException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public EvoteException(UserMessage userMessage) {
		super(userMessage.getMessage());
		this.userMessage = userMessage;
	}

	public String[] getParams() {
		return params;
	}

	public String getCode() {
		return errorCode != null ? errorCode.getCode() : null;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public UserMessage getUserMessage() {
		return userMessage;
	}
}
