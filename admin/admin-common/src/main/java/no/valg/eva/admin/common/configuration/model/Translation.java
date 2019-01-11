package no.valg.eva.admin.common.configuration.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Translation implements Serializable {

	private TextId textId;
	private Map<String, LocaleText> localeTexts;

	public Translation() {
		textId = new TextId();
		localeTexts = new HashMap<>();
	}

	public TextId getTextId() {
		return textId;
	}

	public Map<String, LocaleText> getLocaleTexts() {
		return localeTexts;
	}
}
