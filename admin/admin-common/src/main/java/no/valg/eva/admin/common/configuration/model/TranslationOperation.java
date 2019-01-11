package no.valg.eva.admin.common.configuration.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TranslationOperation implements Serializable {
	private List<TextId> textIdsCreated = new ArrayList<>();
	private List<TextId> textIdsUpdated = new ArrayList<>();
	private List<LocaleText> localeTextsCreated = new ArrayList<>();
	private List<TextId> textIdsCreateSkipped = new ArrayList<>();
	private List<TextId> textIdsUpdateSkipped = new ArrayList<>();

	public List<TextId> getTextIdsCreated() {
		return textIdsCreated;
	}

	public List<LocaleText> getLocaleTextsCreated() {
		return localeTextsCreated;
	}

	public List<TextId> getTextIdsUpdated() {
		return textIdsUpdated;
	}

	public List<TextId> getTextIdsCreateSkipped() {
		return textIdsCreateSkipped;
	}

	public List<TextId> getTextIdsUpdateSkipped() {
		return textIdsUpdateSkipped;
	}
}
