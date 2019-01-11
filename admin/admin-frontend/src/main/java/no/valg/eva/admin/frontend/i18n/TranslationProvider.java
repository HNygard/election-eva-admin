package no.valg.eva.admin.frontend.i18n;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.evote.service.TranslationService;
import no.valg.eva.admin.configuration.domain.model.LocaleText;

/**
 * Per user object that keeps track of user locale and bundle, and provides an interface for getting localized texts.
 */
@SessionScoped
public class TranslationProvider implements Serializable {
	private static final long serialVersionUID = 4628147159308772947L;
	private static final String QUESTION_MARKS = "???";

	private static final String[] L10NPREFIXES = { "@", "javax" };

	@Inject
	private Instance<UserData> userDataInstance;
	@Inject
	private transient ResourceBundleManager messageManager;
	@Inject
	private TranslationService translationService;

	private ResourceBundle bundle = null;

	@PostConstruct
	public void postConstruct() {
		bundle = messageManager.getBundle(getUserLocale());
	}

	public String getByElectionEvent(String key, Long electionEventPk) {
		if (isNotLocalizationKey(key)) {
			return key;
		}

		LocaleText lt = translationService.getLocaleTextByElectionEvent(electionEventPk, userDataInstance.get().getLocale().getPk(), key);
		String value;
		if (lt == null || lt.getLocaleText() == null) {
			value = getText(key, false);
		} else {
			value = lt.getLocaleText();
		}
		return value;
	}

	public String get(String key, Object... params) {
		return getText(key, true, params);
	}

	private String getText(String key, boolean searchWithElectionEvent, Object... params) {
		if (isNotLocalizationKey(key)) {
			return key;
		}

		String message;
		try {
			message = (String) bundle.getObject(key);
			if (params != null) {
				MessageFormat mf = new MessageFormat(message, getUserLocale());
				message = mf.format(params);
			}

			return message;
		} catch (MissingResourceException e) {
			if (searchWithElectionEvent) {
				message = getByElectionEvent(key, userDataInstance.get().getElectionEventPk());
				if (message != null) {
					return message;
				}
			}
			return QUESTION_MARKS + key + QUESTION_MARKS;
		}
	}

	public String getWithTranslatedParams(String key, String... params) {
		Object[] translatedParams = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			translatedParams[i] = get(params[i]);
		}
		return get(key, translatedParams);
	}

	private boolean isNotLocalizationKey(String key) {
		return !key.startsWith(L10NPREFIXES[0]) && !key.startsWith(L10NPREFIXES[1]);
	}

	private Locale getUserLocale() {
		return userDataInstance.get() != null ? userDataInstance.get().getJavaLocale() : EvoteConstants.DEFAULT_JAVA_LOCALE;
	}

	void reloadBundle() {
		bundle = messageManager.getBundle(getUserLocale());
	}

}
