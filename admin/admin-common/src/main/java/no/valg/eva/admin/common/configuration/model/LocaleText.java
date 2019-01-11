package no.valg.eva.admin.common.configuration.model;

import java.io.Serializable;

public class LocaleText implements Serializable {

	private String localeId;
	private String localeText;

	public LocaleText(String localeId) {
		this.localeId = localeId;
	}

	public String getLocaleId() {
		return localeId;
	}

	public String getLocaleText() {
		return localeText;
	}

	public void setLocaleText(String localeText) {
		this.localeText = localeText;
	}
}
