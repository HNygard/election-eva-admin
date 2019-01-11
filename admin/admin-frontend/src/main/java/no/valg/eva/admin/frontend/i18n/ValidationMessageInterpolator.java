package no.valg.eva.admin.frontend.i18n;

import java.util.Locale;

import javax.validation.MessageInterpolator;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

public class ValidationMessageInterpolator implements MessageInterpolator {

	private final ResourceBundleMessageInterpolator defaultInterpolator;

	public ValidationMessageInterpolator() {
		defaultInterpolator = new ResourceBundleMessageInterpolator(new ValidationResourceBundleLocator(), false);
	}

	@Override
	public String interpolate(final String messageTemplate, final Context context) {
		return defaultInterpolator.interpolate(messageTemplate, context);
	}

	@Override
	public String interpolate(final String messageTemplate, final Context context, final Locale locale) {
		return defaultInterpolator.interpolate(messageTemplate, context, locale);
	}

}
