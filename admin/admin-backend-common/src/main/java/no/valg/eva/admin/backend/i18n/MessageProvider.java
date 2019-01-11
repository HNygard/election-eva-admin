package no.valg.eva.admin.backend.i18n;

import lombok.NoArgsConstructor;
import no.evote.exception.EvoteException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static lombok.AccessLevel.PRIVATE;
import static no.valg.eva.admin.frontend.i18n.ResourceBundleManager.EVA_MESSAGES_BUNDLE;

@NoArgsConstructor(access = PRIVATE)
public class MessageProvider {
	
	public static String get(Locale locale, String key) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(EVA_MESSAGES_BUNDLE, locale);	
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			throw new EvoteException("Exception getting message by [key: " + key + ", locale: " + locale + "]: " + e);
		}
	}
	
	static String getWithTranslatedParameters(Locale locale, String key, String... params) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(EVA_MESSAGES_BUNDLE, locale);
		try {
			return MessageFormat.format(resourceBundle.getString(key), params);
		} catch (MissingResourceException e) {
			throw new EvoteException("Exception getting message by [key: " + key + ", locale: " + locale + ", params: " + params + "]: " + e);
		}
	}

}
