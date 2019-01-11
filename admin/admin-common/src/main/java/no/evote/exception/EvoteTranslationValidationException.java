package no.evote.exception;

public class EvoteTranslationValidationException extends EvoteException {
	private final String textId;
	private Exception rootCause = null;

	public EvoteTranslationValidationException(final String textId) {
		this.textId = textId;
	}

	public EvoteTranslationValidationException(final String textId, final Exception e) {
		this.textId = textId;
		this.rootCause = e;
	}

	public String getTextId() {
		return textId;
	}

	public Exception getE() {
		return rootCause;
	}
}
