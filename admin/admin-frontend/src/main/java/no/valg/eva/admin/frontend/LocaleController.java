package no.valg.eva.admin.frontend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.service.TranslationService;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

/**
 * For changing the language used in the user interface.
 */
@Named
@SessionScoped
public class LocaleController extends BaseController {

	@Inject
	private TranslationService translationService;
	@Inject
	private MessageProvider messageProvider;

	private Map<String, Locale> localeMap;
	private Map<String, LocaleId> localeIdMap;

	@PostConstruct
	public void init() {
		localeMap = new HashMap<>();
		localeIdMap = new HashMap<>();
		List<Locale> localeList = translationService.getAllLocales();

		for (Locale locale : localeList) {
			localeMap.put(locale.getId(), locale);

			localeIdMap.put(locale.getId(), locale.toLocaleId());
		}
	}

	public Map<String, LocaleId> getLocaleIdMap() {
		return localeIdMap;
	}

	@Deprecated
	public Map<String, Locale> getLocaleMap() {
		return localeMap;
	}

	public String buildLocaleKey(LocaleId localeId) {
		if (localeId != null) {
			return messageProvider.get(localeId.getName());
		} else {
			return "";
		}
	}
}
