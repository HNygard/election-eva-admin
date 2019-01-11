package no.valg.eva.admin.frontend.i18n;

import javax.enterprise.context.ApplicationScoped;
import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
public class ResourceBundleManager {

	public static final String EVA_MESSAGES_BUNDLE = "no.valg.eva.admin.common.Messages";

	public ResourceBundle getBundle(final Locale locale) {
		return ResourceBundle.getBundle(EVA_MESSAGES_BUNDLE, locale);
	}
}
