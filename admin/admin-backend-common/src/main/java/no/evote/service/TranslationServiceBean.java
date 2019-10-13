package no.evote.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import no.evote.exception.EvoteTranslationValidationException;
import no.evote.security.UserData;
import no.valg.eva.admin.util.AntiSamyFilter;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.common.repository.TextIdRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.TextId;

import org.apache.log4j.Logger;

/**
 * Methods for fetching and maintaining translations. Entities are detached before returned. The reason for this is: We apply AntiSamy filtering on translations
 * to and from the database. If this alters translations that have been retrieved from the database, Hibernate will try to update the database (unless we detach
 * them). And in some cases (e.g. in the list proposal apps) we don't have write access to the translation tables.
 */
@Default
@ApplicationScoped
public class TranslationServiceBean {
	private static final Logger LOGGER = Logger.getLogger(TranslationServiceBean.class);

	@Inject
	private TextIdRepository textIdRepository;
	@Inject
	private LocaleRepository localeRepository;
	@Inject
	private LocaleTextRepository localeTextRepository;

	/**
	 * Update global translations. Is in a separate method because of securable object.
	 */
	public void updateGlobalLocaleTexts(UserData userData, Long localePk, Map<String, String> localeTexts, boolean addNewTextIds) {
		updateLocaleTextsImpl(userData, null, localePk, localeTexts, addNewTextIds);
	}

	/**
	 * Update local translations. Is in a separate method because of securable object.
	 */
	public void updateLocaleTexts(UserData userData, ElectionEvent electionEvent, Long localePk, Map<String, String> localeTexts, boolean addNewTextIds) {
		updateLocaleTextsImpl(userData, electionEvent, localePk, localeTexts, addNewTextIds);
	}

	/**
	 * Update either local or global translations.
	 */
	private void updateLocaleTextsImpl(UserData userData, ElectionEvent electionEvent, Long localePk, Map<String, String> localeTexts, boolean addNewTextIds) {
		String textIdStr = null;
		try {
			Locale locale = localeRepository.findByPk(localePk);
			Map<String, LocaleText> existingLocaleTexts = findLocaleTextMapByElectionEvent(electionEvent, localePk);
			Map<String, TextId> existingTextIds = findTextIdMapByElectionEvent(electionEvent);

			for (Entry<String, String> entry : localeTexts.entrySet()) {
				textIdStr = entry.getKey();
				String newText = entry.getValue();

				LocaleText localeText = existingLocaleTexts.get(textIdStr);
				if (localeText == null) {
					if (newText == null) {
						continue;
					}

					TextId textId = existingTextIds.get(textIdStr);
					if (textId == null) {
						if (addNewTextIds) {
							textId = new TextId(electionEvent, textIdStr, null);
							textId = textIdRepository.create(userData, textId);
						} else {
							LOGGER.warn("Ignoring translation for unknown text id: '" + textIdStr + "'");
							continue;
						}
					}
					localeText = new LocaleText(locale, textId, newText);
					localeTextRepository.create(userData, localeText);
				} else {
					if (newText != null) {
						if (!newText.equals(localeText.getLocaleText())) {
							localeText.setLocaleText(newText);
							localeTextRepository.update(userData, localeText);
						}
					} else {
						localeTextRepository.delete(userData, localeText.getPk());
					}
				}
			}
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
			if (!constraintViolations.isEmpty()) {
				ConstraintViolation violation = constraintViolations.iterator().next();
				LOGGER.warn(violation.getConstraintDescriptor().getAnnotation().annotationType(), e);
				throw new EvoteTranslationValidationException(textIdStr, e);
			}
		}
	}

	public Map<String, String> getLocaleTexts(ElectionEvent electionEvent, Long localePk) {
		List<Object[]> result;
		if (electionEvent != null) {
			result = localeTextRepository.findByElectionEvent(electionEvent.getPk(), localePk);
		} else {
			result = localeTextRepository.findGlobal(localePk);
		}

		Map<String, String> texts = new HashMap<>();
		for (Object[] row : result) {
			String textId = (String) row[0];
			String localeText = AntiSamyFilter.filter((String) row[1]);
			texts.put(textId, localeText);
		}

		return texts;
	}

	public TextId findTextIdByElectionEvent(ElectionEvent electionEvent, String textIdStr) {
		if (electionEvent != null) {
			return textIdRepository.findByElectionEventAndId(electionEvent.getPk(), textIdStr);
		} else {
			return textIdRepository.findGlobalById(textIdStr);
		}
	}

	public List<TextId> findTextIdsByElectionEvent(ElectionEvent electionEvent) {
		if (electionEvent != null) {
			return textIdRepository.findByElectionEvent(electionEvent.getPk());
		} else {
			return textIdRepository.findGlobal();
		}
	}

	private Map<String, TextId> findTextIdMapByElectionEvent(ElectionEvent electionEvent) {
		List<TextId> textIdList = findTextIdsByElectionEvent(electionEvent);

		Map<String, TextId> textIdMap = new HashMap<>();
		for (TextId textId : textIdList) {
			textIdMap.put(textId.getTextId(), textId);
		}

		return textIdMap;
	}

	private Map<String, LocaleText> findLocaleTextMapByElectionEvent(ElectionEvent electionEvent, Long localePk) {
		List<LocaleText> localeTexts;
		if (electionEvent != null) {
			localeTexts = localeTextRepository.findByElectionEventLocale(electionEvent.getPk(), localePk);
		} else {
			localeTexts = localeTextRepository.findGlobalByLocale(localePk);
		}
		Map<String, LocaleText> localeTextMap = new HashMap<>();
		for (LocaleText localeText : localeTexts) {
			localeTextMap.put(localeText.getTextId().getTextId(), localeText);
		}
		return localeTextMap;
	}

	public LocaleText getLocaleTextByElectionEvent(Long electionEventPk, Long localePk, String textId) {
		if (electionEventPk != null) {
			return localeTextRepository.findByElectionEventLocaleAndTextId(electionEventPk, localePk, textId);
		} else {
			return localeTextRepository.findGlobalByLocaleAndTextId(localePk, textId);
		}
	}

	/**
	 * Update local text id descriptions. Is in a separate method because of securable object.
	 */
	public void updateTextIdDescriptions(UserData userData, ElectionEvent electionEvent, Map<String, String> textIdDescriptions) {
		updateTextIdDescriptionsImpl(userData, electionEvent, textIdDescriptions);
	}

	/**
	 * Update global text id descriptions. Is in a separate method because of securable object.
	 */
	public void updateGlobalTextIdDescriptions(UserData userData, Map<String, String> textIdDescriptions) {
		updateTextIdDescriptionsImpl(userData, null, textIdDescriptions);
	}

	/**
	 * Update global and local text id descriptions.
	 */
	private void updateTextIdDescriptionsImpl(UserData userData, ElectionEvent electionEvent, Map<String, String> textIdDescriptions) {
		TextId textId;
		Map<String, TextId> existingTextIds = findTextIdMapByElectionEvent(electionEvent);

		for (Entry<String, String> entry : textIdDescriptions.entrySet()) {
			String textIdStr = entry.getKey();
			String description = entry.getValue();

			if (existingTextIds.get(textIdStr) == null) {
				LOGGER.warn("Ignoring description for unknown text id: '" + textIdStr + "'");
				continue;
			}

			textId = findTextIdByElectionEvent(electionEvent, textIdStr);
			textId.setInfoText(description);
			textIdRepository.update(userData, textId);
		}
	}

	public void deleteTextId(UserData userData, TextId textId) {
		if (textId != null && textId.getPk() != null) {
			for (LocaleText localeText : localeTextRepository.findByTextId(textId.getPk())) {
				localeTextRepository.delete(userData, localeText.getPk());
			}
			textIdRepository.delete(userData, textId.getPk());
		}
	}
}
