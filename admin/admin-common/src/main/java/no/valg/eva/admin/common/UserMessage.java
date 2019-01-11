package no.valg.eva.admin.common;

import java.io.Serializable;

/**
 * Contains key and params for creating a translated message
 */
public class UserMessage implements Serializable {
	
	private final String message;
	private final Object[] args;

	public UserMessage(String message, Object... args) {
		this.message = message;
		this.args = args;
	}

	public String getMessage() {
		return message;
	}

	public Object[] getArgs() {
		return args;
	}
}
