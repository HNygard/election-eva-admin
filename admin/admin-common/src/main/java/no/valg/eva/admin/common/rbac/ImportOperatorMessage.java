package no.valg.eva.admin.common.rbac;

import no.valg.eva.admin.common.UserMessage;

public class ImportOperatorMessage extends UserMessage {

	private final int line;
	
	public ImportOperatorMessage(int line, String message, Object... args) {
		super(message, args);
		this.line = line;
	}

	public String line() {
		return Integer.toString(line);
	}
}
