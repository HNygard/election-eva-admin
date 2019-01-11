package no.valg.eva.admin.frontend.i18n;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import no.valg.eva.admin.frontend.cdi.BeanManager;

import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

public class ValidationResourceBundleLocator implements ResourceBundleLocator, Serializable {
	private static final long serialVersionUID = -2854349420095568493L;
	private transient ResourceBundleManager resourceBundleManager;

	@Override
	@SuppressWarnings("rawtypes")
	public ResourceBundle getResourceBundle(final Locale locale) {
		return getResourceBundleManager().getBundle(locale);
	}

	public ResourceBundleManager getResourceBundleManager() {
		if (resourceBundleManager == null) {
			resourceBundleManager = BeanManager.lookup(ResourceBundleManager.class);
		}
		return resourceBundleManager;
	}
}
