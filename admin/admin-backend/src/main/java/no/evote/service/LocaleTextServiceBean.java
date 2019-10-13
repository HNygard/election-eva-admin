package no.evote.service;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.common.repository.TextIdRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.TextId;

/**
 * Methods for fetching and and maintaining translations. Most of the translation functionality has been moved to TranslationService which has a rewritten api.
 */
@Default
@ApplicationScoped
public class LocaleTextServiceBean {
	@Inject
	private TranslationServiceBean translationService;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private LocaleTextRepository localeTextRepository;
	@Inject
	private TextIdRepository textIdRepository;
	@Inject
	private LocaleRepository localeRepository;

	public LocaleTextServiceBean() {

	}

	/**
	 * Create a locale text for all locales
	 */
	public List<LocaleText> createLocaleTextToAllLocale(final UserData userData, final String textIdStr, final String text, final ElectionEvent electionEvent) {
		final List<LocaleText> localeTextList = new ArrayList<>();

		final TextId textId = new TextId();
		textId.setTextId(textIdStr);
		if (electionEvent != null) {
			textId.setElectionEvent(electionEvent);
		}

		textIdRepository.create(userData, textId);
		for (Locale locale : localeRepository.findAllLocales()) {
			localeTextList.add(createLocaleText(userData, textId, text, locale.getPk()));
		}
		return localeTextList;
	}

	/**
	 * Create a locale text for all locales for specific election event
	 */
	public List<LocaleText> createLocaleTextToAllLocaleForEvent(final UserData userData, final String textIdStr, final String text,
			final ElectionEvent electionEvent) {
		final List<LocaleText> localeTextList = new ArrayList<>();

		final TextId textId = new TextId();
		textId.setTextId(textIdStr);
		if (electionEvent != null) {
			textId.setElectionEvent(electionEvent);
		}

		textIdRepository.create(userData, textId);

		// Only create locale texts for the locales defined on the election event
		for (Locale locale : electionEventRepository.getLocalesForEvent(electionEvent)) {
			localeTextList.add(createLocaleText(userData, textId, text, locale.getPk()));
		}
		return localeTextList;
	}

	/**
	 * Update a locale text for all locales
	 */
	public void updateLocaleTextToAllLocale(final UserData userData, final String textIdString, final String text, final ElectionEvent electionEvent) {
		final TextId textId = translationService.findTextIdByElectionEvent(electionEvent, textIdString);
		for (LocaleText localeText : localeTextRepository.findByTextId(textId.getPk())) {
			localeText.setLocaleText(text);
			localeTextRepository.update(userData, localeText);
		}
	}

	public LocaleText createLocaleText(final UserData userData, final TextId textId, final String text, final Long localePk) {
		final LocaleText localeText = new LocaleText();
		localeText.setLocale(localeRepository.findByPk(localePk));
		localeText.setLocaleText(text);
		localeText.setTextId(textId);
		localeTextRepository.create(userData, localeText);
		return localeText;
	}

	public LocaleText createLocaleTextForElectionEvent(final UserData userData, final String textIdStr, final String text, final Locale locale,
			final ElectionEvent electionEvent) {
		TextId textId = translationService.findTextIdByElectionEvent(electionEvent, textIdStr);

		if (textId == null) {
			throw new EvoteException("Unable to create texts for new locale, the following translation is missing: " + textIdStr);
		}

		final LocaleText localeText = new LocaleText();
		localeText.setLocale(locale);
		localeText.setLocaleText(text);
		localeText.setTextId(textId);
		localeTextRepository.create(userData, localeText);
		return localeText;
	}
}
